package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.Phrasal;
import edu.stanford.nlp.mt.lm.*;
import edu.stanford.nlp.mt.util.IOTools;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.util.StringUtils;

import java.io.*;
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
    private String iniFilePath;

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
    }

    public void runConsoleInteractiveModeDecoding() {
        this.runConsoleInteractiveModeDecoding(null);
    }

    public void runConsoleInteractiveModeDecoding(String [] userArgs) {
        try {
            String[] decode_args = new String[13];
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
            decode_args[11] = "hierarchical" + " " + reorderingModelFilePath + " " + "msd2-bidirectional-fe";
            decode_args[12] = iniFilePath;

            Phrasal phrasal = null;
            if (userArgs != null) {
                phrasal = prepareDecoding(userArgs);
            } else {
                phrasal = prepareDecoding(decode_args);
            }

            phrasal.decode(System.in, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runFileDecoding(String fileToBeTranslatedPath, String translationOutputFilePath) {
        this.runFileDecoding(fileToBeTranslatedPath, translationOutputFilePath, null);
    }

    public void runFileDecoding(String fileToBeTranslatedPath, String translationOutputFilePath, String [] userArgs) {
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

            String[] decode_args = new String[15];
            String toTranslatePath = toTranslate.getCanonicalPath();
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
            decode_args[11] = "hierarchical" + " " + reorderingModelFilePath + " " + "msd2-bidirectional-fe";
            decode_args[12] = "-text";
            decode_args[13] = toTranslatePath;
            decode_args[14] = iniFilePath;

            Phrasal phrasal = null;
            if (userArgs != null) {
                phrasal = prepareDecoding(userArgs);
            } else {
                phrasal = prepareDecoding(decode_args);
            }

            //Save standard output
            PrintStream stdout = System.out;
            //Set standard output to specified file (file with translation)
            PrintStream output = new PrintStream(new FileOutputStream(translationOutput.getAbsoluteFile()));
            System.setOut(output);
            //Start translation
            phrasal.decode(new FileInputStream(toTranslate), true);
            //Restore standard output
            System.setOut(stdout);
        } catch (Exception e) {
            e.printStackTrace();
        }
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