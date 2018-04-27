import pl.edu.pw.elka.phrasalwrapper.*;

import java.io.FileNotFoundException;

/**
 * Created by lsienko on 27.04.18.
 */
public class Example {

    public void exampleUseCase(String fileToTranslatePath) throws Exception {
        ParallerCorpus corpus = new ParallerCorpus("/home/lsienko/Pobrane/test/europarl-v7.pl-en.en",
                "/home/lsienko/Pobrane/test/europarl-v7.pl-en.pl");
        corpus.tokenize();

        WordAlignmentModel alignmentModel = new WordAlignmentModel(corpus);
        alignmentModel.runWordAligmentProcess();

        LanguageModel languageModel = new LanguageModel(5, corpus);
        languageModel.buildLanguageModel();

        TranslationModel translationModel = new TranslationModel(alignmentModel);
        translationModel.buildTranslationModel();

        Decoder decoder = new Decoder(languageModel, translationModel);
        decoder.runDecoding(fileToTranslatePath);
    }

}
