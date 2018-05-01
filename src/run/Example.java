package run;

import pl.edu.pw.elka.phrasalwrapper.*;

/**
 * Created by lsienko on 27.04.18.
 */
public class Example {
    //TODO move this out of the "run" package and remove "run" package

    public static void exampleUseCase() throws Exception {
        ParallerCorpus corpus = new ParallerCorpus("/home/lsienko/Pobrane/test/europarl-v7.pl-en.en.tok.en",
                "/home/lsienko/Pobrane/test/europarl-v7.pl-en.pl.tok.pl");
        //corpus.tokenize();

        WordAlignmentModel alignmentModel = new WordAlignmentModel(corpus);
        //alignmentModel.runWordAlignmentProcess();

        LanguageModel languageModel = new LanguageModel(5, corpus);
        //languageModel.buildLanguageModel();

        TranslationModel translationModel = new TranslationModel(alignmentModel, corpus);
        translationModel.buildTranslationModel();

        Decoder decoder = new Decoder(languageModel, translationModel);
        decoder.runConsoleInteractiveModeDecoding();
    }

    public static void main(String[] args) throws Exception {
        //Phrasal.main(new String[] {"-help"});
        exampleUseCase();
    }
}


/**
 * //////////////////////////////////
 *         BufferedReader stdInput = new BufferedReader(new
 *                 InputStreamReader(engProcess.getInputStream()));
 *
 *         BufferedReader stdError = new BufferedReader(new
 *                 InputStreamReader(engProcess.getErrorStream()));
 *
 * // read the output from the command
 *         System.out.println("Here is the standard output of the command:\n");
 *         String s = null;
 *         while ((s = stdInput.readLine()) != null) {
 *             System.out.println(s);
 *         }
 *
 * // read any errors from the attempted command
 *         System.out.println("Here is the standard error of the command (if any):\n");
 *         while ((s = stdError.readLine()) != null) {
 *             System.out.println(s);
 *         }
 * //////////////////////////////////
 *         System.err.println("Exit status=" + engProcess.exitValue());
 */

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