package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.train.PhraseExtract;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by lsienko on 26.04.18.
 */
public class TranslationModel {

    private WordAlignmentModel alignmentModel;
    private String outputFolder;

    public TranslationModel(WordAlignmentModel alignmentModel, ParallerCorpus corpus) {
        this.alignmentModel = alignmentModel;
        this.outputFolder = corpus.getPathToModelsFolder() + "/translation_model";
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void buildTranslationModel() throws IOException {
        this.buildTranslationModel(null);
    }

    public void buildTranslationModel(String [] userArgs) throws IOException {
        File outputDirectory = new File(this.outputFolder);
        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
        outputDirectory.mkdir();

        File berkeley_aligner_folder = new File(alignmentModel.getOutputFolder());
        String aligner_folder_path = berkeley_aligner_folder.getAbsolutePath();

        File translation_model_folder = new File(outputFolder);
        String translation_model_path = translation_model_folder.getAbsolutePath();

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
            if (userArgs != null) {
                PhraseExtract.main(userArgs);
            } else {
                PhraseExtract.main(phrase_extract_args);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("\nCheck if you've built word aligment model by calling WordAlignmentModel.runWordAlignmentProcess() method before.");
        }

    }
}
