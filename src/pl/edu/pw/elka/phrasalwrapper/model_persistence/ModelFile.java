package pl.edu.pw.elka.phrasalwrapper.model_persistence;

public enum ModelFile {

    GIZA_FOR_TO_ENG_ALIGNMENT(ModelDirectory.GIZA_WORD_ALIGNMENT, "align_for_eng/alignment.A3.final"),
    GIZA_ENG_TO_FOR_ALIGNMENT(ModelDirectory.GIZA_WORD_ALIGNMENT, "align_eng_for/alignment.A3.final"),
    BERKELEY_ALIGNMENT_DIRECTORY(ModelDirectory.BERKELEY_WORD_ALIGNMENT, "", Boolean.TRUE),
    LANG_MODEL_ARPA(ModelDirectory.LANGUAGE_MODEL, "lang_model.arpa"),
    LANG_MODEL_BIN(ModelDirectory.LANGUAGE_MODEL, "lang_model.bin"),
    TUNER_WEIGHTS(ModelDirectory.TUNER_MODEL, "tuning.online.final.binwts"),
    TRANSLATION_PHRASE_TABLE(ModelDirectory.TRANSLATION_MODEL, "phrase-table.gz"),
    TRANSLATION_REORDERING_MODEL(ModelDirectory.TRANSLATION_MODEL, "lo-hier.msd2-bidirectional-fe.gz");

    private ModelDirectory modelDirectory;
    private String fileRelativePathFromModelDirectory;
    private Boolean resourceIsDirectory;

    ModelFile(ModelDirectory modelDirectory, String fileRelativePathFromModelDirectory) {
        this.modelDirectory = modelDirectory;
        this.fileRelativePathFromModelDirectory = fileRelativePathFromModelDirectory;
        this.resourceIsDirectory = false;
    }

    ModelFile(ModelDirectory modelDirectory, String fileRelativePathFromModelDirectory, Boolean resourceIsDirectory) {
        this.modelDirectory = modelDirectory;
        this.fileRelativePathFromModelDirectory = fileRelativePathFromModelDirectory;
        this.resourceIsDirectory = resourceIsDirectory;
    }

    public static String generateCanonicalPathToOneModelFile(ModelsPersistence modelsPersistence, ModelFile modelFile) {
        return modelsPersistence.getCanonicalPathToModelsDir() + "/" + modelFile.getRelativePathFromModelsDirectory();
    }

    private String getRelativePathFromModelsDirectory() {
        return modelDirectory.getModelDirectoryName() + "/" + fileRelativePathFromModelDirectory;
    }

    public Boolean isResourceDirectory() {
        return resourceIsDirectory;
    }
}
