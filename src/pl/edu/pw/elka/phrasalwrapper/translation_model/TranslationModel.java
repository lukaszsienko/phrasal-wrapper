package pl.edu.pw.elka.phrasalwrapper.translation_model;

import edu.stanford.nlp.mt.train.PhraseExtract;
import org.apache.commons.io.FileUtils;
import pl.edu.pw.elka.phrasalwrapper.ModelsOutputDirectory;

import java.io.File;
import java.io.IOException;


public abstract class TranslationModel {
    public static final String MODEL_DIR_NAME = "translation_model";

    private String outputFolder;

    public TranslationModel(ModelsOutputDirectory modelsOutputDirectory) {
        this.outputFolder = modelsOutputDirectory.getCanonicalPathToOutputDir() + "/" + MODEL_DIR_NAME;
    }

    public void buildTranslationModel() throws IOException {
        File outputDirectory = new File(this.outputFolder);
        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
        outputDirectory.mkdir();

        String[] phrase_extract_args = getPhraseExtractParameters();

        System.setProperty("ShowPhraseRestriction", "true");

        try {
            PhraseExtract.main(phrase_extract_args);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("\nCheck if you've built word aligment model by calling WordAlignmentModel.runWordAlignmentProcess() method before.");
        }

    }

    public String getOutputFolder() {
        return outputFolder;
    }


    protected String[] getPhraseExtractParameters() throws IOException {
        File translation_model_folder = new File(outputFolder);
        String translation_model_path = translation_model_folder.getCanonicalPath();

        String[] phrase_extract_args = new String[26];
        phrase_extract_args[0] = "-threads";
        phrase_extract_args[1] = "1";
        phrase_extract_args[2] = "-outputDir";
        phrase_extract_args[3] = translation_model_path;
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
