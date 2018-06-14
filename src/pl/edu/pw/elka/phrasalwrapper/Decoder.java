package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.Phrasal;
import edu.stanford.nlp.mt.lm.*;
import edu.stanford.nlp.mt.util.IOTools;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.util.StringUtils;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelFile;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Decoder {

    private String phraseTableFilePath;
    private String reorderingModelFilePath;
    private String languageModelFilePath;
    private String tunerOutputFilePath;
    private final PrintStream STD_OUT;
    private ModelsPersistence modelsPersistence;

    private Phrasal loadedDecodingModel;

    public Decoder(ModelsPersistence modelsPersistence) throws IOException {
        try {
            this.tunerOutputFilePath = modelsPersistence.getDetectedModelFilePath(ModelFile.TUNER_WEIGHTS);
        } catch (IOException exp) {
            System.out.println("Phrasal-wrapper warning: running decoding without loading model tuning weights file.");
        }
        this.languageModelFilePath = modelsPersistence.getDetectedModelFilePath(ModelFile.LANG_MODEL_BIN);
        this.phraseTableFilePath = modelsPersistence.getDetectedModelFilePath(ModelFile.TRANSLATION_PHRASE_TABLE);
        this.reorderingModelFilePath = modelsPersistence.getDetectedModelFilePath(ModelFile.TRANSLATION_REORDERING_MODEL);

        STD_OUT = System.out;
        this.modelsPersistence = modelsPersistence;
    }

    public String translateSentence(String sentenceToTranslate) {
        if (loadedDecodingModel == null) {
            loadDecodingModel();
        }

        String input = Tokenizer.cleanText(sentenceToTranslate);
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream outputStream = null;
        try {
            outputStream = new PrintStream(output, true, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.setOut(outputStream);

        try {
            loadedDecodingModel.decode(inputStream, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setOut(STD_OUT); //Restore standard output

        String translatedSentence = new String(output.toByteArray(), StandardCharsets.UTF_8);

        try {
            output.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        translatedSentence = translatedSentence.replaceAll("\r", "").replaceAll("\n", "");

        return translatedSentence;
    }

    public void loadDecodingModel() {
        Utilities.printMessage("Started loading decode model...");
        String[] decode_args = getDecodingParameters();
        try {
            loadedDecodingModel = loadDecoding(decode_args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utilities.printMessage("Finished loading decode model.");
    }

    private String[] getDecodingParameters() {
        String[] decode_args;
        boolean withTuning = this.tunerOutputFilePath != null && !this.tunerOutputFilePath.isEmpty();
        if (withTuning) {
            decode_args = new String[14];
        } else {
            decode_args = new String[12];
        }
        decode_args[0] = "-ttable-file";
        decode_args[1] = phraseTableFilePath;
        decode_args[2] = "-lmodel-file";
        decode_args[3] = "kenlm:"+languageModelFilePath;
        decode_args[4] = "-ttable-limit";
        decode_args[5] = "20";
        decode_args[6] = "-distortion-limit";
        decode_args[7] = "5";
        decode_args[8] = "-threads";
        decode_args[9] = "2";
        decode_args[10] = "-reordering-model";
        decode_args[11] = "hierarchical" + " " + reorderingModelFilePath + " " + "msd2-bidirectional-fe" + " hierarchical hierarchical bin";
        if (withTuning) {
            decode_args[12] = "-weights-file";
            decode_args[13] = tunerOutputFilePath;
        }
        return decode_args;
    }

    private Phrasal loadDecoding(String[] args) throws Exception {
        final Properties options = StringUtils.argsToProperties(args);
        final String configFile = options.containsKey("") ? (String) options.get("") : null;
        options.remove("");

        // by default, exit on uncaught exception
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            System.err.println("Uncaught top-level exception");
            ex.printStackTrace();
            System.exit(-1);
        });

        final Map<String, List<String>> configuration = getConfigurationFrom(configFile, options);

        Utilities.extractAndLoadKenLMLibrary(modelsPersistence);

        edu.stanford.nlp.mt.lm.LanguageModel<IString> lm = LanguageModelFactory.load(options.getProperty("lmodel-file"));
        final Phrasal phrasal = Phrasal.loadDecoder(configuration, lm);

        return phrasal;
    }

    private Map<String, List<String>> getConfigurationFrom(String configFile, Properties options) throws IOException {
        final Map<String, List<String>> config = configFile == null ? new HashMap<>()
                : IOTools.readConfigFile(configFile);
        // Command-line options supersede config file options
        options.entrySet().stream().forEach(e -> config.put(e.getKey().toString(),
                Arrays.asList(e.getValue().toString().split("\\s+"))));
        return config;
    }
}