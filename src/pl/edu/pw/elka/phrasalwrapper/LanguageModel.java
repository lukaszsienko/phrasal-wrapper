package pl.edu.pw.elka.phrasalwrapper;

import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelDirectory;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelFile;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;

import java.io.File;

public class LanguageModel {

    private String ngram;
    private String pathsToFilesWithModelData;
    private String outputFolder;
    private ModelsPersistence modelsPersistence;

    public LanguageModel(int ngram, ParallelCorpus parallelCorpus, TextCorpus additionalEnglishText, ModelsPersistence modelsPersistence) {
        this(ngram, parallelCorpus, modelsPersistence);
        this.pathsToFilesWithModelData = this.pathsToFilesWithModelData + " " + additionalEnglishText.getCorpusFilePath();
    }

    public LanguageModel(int ngram, ParallelCorpus parallelCorpus, ModelsPersistence modelsPersistence) {
        this.ngram = String.valueOf(ngram);
        this.pathsToFilesWithModelData = parallelCorpus.getEnglishFilePath();
        this.outputFolder = ModelDirectory.generateCanonicalPathToWholeModelDirectory(modelsPersistence, ModelDirectory.LANGUAGE_MODEL);
        this.modelsPersistence = modelsPersistence;
    }

    public void buildLanguageModel() throws Exception {
        File outputDirectory = Utilities.createDirectoryRemovingOldIfExisits(this.outputFolder);

        File kenLMextractedLibrary = Utilities.extractAndLoadKenLMLibrary(modelsPersistence);

        File lmplzExecutable = new File(kenLMextractedLibrary.getCanonicalPath()+"/lmplz");
        File buildBinaryExecutable = new File(kenLMextractedLibrary.getCanonicalPath()+"/build_binary");

        lmplzExecutable.setExecutable(true);
        buildBinaryExecutable.setExecutable(true);

        String outputArpaModelPath = ModelFile.generateCanonicalPathToOneModelFile(modelsPersistence, ModelFile.LANG_MODEL_ARPA);
        String outputBinModelPath = ModelFile.generateCanonicalPathToOneModelFile(modelsPersistence, ModelFile.LANG_MODEL_BIN);

        String buildCommand = lmplzExecutable.getCanonicalPath()+" -o " + ngram + " < " + pathsToFilesWithModelData + " > "+outputArpaModelPath;
        String transferCommand = buildBinaryExecutable.getCanonicalPath()+" trie "+outputArpaModelPath+" "+outputBinModelPath;

        Runtime runtime = Runtime.getRuntime();

        String[] build_cmd = {"/bin/sh","-c", buildCommand};
        Process buildTextModel = runtime.exec(build_cmd);
        buildTextModel.waitFor();
        if (buildTextModel.exitValue() != 0) {
            Utilities.printBashProcessOutput(buildTextModel);
            throw new Exception("Language model building exception, build command did not return 0.");
        }

        Process transferToBinaryModel = runtime.exec(transferCommand);
        transferToBinaryModel.waitFor();
        if (transferToBinaryModel.exitValue() != 0) {
            Utilities.printBashProcessOutput(transferToBinaryModel);
            throw new Exception("Language model building exception, transfer command did not return 0.");
        }

        modelsPersistence.registerNewDetectedModelFile(ModelFile.LANG_MODEL_ARPA, outputArpaModelPath);
        modelsPersistence.registerNewDetectedModelFile(ModelFile.LANG_MODEL_BIN, outputBinModelPath);
    }
}
