package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.train.PhraseExtract;

import java.io.File;

/**
 * Created by lsienko on 26.04.18.
 */
public class TranslationModel {

    private WordAlignmentModel alignmentModel;
    private String path; //TODO calculate

    public TranslationModel(WordAlignmentModel alignmentModel) {
        this.alignmentModel = alignmentModel;
    }

    public String getPath() {
        return path;
    }

    public void buildTranslationModel() {
        File berkeley_aligner_folder = new File(alignmentModel.getOutputFolder());
        String aligner_folder_path = berkeley_aligner_folder.getAbsolutePath();

        File translation_model_folder = new File(path);
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
            PhraseExtract.main(phrase_extract_args);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
