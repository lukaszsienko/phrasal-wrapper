package pl.edu.pw.elka.phrasalwrapper.translation_model;

import edu.stanford.nlp.mt.train.PhraseExtract;
import pl.edu.pw.elka.phrasalwrapper.Utilities;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelDirectory;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelFile;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;

import java.io.File;
import java.io.IOException;


public abstract class TranslationModel {

    protected ModelsPersistence modelsPersistence;
    private String outputModelFolder;

    public TranslationModel(ModelsPersistence modelsPersistence) {
        this.modelsPersistence = modelsPersistence;
        this.outputModelFolder = ModelDirectory.generateCanonicalPathToWholeModelDirectory(modelsPersistence, ModelDirectory.TRANSLATION_MODEL);
    }

    public void buildTranslationModel() throws IOException {
        Utilities.printMessage("Started building translation model (phrase table and reordering model)...");
        File outputDirectory = Utilities.createDirectoryRemovingOldIfExisits(this.outputModelFolder);

        String[] phrase_extract_args = getPhraseExtractParameters(outputDirectory.getCanonicalPath());

        System.setProperty("ShowPhraseRestriction", "true");

        try {
            PhraseExtract.main(phrase_extract_args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String phraseTableFilePath = ModelFile.generateCanonicalPathToOneModelFile(modelsPersistence, ModelFile.TRANSLATION_PHRASE_TABLE);
        String reorderingModelFilePath = ModelFile.generateCanonicalPathToOneModelFile(modelsPersistence, ModelFile.TRANSLATION_REORDERING_MODEL);
        modelsPersistence.registerNewDetectedModelFile(ModelFile.TRANSLATION_PHRASE_TABLE, phraseTableFilePath);
        modelsPersistence.registerNewDetectedModelFile(ModelFile.TRANSLATION_REORDERING_MODEL, reorderingModelFilePath);
        Utilities.printMessage("Finished building translation model.");
    }

    protected String[] getPhraseExtractParameters(String outputPath) {
        String[] phrase_extract_args = new String[26];
        phrase_extract_args[0] = "-threads";
        phrase_extract_args[1] = "1";
        phrase_extract_args[2] = "-outputDir";
        phrase_extract_args[3] = outputPath;
        phrase_extract_args[4] = "-extractors";
        phrase_extract_args[5] = "edu.stanford.nlp.mt.train.MosesPharoahFeatureExtractor=phrase-table.gz:edu.stanford.nlp.mt.train.CountFeatureExtractor=phrase-table.gz:edu.stanford.nlp.mt.train.LexicalReorderingFeatureExtractor=lo-hier.msd2-bidirectional-fe.gz";
        phrase_extract_args[6] = "-hierarchicalOrientationModel";
        phrase_extract_args[7] = "true";
        phrase_extract_args[8] = "-orientationModelType";
        phrase_extract_args[9] = "msd2-bidirectional-fe";
        phrase_extract_args[10] = "-symmetrization";
        phrase_extract_args[11] = "grow-diag";
        phrase_extract_args[12] = "-phiFilter";
        phrase_extract_args[13] = "1e-3";
        phrase_extract_args[14] = "-maxELen";
        phrase_extract_args[15] = "4";
        phrase_extract_args[16] = "-maxLen";
        phrase_extract_args[17] = "4";
        phrase_extract_args[18] = "-maxLenE";
        phrase_extract_args[19] = "4";
        phrase_extract_args[20] = "-maxLenF";
        phrase_extract_args[21] = "4";
        phrase_extract_args[22] = "-maxELenE";
        phrase_extract_args[23] = "4";
        phrase_extract_args[24] = "-maxELenF";
        phrase_extract_args[25] = "4";

        return phrase_extract_args;
    }
}
