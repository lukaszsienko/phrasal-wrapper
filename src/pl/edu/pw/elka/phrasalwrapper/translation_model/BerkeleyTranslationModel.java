package pl.edu.pw.elka.phrasalwrapper.translation_model;

import pl.edu.pw.elka.phrasalwrapper.ModelsOutputDirectory;
import pl.edu.pw.elka.phrasalwrapper.Utilities;
import pl.edu.pw.elka.phrasalwrapper.word_alignment.BerkeleyWordAlignmentModel;

import java.io.File;
import java.io.IOException;

public class BerkeleyTranslationModel extends TranslationModel {

    private BerkeleyWordAlignmentModel alignmentModel;

    public BerkeleyTranslationModel(BerkeleyWordAlignmentModel alignmentModel, ModelsOutputDirectory modelsOutputDirectory) {
        super(modelsOutputDirectory);
        this.alignmentModel = alignmentModel;
    }

    @Override
    protected String[] getPhraseExtractParameters() throws IOException {
        String[] phrase_extract_args =  super.getPhraseExtractParameters();

        File berkeley_aligner_folder = new File(alignmentModel.getOutputFolderPath());
        String aligner_folder_path = berkeley_aligner_folder.getCanonicalPath();

        String[] berkeley_args = new String[2];
        berkeley_args[0] = "-inputDir";
        berkeley_args[1] = aligner_folder_path;

        String[] allArgs = Utilities.concatenateTables(berkeley_args, phrase_extract_args);

        return allArgs;
    }
}
