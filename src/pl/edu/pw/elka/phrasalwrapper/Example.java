package pl.edu.pw.elka.phrasalwrapper;

import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;
import pl.edu.pw.elka.phrasalwrapper.translation_model.BerkeleyTranslationModel;
import pl.edu.pw.elka.phrasalwrapper.translation_model.GizaTranslationModel;
import pl.edu.pw.elka.phrasalwrapper.translation_model.TranslationModel;
import pl.edu.pw.elka.phrasalwrapper.word_alignment.BerkeleyWordAlignmentModel;
import pl.edu.pw.elka.phrasalwrapper.word_alignment.GizaWordAlignmentModel;

public class Example {

    public static void exampleUseCase(String foreignFilePath, String englishFilePath, String englishOnlyCorpusFilePath, String modelOutputDirPath, String modelName) throws Exception {
        ModelsPersistence modelsPersistence =  ModelsPersistence.createEmptyModelsDirectory(modelOutputDirPath, modelName);

        final int EVERY_N_TH_GOES_TO_TUNING_SET = 14;
        CorpusPreparer corpusPreparer = new CorpusPreparer(foreignFilePath, englishFilePath);
        corpusPreparer.splitCorpusIntoTrainAndTuneParts(EVERY_N_TH_GOES_TO_TUNING_SET);
        ParallelCorpus trainingCorpus = corpusPreparer.getTrainingCorpus();
        ParallelCorpus tuningCorpus = corpusPreparer.getTuningCorpus();

        TextCorpus englishMonolingualCorpus = new TextCorpus(englishOnlyCorpusFilePath);
        englishMonolingualCorpus.tokenize();

        LanguageModel languageModel = new LanguageModel(5, trainingCorpus, englishMonolingualCorpus, modelsPersistence);
        languageModel.buildLanguageModel();

        //Berkeley word alignment model
        BerkeleyWordAlignmentModel alignmentModel = new BerkeleyWordAlignmentModel(trainingCorpus, modelsPersistence);
        alignmentModel.runWordAlignmentProcess();
        //Building translation model using Berkeley
        TranslationModel translationModelBerkeley = new BerkeleyTranslationModel(alignmentModel, modelsPersistence);
        translationModelBerkeley.buildTranslationModel();

        //GIZA++ word alignment model
        GizaWordAlignmentModel gizaAlignmentModel = new GizaWordAlignmentModel(trainingCorpus, modelsPersistence);
        alignmentModel.runWordAlignmentProcess();
        //Building translation model using GIZA++
        TranslationModel translationModelGiza = new GizaTranslationModel(gizaAlignmentModel, trainingCorpus, modelsPersistence);
        translationModelGiza.buildTranslationModel();
        //Last built translation model is registered in model persistence and will be used for decoding

        TranslationTuner tuner = new TranslationTuner(tuningCorpus, modelsPersistence);
        tuner.runTuning();

        Decoder decoder = new Decoder(modelsPersistence);
        decoder.translateSentence("Ann has a cat");
    }

    public static void main(String[] args) throws Exception {
        String foreignFilePath = args[0];
        String englishFilePath = args[1];
        String englishOnlyCorpusFilePath = args[2];
        String modelOutputDirPath = args[3];
        String modelName = args[4];
        exampleUseCase(foreignFilePath, englishFilePath, englishOnlyCorpusFilePath, modelOutputDirPath, modelName);
    }
}