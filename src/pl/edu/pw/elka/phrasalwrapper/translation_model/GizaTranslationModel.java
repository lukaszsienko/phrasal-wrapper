package pl.edu.pw.elka.phrasalwrapper.translation_model;

import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;
import pl.edu.pw.elka.phrasalwrapper.ParallelCorpus;
import pl.edu.pw.elka.phrasalwrapper.Utilities;
import pl.edu.pw.elka.phrasalwrapper.word_alignment.GizaWordAlignmentModel;

public class GizaTranslationModel extends TranslationModel {

    private GizaWordAlignmentModel alignmentModel;
    private ParallelCorpus corpus;

    public GizaTranslationModel(GizaWordAlignmentModel alignmentModel, ParallelCorpus corpus, ModelsPersistence modelsPersistence) {
        super(modelsPersistence);
        this.alignmentModel = alignmentModel;
        this.corpus = corpus;
    }

    @Override
    protected String[] getPhraseExtractParameters(String outputPath) {
        String[] phrase_extract_args =  super.getPhraseExtractParameters(outputPath);

        String[] giza_args = new String[8];
        giza_args[0] = "-fCorpus";
        giza_args[1] = corpus.getForeignFilePath();
        giza_args[2] = "-eCorpus";
        giza_args[3] = corpus.getEnglishFilePath();
        giza_args[4] = "-feAlign";
        giza_args[5] = alignmentModel.getForToEngWordAlignmentFilePath();
        giza_args[6] = "-efAlign";
        giza_args[7] = alignmentModel.getEngToForWordAlignmentFilePath();

        String[] allArgs = Utilities.concatenateTables(giza_args, phrase_extract_args);

        return allArgs;
    }
}
