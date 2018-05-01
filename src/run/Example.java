package run;

import edu.stanford.nlp.mt.Phrasal;
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
