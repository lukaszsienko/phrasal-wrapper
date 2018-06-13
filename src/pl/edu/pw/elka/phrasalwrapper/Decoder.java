package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.Phrasal;
import edu.stanford.nlp.mt.lm.*;
import edu.stanford.nlp.mt.util.IOTools;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.util.StringUtils;
import pl.edu.pw.elka.phrasalwrapper.translation_model.TranslationModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Decoder {

    private String phraseTableFilePath;
    private String reorderingModelFilePath;
    private String languageModelFilePath;
    private String tunerOutputFilePath;
    private String iniFilePath;
    private final PrintStream STD_OUT;
    private ModelsOutputDirectory modelsOutputDirectory;

    private Phrasal loadedDecodingModel;

    public Decoder(LanguageModel languageModel, TranslationModel translationModel, ModelsOutputDirectory modelsOutputDirectory, TranslationTuner tuner) throws IOException{
        this(languageModel, translationModel, modelsOutputDirectory);
        this.tunerOutputFilePath = tuner.getTunerFinalWeightsFilePath();
    }

    public Decoder(LanguageModel languageModel, TranslationModel translationModel, ModelsOutputDirectory modelsOutputDirectory) throws IOException {
        File phraseTableFile = new File(translationModel.getOutputFolder() + "/phrase-table.gz");
        if (!phraseTableFile.exists()) {
            System.err.println("\nCheck if you've built translation model by calling TranslationModel.buildTranslationModel() method before.");
        }
        File reorderingModelFile = new File(translationModel.getOutputFolder() + "/lo-hier.msd2-bidirectional-fe.gz");
        if (!reorderingModelFile.exists()) {
            System.err.println("\nCheck if you've built translation model by calling TranslationModel.buildTranslationModel() method before.");
        }
        this.phraseTableFilePath = phraseTableFile.getCanonicalPath();
        this.reorderingModelFilePath = reorderingModelFile.getCanonicalPath();

        File langModelFile = new File(languageModel.getOutputFolder()+"/"+languageModel.getModelBinaryFileName());
        if (!langModelFile.exists()) {
            System.err.println("\nCheck if you've built language model by calling LanguageModel.buildLanguageModel() method before.");
        }
        this.languageModelFilePath = langModelFile.getCanonicalPath();

        try {
            Path iniFilePath = Utilities.getResourcePath("/phrasal.ini");
            File dstIniFile = new File(translationModel.getOutputFolder()+"/phrasal.ini");
            Files.copy(iniFilePath, dstIniFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.iniFilePath = dstIniFile.getCanonicalPath();
        } catch (IOException exp) {
            exp.printStackTrace();
        }

        STD_OUT = System.out;
        this.modelsOutputDirectory = modelsOutputDirectory;
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
        String[] decode_args = getDecodingParameters();
        try {
            loadedDecodingModel = loadDecoding(decode_args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getDecodingParameters() {
        String[] decode_args;
        boolean withTuning = this.tunerOutputFilePath != null && !this.tunerOutputFilePath.isEmpty();
        if (withTuning) {
            decode_args = new String[15];
        } else {
            decode_args = new String[13];
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
            decode_args[14] = iniFilePath;
        } else {
            decode_args[12] = iniFilePath;
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

        modelsOutputDirectory.reloadKenLMexecutables();

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