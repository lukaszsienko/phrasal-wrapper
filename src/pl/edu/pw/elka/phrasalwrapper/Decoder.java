package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.Phrasal;
import edu.stanford.nlp.mt.lm.*;
import edu.stanford.nlp.mt.util.IOTools;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by lsienko on 27.04.18.
 */
public class Decoder {

    private LanguageModel languageModel;
    private TranslationModel translationModel;

    public Decoder(LanguageModel languageModel, TranslationModel translationModel) {
        this.languageModel = languageModel;
        this.translationModel = translationModel;
    }

    public void runDecoding(String fileToTranslatePath) {
        try {
            File ini_file = new File(getClass().getResource("/phrasal.ini").getPath());
            String ini_file_path = ini_file.getAbsolutePath();

            File phrase_table_file = new File(translationModel.getPath() + "/phrase-table.gz");
            String phrase_table_file_path = phrase_table_file.getAbsolutePath();

            File lang_model_file = new File(languageModel.getOutputFolder()+"/"+languageModel.getModelBinaryFileName());
            String lang_model_file_path = lang_model_file.getAbsolutePath();

            File toTranslate = new File(fileToTranslatePath);
            String toTranslatePath = toTranslate.getAbsolutePath();

            String[] decode_args = new String[15];
            decode_args[0] = "-ttable-file";
            decode_args[1] = phrase_table_file_path;
            decode_args[2] = "-lmodel-file";
            decode_args[3] = "kenlm:"+lang_model_file_path;
            decode_args[4] = "-ttable-limit";
            decode_args[5] = "20";
            decode_args[6] = "-distortion-limit";
            decode_args[7] = "5";
            decode_args[8] = "-threads";
            decode_args[9] = "2";

            File reordering_model_file = new File(translationModel.getPath() + "lo-hier.msd2-bidirectional-fe.gz");
            String reordering_model_file_path = reordering_model_file.getAbsolutePath();

            decode_args[10] = "-reordering-model";
            decode_args[11] = "hierarchical" + " " + reordering_model_file_path + " " + "msd2-bidirectional-fe";
            decode_args[12] = "-text";
            decode_args[13] = toTranslatePath; //TODO check this
            decode_args[14] = ini_file_path;

            runDecoding(decode_args);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runDecoding(String[] args) throws Exception {
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

        System.out.println("Loading lang model...");
        edu.stanford.nlp.mt.lm.LanguageModel<IString> lm = LanguageModelFactory.load(options.getProperty("lmodel-file"));
        System.out.println("Loading translation model...");
        final Phrasal p = Phrasal.loadDecoder(configuration, lm);
        System.out.println("Type sth to translate:");

        p.decode(System.in, true);
        /*if (options.containsKey("text")) p.runDecoding(new FileInputStream(new File(options.getProperty("text"))), true);
        else p.runDecoding(System.in, true);*/
    }

    private static Map<String, List<String>> getConfigurationFrom(String configFile, Properties options)
            throws IOException {
        final Map<String, List<String>> config = configFile == null ? new HashMap<>()
                : IOTools.readConfigFile(configFile);
        // Command-line options supersede config file options
        options.entrySet().stream().forEach(e -> config.put(e.getKey().toString(),
                Arrays.asList(e.getValue().toString().split("\\s+"))));
        return config;
    }
}
