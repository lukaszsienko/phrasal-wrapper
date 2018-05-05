package pl.edu.pw.elka.phrasalwrapper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lsienko on 27.04.18.
 */
public class Example {

    public static void exampleUseCase(String englishFilePath, String foreignFilePath) throws Exception {
        printCurrentTimestamp();
        System.out.println("Program started. Starting tokenization...");
        Date start = new Date();//////////////

        ParallerCorpus corpus = new ParallerCorpus(englishFilePath, foreignFilePath);
        corpus.tokenize();

        Date token_end = new Date();/////////////
        System.out.println("Tokenization finished. It takes ");
        printDifference(start, token_end);

        System.out.println("Alignment starting...");
        Date start_align = new Date();//////////////

        WordAlignmentModel alignmentModel = new WordAlignmentModel(corpus);
        alignmentModel.runWordAlignmentProcess();

        Date end_align = new Date();/////////////
        System.out.println("Alignment finished. It takes ");
        printDifference(start_align, end_align);
        System.out.println("Total time from program start: ");
        printDifference(start, end_align);

        System.out.println("Language model starting...");
        Date language_model_start = new Date();/////////////

        LanguageModel languageModel = new LanguageModel(5, corpus);
        languageModel.buildLanguageModel();

        Date language_model_end = new Date();/////////////
        System.out.println("Language model finished. It takes ");
        printDifference(language_model_start, language_model_end);
        System.out.println("Total time from program start: ");
        printDifference(start, language_model_end);

        System.out.println("Translation model starting...");
        Date translation_model_start = new Date();/////////////

        TranslationModel translationModel = new TranslationModel(alignmentModel, corpus);
        translationModel.buildTranslationModel();

        Date translation_model_end = new Date();/////////////
        System.out.println("Translation model finished. It takes ");
        printDifference(translation_model_start, translation_model_end);
        System.out.println("Total time from program start: ");
        printDifference(start, translation_model_end);

        System.out.println("Decoding starting...");
        printCurrentTimestamp();

        Decoder decoder = new Decoder(languageModel, translationModel);
        decoder.runConsoleInteractiveModeDecoding();
    }

    public static void printCurrentTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        Date now = new Date();
        System.out.println(formatter.format(now));
    }

    public static void printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays,
                elapsedHours, elapsedMinutes, elapsedSeconds);
    }


    public static void main(String[] args) throws Exception {
        /*String englishFilePath = "/home/lsienko/Pobrane/test/europarl-v7.pl-en.en";
        String foreignFilePath = "/home/lsienko/Pobrane/test/europarl-v7.pl-en.pl";*/

        String englishFilePath = args[0];
        String foreignFilePath = args[1];
        exampleUseCase(englishFilePath, foreignFilePath);
    }
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