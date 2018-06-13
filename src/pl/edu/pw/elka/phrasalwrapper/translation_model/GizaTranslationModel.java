package pl.edu.pw.elka.phrasalwrapper.translation_model;

import pl.edu.pw.elka.phrasalwrapper.ModelsOutputDirectory;
import pl.edu.pw.elka.phrasalwrapper.ParallerCorpus;
import pl.edu.pw.elka.phrasalwrapper.Utilities;
import pl.edu.pw.elka.phrasalwrapper.word_alignment.GizaWordAlignmentModel;

import java.io.IOException;

public class GizaTranslationModel extends TranslationModel {

    private GizaWordAlignmentModel alignmentModel;
    private ParallerCorpus corpus;

    public GizaTranslationModel(GizaWordAlignmentModel alignmentModel, ParallerCorpus corpus, ModelsOutputDirectory modelsOutputDirectory) {
        super(modelsOutputDirectory);
        this.alignmentModel = alignmentModel;
        this.corpus = corpus;
    }

    @Override
    protected String[] getPhraseExtractParameters() throws IOException {
        String[] phrase_extract_args =  super.getPhraseExtractParameters();

        String[] giza_args = new String[8];
        giza_args[0] = "-fCorpus";
        giza_args[1] = corpus.getForeignFilePath();
        giza_args[2] = "-eCorpus";
        giza_args[3] = corpus.getEnglishFilePath();
        giza_args[4] = "-feAlign";
        //giza_args[5] = pathToModelFolder+"/word_align/wynik_pl_en/wyrownanie.A3.final";
        giza_args[5] = alignmentModel.getForEngWordALignmentFilePath();
        giza_args[6] = "-efAlign";
        //giza_args[7] = pathToModelFolder+"/word_align/wynik_en_pl/wyrownanie.A3.final";
        giza_args[7] = alignmentModel.getEngForWordALignmentFilePath();

        String[] allArgs = Utilities.concatenateTables(giza_args, phrase_extract_args);

        return allArgs;
    }
}
