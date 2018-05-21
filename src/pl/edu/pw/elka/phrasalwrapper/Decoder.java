package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.Phrasal;
import edu.stanford.nlp.mt.lm.*;
import edu.stanford.nlp.mt.util.IOTools;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by lsienko on 27.04.18.
 */
public class Decoder {

    private LanguageModel languageModel;
    private String phraseTableFilePath;
    private String reorderingModelFilePath;
    private String languageModelFilePath;
    private String tunerOutputFilePath;
    private String iniFilePath;
    private final PrintStream STD_OUT;

    private Phrasal loadedDecodingModel;

    public Decoder(LanguageModel languageModel, TranslationModel translationModel, TranslationTuner tuner) throws IOException{
        this(languageModel, translationModel);
        this.tunerOutputFilePath = tuner.getTunerFinalWeightsFilePath();
    }

    public Decoder(LanguageModel languageModel, TranslationModel translationModel, String tunerOutputFilePath) throws IOException{
        this(languageModel, translationModel);
        this.tunerOutputFilePath = tunerOutputFilePath;
    }

    public Decoder(LanguageModel languageModel, TranslationModel translationModel) throws IOException {
        this.languageModel = languageModel; //for loading kenLM library if not declared by user in -Djava.library.path=...

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
    }

    public void loadModelWithDefaultConfigInServerMode() {
        String[] decode_args = getDecodingFromConsoleParameters();
        try {
            loadedDecodingModel = prepareDecoding(decode_args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String translateSentenceInServerMode(String sentenceToTranslate) {
        if (loadedDecodingModel == null) {
            loadModelWithDefaultConfigInServerMode();
        }

        String input = Tokenizer.cleanTextBeforeProcessing(sentenceToTranslate);
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

        return translatedSentence;
    }

    public void runDecodingFromConsoleInInteractiveMode() {
        //TODO add Utilities.cleanTextBeforeProcessing of input from System.in, postponed because feature not used
        String[] decode_args = getDecodingFromConsoleParameters();
        try {
            loadedDecodingModel = prepareDecoding(decode_args);
            loadedDecodingModel.decode(System.in, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runDecodingFromConsoleInInteractiveMode(String [] userArgs) {
        //TODO add Utilities.cleanTextBeforeProcessing of input from System.in, postponed because feature not used
        try {
            loadedDecodingModel = prepareDecoding(userArgs);
            loadedDecodingModel.decode(System.in, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getDecodingFromConsoleParameters() {
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

    /*
         Usage: java edu.stanford.nlp.mt.Phrasal OPTS [ini_file] < input > output

         Phrasal: A phrase-based machine translation decoder from the Stanford NLP group.

         Command-line arguments override arguments specified in the optional ini_file:
         -text file : Filename of file to runDecoding

         -ttable-file filename : Translation model file. Multiple models can be specified by separating filenames with colons.
         -lmodel-file filename : Language model file. For KenLM, prefix filename with 'kenlm:'
         -ttable-limit num : Translation option limit.
         -n-best-list num : n-best list size.
         -distinct-n-best-list boolean : Generate distinct n-best lists (default: false)
         -  -force-runDecoding filename [filename] : Force runDecoding to reference file(s).
         -prefix-align-compounds boolean : Apply heuristic compound word alignmen for prefix decoding? Affects cube pruning decoder only. (default: false)
         -stack num : Stack/beam size.
         -search-algorithm [cube|multibeam] : Inference algorithm (default:cube)
         -reordering-model type filename [options] : Lexicalized re-ordering model where type is [classic|hierarchical]. Multiple models can be separating filenames with colons.
         -weights-file filename : Load all model weights from file.
         -max-sentence-length num : Maximum input sentence length.
         -min-sentence-length num : Minimum input sentence length.
         -distortion-limit num [cost] : Hard distortion limit and delay cost (default cost: 0.0).
         -additional-featurizers class [class] : List of additional feature functions.
         -disabled-featurizers class [class] : List of baseline featurizers to disable.
         -threads num : Number of decoding threads (default: 1)
         -use-itg-constraints boolean : Use ITG constraints for decoding (multibeam search only)
         -recombination-mode name : Recombination mode [pharoah,exact,dtu] (default: exact).
         -drop-unknown-words boolean : Drop unknown source words from the output (default: false)
         -independent-phrase-tables filename [filename] : Phrase tables that cannot have associated reordering models. Optionally supports custom per-table prefixes for features (e.g., pref:filename).
         -alignment-output-file filename : Output word-word alignments to file for each translation.
         -preprocessor-filter language [opts] : Pre-processor to apply to source input.
         -postprocessor-filter language [opts] : Post-processor to apply to target output.
         -source-class-map filename : Feature API: Line-delimited source word->class mapping (TSV format).
         -target-class-map filename : Feature API: Line-delimited target word->class mapping (TSV format).
         -gaps options : DTU: Enable Galley and Manning (2010) gappy decoding.
         -max-pending-phrases num : DTU: Max number of pending phrases for decoding.
         -gaps-in-future-cost boolean : DTU: Allow gaps in future cost estimate (default: true)
         -linear-distortion-options type : DTU: linear distortion type (default: standard)
         -print-model-scores boolean : Output model scores with translations (default: false)
         -input-properties file : File specifying properties of each source input.
         -feature-augmentation mode : Feature augmentation mode [all|dense|extended].
         -wrap-boundary boolean : Add boundary tokens around each input sentence (default: false).
         -ksr_nbest_size int : size of n-best list for KSR computation (default: 0, i.e. no KSR computation).
         -wpa_nbest_size int : size of n-best list for word prediction accuracy computation (default: 0, i.e. no WPA computation).
         -reference String : reference file for KSR/WPA computation.
 */

    public void runDecodingFromFile(String fileToBeTranslatedPath, String translationOutputFilePath) {
        this.runDecodingFromFile(fileToBeTranslatedPath, translationOutputFilePath, null);
    }

    public void runDecodingFromFile(String fileToBeTranslatedPath, String translationOutputFilePath, String [] userArgs) {
        //TODO add Utilities.cleanTextBeforeProcessing of input File contents, postponed because feature not used
        try {
            File toTranslate = new File(fileToBeTranslatedPath);
            if (!toTranslate.exists()) {
                throw new Exception("Specified file to translate does not exist.");
            }
            File translationOutput = new File(translationOutputFilePath);
            translationOutput.delete();
            boolean created = translationOutput.createNewFile();
            if (!created) {
                throw new Exception("Output file to could not be created.");
            }

            if (userArgs != null) {
                loadedDecodingModel = prepareDecoding(userArgs);
            } else {
                String toTranslatePath = toTranslate.getCanonicalPath();
                String[] decode_args = getDecodingFromFileParameters(toTranslatePath);
                loadedDecodingModel = prepareDecoding(decode_args);
            }

            //Set standard output to specified file (file with translation)
            PrintStream output = new PrintStream(new FileOutputStream(translationOutput.getAbsoluteFile()));
            System.setOut(output);
            //Start translation
            loadedDecodingModel.decode(new FileInputStream(toTranslate), true);
            //Restore standard output
            System.setOut(STD_OUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getDecodingFromFileParameters(String toTranslatePath) {
        String[] decode_args = new String[15];
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
        decode_args[12] = "-text";
        decode_args[13] = toTranslatePath;
        decode_args[14] = iniFilePath;

        return decode_args;
    }

    private Phrasal prepareDecoding(String[] args) throws Exception {
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

        this.languageModel.extractAndLoadKenLMLibrary();
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