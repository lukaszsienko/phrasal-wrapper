package pl.edu.pw.elka.phrasalwrapper;

import pl.edu.pw.elka.phrasalwrapper.translation_model.BerkeleyTranslationModel;
import pl.edu.pw.elka.phrasalwrapper.translation_model.TranslationModel;
import pl.edu.pw.elka.phrasalwrapper.word_alignment.BerkeleyWordAlignmentModel;

public class Example {

    public static void exampleUseCase(String foreignFilePath, String englishFilePath, String englishOnlyCorpusFilePath, String foreignPartOfParallerTuningCorpusPath, String englishPartOfParallerTuningCorpusPath, String modelOutputDir) throws Exception {
        ModelsOutputDirectory modelsOutputDirectory = new ModelsOutputDirectory(modelOutputDir, false);

        final int EVERY_N_TH_GOES_TO_TUNING_SET = 14;
        CorpusPreparer corpusPreparer = new CorpusPreparer(foreignFilePath, englishFilePath);
        corpusPreparer.splitCorpusIntoTrainAndTuneParts(EVERY_N_TH_GOES_TO_TUNING_SET);
        ParallerCorpus trainingCorpus = corpusPreparer.getTrainingCorpus();
        ParallerCorpus tuningCorpus = corpusPreparer.getTuningCorpus();

        TextCorpus englishMonolingualCorpus = new TextCorpus(englishOnlyCorpusFilePath);
        englishMonolingualCorpus.tokenize();

        BerkeleyWordAlignmentModel alignmentModel = new BerkeleyWordAlignmentModel(trainingCorpus, modelsOutputDirectory);
        alignmentModel.runWordAlignmentProcess();

        LanguageModel languageModel = new LanguageModel(5, trainingCorpus, englishMonolingualCorpus, modelsOutputDirectory);
        languageModel.buildLanguageModel();

        TranslationModel translationModel = new BerkeleyTranslationModel(alignmentModel, modelsOutputDirectory);
        translationModel.buildTranslationModel();

        TranslationTuner tuner = new TranslationTuner(tuningCorpus, languageModel, translationModel, modelsOutputDirectory);
        tuner.runTuning();
    }

    public static void main(String[] args) throws Exception {
        String foreignFilePath = args[0];
        String englishFilePath = args[1];
        String englishOnlyCorpusFilePath = args[2];
        String foreignPartOfParallerTuningCorpusPath = args[3];
        String englishPartOfParallerTuningCorpusPath = args[4];
        String modelOutputDir = args[5];
        exampleUseCase(foreignFilePath, englishFilePath, englishOnlyCorpusFilePath, foreignPartOfParallerTuningCorpusPath, englishPartOfParallerTuningCorpusPath, modelOutputDir);
    }
}