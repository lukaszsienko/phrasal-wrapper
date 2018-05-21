package pl.edu.pw.elka.phrasalwrapper;

/**
 * Created by lsienko on 27.04.18.
 */
public class Example {

    public static void exampleUseCase(String englishFilePath, String foreignFilePath, String englishOnlyCorpusFilePath, String englishPartOfParallerTuningCorpusPath, String foreignPartOfParallerTuningCorpusPath) throws Exception {
        ParallerCorpus corpus = new ParallerCorpus(englishFilePath, foreignFilePath, englishOnlyCorpusFilePath);
        corpus.tokenize();

        WordAlignmentModel alignmentModel = new WordAlignmentModel(corpus);
        alignmentModel.runWordAlignmentProcess();

        LanguageModel languageModel = new LanguageModel(5, corpus);
        languageModel.buildLanguageModel();

        TranslationModel translationModel = new TranslationModel(alignmentModel, corpus);
        translationModel.buildTranslationModel();

        TranslationTuner tuner = new TranslationTuner(englishPartOfParallerTuningCorpusPath, foreignPartOfParallerTuningCorpusPath, corpus, languageModel, translationModel);
        tuner.tokenizeTuningCorpus();
        tuner.runTuning();

        Decoder decoder = new Decoder(languageModel, translationModel, tuner);
        decoder.runDecodingFromConsoleInInteractiveMode();
    }

    public static void main(String[] args) throws Exception {
        /*String englishFilePath = "/home/lsienko/Pobrane/test/train/europarl-v7.pl-en.en.train-eng.en";
        String foreignFilePath = "/home/lsienko/Pobrane/test/train/europarl-v7.pl-en.pl.train-for.pl";
        String englishOnlyCorpusFilePath = "/home/lsienko/Pobrane/test/europarl-v7.en";
        String englishPartOfParallerTuningCorpusPath = "/home/lsienko/Pobrane/test/tune/europarl-v7.pl-en.en.tune-eng"
        String foreignPartOfParallerTuningCorpusPath = "/home/lsienko/Pobrane/test/tune/europarl-v7.pl-en.pl.tune-for" */

        String englishFilePath = args[0];
        String foreignFilePath = args[1];
        String englishOnlyCorpusFilePath = args[2];
        String englishPartOfParallerTuningCorpusPath = args[3];
        String foreignPartOfParallerTuningCorpusPath = args[4];
        exampleUseCase(englishFilePath, foreignFilePath, englishOnlyCorpusFilePath, englishPartOfParallerTuningCorpusPath, foreignPartOfParallerTuningCorpusPath);
    }
}