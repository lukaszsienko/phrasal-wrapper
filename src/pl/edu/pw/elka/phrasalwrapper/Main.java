package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.Phrasal;
import edu.stanford.nlp.mt.lm.LanguageModel;
import edu.stanford.nlp.mt.lm.LanguageModelFactory;
import edu.stanford.nlp.mt.train.PhraseExtract;
import edu.stanford.nlp.mt.util.IOTools;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by lsienko on 12.04.18.
 */
public class Main {
    public static void main(String[] args) {

        /**
         *
         *
         * java

         -server -ea -Xmx${MEM} -Xms${MEM} -XX:+UseParallelGC -XX:+UseParallelOldGC

         -Djava.library.path=/home/lsienko/phrasal/src-cc

         edu.stanford.nlp.mt.Phrasal

         $RUNNAME.ini
         newstest2012.newstest2011.baseline.ini

         -log-prefix $RUNNAME
         -log-prefix newstest2012.newstest2011.baseline


         <$DECODE_FILE

         >$RUNNAME.trans
         >newstest2012.newstest2011.baseline.trans

         > logs/$RUNNAME.log
         >logs/newstest2012.newstest2011.baseline.log
         *
         *
         *
         * Usage: java edu.stanford.nlp.mt.Phrasal OPTS [ini_file] < input > output

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
        try {

            /*File ini_file = new File("resources/phrasal.ini");
            String ini_file_path = ini_file.getAbsolutePath();

            File phrase_table_file = new File("resources/translation_model/phrase-table.gz");
            String phrase_table_file_path = phrase_table_file.getAbsolutePath();

            File lang_model_file = new File("resources/language_model/5gm.bin");
            String lang_model_file_path = lang_model_file.getAbsolutePath();*/

            String ini_file_path = args[0];
            String phrase_table_file_path = args[1];
            String lang_model_file_path = args[2];

            String[] decode_args = new String[13];
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

            /*File reordering_model_file = new File("resources/translation_model/lo-hier.msd2-bidirectional-fe.gz");
            String reordering_model_file_path = reordering_model_file.getAbsolutePath();*/
            String reordering_model_file_path = args[3];

            decode_args[10] = "-reordering-model";
            decode_args[11] = "hierarchical" + " " + reordering_model_file_path + " " + "msd2-bidirectional-fe";
            decode_args[12] = ini_file_path;

            //Phrasal.main(decode_args);
            decode(decode_args);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildTranslationModel() {
        // BUILDING TRANSLATION MODEL (PHRASE TABLE EXTRACTION)
        File berkeley_aligner_folder = new File("resources/aligner_output");
        String aligner_folder_path = berkeley_aligner_folder.getAbsolutePath();

        File translation_model_folder = new File("resources/translation_model");
        String translation_model_path = translation_model_folder.getAbsolutePath();

        //String aligner_folder_path = args[0];
        //String translation_model_path = args[1];

        String[] phrase_extract_args = new String[18];
        phrase_extract_args[0] = "-threads";
        phrase_extract_args[1] = "1";
        phrase_extract_args[2] = "-inputDir";
        phrase_extract_args[3] = aligner_folder_path;
        phrase_extract_args[4] = "-outputDir";
        phrase_extract_args[5] = translation_model_path;
        phrase_extract_args[6] = "-extractors";
        phrase_extract_args[7] = "edu.stanford.nlp.mt.train.MosesPharoahFeatureExtractor=phrase-table.gz:edu.stanford.nlp.mt.train.CountFeatureExtractor=phrase-table.gz:edu.stanford.nlp.mt.train.LexicalReorderingFeatureExtractor=lo-hier.msd2-bidirectional-fe.gz";
        phrase_extract_args[8] = "-hierarchicalOrientationModel";
        phrase_extract_args[9] = "true";
        phrase_extract_args[10] = "-orientationModelType";
        phrase_extract_args[11] = "msd2-bidirectional-fe";
        phrase_extract_args[12] = "-symmetrization";
        phrase_extract_args[13] = "grow-diag";
        phrase_extract_args[14] = "-phiFilter";
        phrase_extract_args[15] = "1e-4";
        phrase_extract_args[16] = "-maxELen";
        phrase_extract_args[17] = "5";

        System.setProperty("ShowPhraseRestriction", "true");

        try {
            PhraseExtract.main(phrase_extract_args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void decode(String[] args) throws Exception {
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
        LanguageModel<IString> lm = LanguageModelFactory.load(options.getProperty("lmodel-file"));
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
