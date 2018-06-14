package pl.edu.pw.elka.phrasalwrapper.translation_model;

import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;
import pl.edu.pw.elka.phrasalwrapper.Utilities;
import pl.edu.pw.elka.phrasalwrapper.word_alignment.BerkeleyWordAlignmentModel;

public class BerkeleyTranslationModel extends TranslationModel {

    private BerkeleyWordAlignmentModel alignmentModel;

    public BerkeleyTranslationModel(BerkeleyWordAlignmentModel alignmentModel, ModelsPersistence modelsPersistence) {
        super(modelsPersistence);
        this.alignmentModel = alignmentModel;
    }

    @Override
    protected String[] getPhraseExtractParameters(String outputPath) {
        String[] phrase_extract_args =  super.getPhraseExtractParameters(outputPath);

        String[] berkeley_args = new String[2];
        berkeley_args[0] = "-inputDir";
        berkeley_args[1] = alignmentModel.getOutputFolderPath();

        String[] allArgs = Utilities.concatenateTables(berkeley_args, phrase_extract_args);

        return allArgs;
    }
}
