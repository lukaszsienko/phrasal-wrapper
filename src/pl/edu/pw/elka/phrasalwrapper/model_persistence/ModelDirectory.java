package pl.edu.pw.elka.phrasalwrapper.model_persistence;

public enum ModelDirectory {

    GIZA_WORD_ALIGNMENT("giza_word_aligner_output"),
    BERKELEY_WORD_ALIGNMENT("berkeley_word_aligner_output"),
    LANGUAGE_MODEL("language_model"),
    TRANSLATION_MODEL("translation_model"),
    TUNER_MODEL("tuner_output");

    private String modelDirectoryName;

    ModelDirectory(String modelDirectoryName) {
        this.modelDirectoryName = modelDirectoryName;
    }

    public String getModelDirectoryName() {
        return modelDirectoryName;
    }

    public static String generateCanonicalPathToWholeModelDirectory(ModelsPersistence modelsPersistence, ModelDirectory modelDirectory) {
        return modelsPersistence.getCanonicalPathToModelsDir() + "/" + modelDirectory.getModelDirectoryName();
    }
}
