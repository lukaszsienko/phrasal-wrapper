package pl.edu.pw.elka.phrasalwrapper;

import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelDirectory;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelFile;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class LanguageModel {

    private String ngram;
    private String baseModelDataFilePath;
    private String additionalModelDataFilePath;
    private String outputFolder;
    private ModelsPersistence modelsPersistence;

    public LanguageModel(int ngram, ParallelCorpus parallelCorpus, TextCorpus additionalEnglishText, ModelsPersistence modelsPersistence) {
        this(ngram, parallelCorpus, modelsPersistence);
        this.additionalModelDataFilePath = additionalEnglishText.getCorpusFilePath();
    }

    public LanguageModel(int ngram, ParallelCorpus parallelCorpus, ModelsPersistence modelsPersistence) {
        this.ngram = String.valueOf(ngram);
        this.baseModelDataFilePath = parallelCorpus.getEnglishFilePath();
        this.additionalModelDataFilePath = "";
        this.outputFolder = ModelDirectory.generateCanonicalPathToWholeModelDirectory(modelsPersistence, ModelDirectory.LANGUAGE_MODEL);
        this.modelsPersistence = modelsPersistence;
    }

    public void buildLanguageModel() throws Exception {
        Utilities.printMessage("Started building language model...");
        Utilities.createDirectoryRemovingOldIfExists(this.outputFolder);

        File kenLMextractedLibrary = Utilities.extractAndLoadKenLMLibrary(modelsPersistence);

        File lmplzExecutable = new File(kenLMextractedLibrary.getCanonicalPath()+"/lmplz");
        File buildBinaryExecutable = new File(kenLMextractedLibrary.getCanonicalPath()+"/build_binary");

        lmplzExecutable.setExecutable(true);
        buildBinaryExecutable.setExecutable(true);

        String langModelInputDataFilePath;
        if (additionalModelDataFilePath.isEmpty()) {
            langModelInputDataFilePath = baseModelDataFilePath;
        } else {
            String conbinedDataFilePath = lmplzExecutable.toPath().resolveSibling("model_input").toFile().getCanonicalPath();
            String combine2FilesCommand = "cat "+baseModelDataFilePath+" "+additionalModelDataFilePath+" > "+conbinedDataFilePath;

            String[] combine_files_cmd = {"/bin/sh","-c", combine2FilesCommand};
            ProcessBuilder combine_files_proc_builder = new ProcessBuilder(Arrays.asList(combine_files_cmd));
            combine_files_proc_builder.inheritIO();
            Process combineInputData = combine_files_proc_builder.start();
            combineInputData.waitFor();
            if (combineInputData.exitValue() != 0) {
                throw new Exception("Language model building exception, combine input data process did not return 0.");
            }

            langModelInputDataFilePath = conbinedDataFilePath;
        }

        String outputArpaModelPath = ModelFile.generateCanonicalPathToOneModelFile(modelsPersistence, ModelFile.LANG_MODEL_ARPA);
        String outputBinModelPath = ModelFile.generateCanonicalPathToOneModelFile(modelsPersistence, ModelFile.LANG_MODEL_BIN);

        String buildArpaModelCommand = lmplzExecutable.getCanonicalPath()+" -o " + ngram + " < " + langModelInputDataFilePath + " > "+outputArpaModelPath;
        String buildBinModelCommand = buildBinaryExecutable.getCanonicalPath()+" trie "+outputArpaModelPath+" "+outputBinModelPath;

        String[] build_arpa_model_cmd = {"/bin/sh","-c", buildArpaModelCommand};
        ProcessBuilder pbArpaModel = new ProcessBuilder(Arrays.asList(build_arpa_model_cmd));
        pbArpaModel.inheritIO();
        Process buildTextModel = pbArpaModel.start();
        buildTextModel.waitFor();
        if (buildTextModel.exitValue() != 0) {
            throw new Exception("Language model building exception, build command did not return 0.");
        }

        if (!additionalModelDataFilePath.isEmpty()) {
            Files.delete(Paths.get(langModelInputDataFilePath));
        }

        String[] build_bin_model_cmd = {"/bin/sh","-c", buildBinModelCommand};
        ProcessBuilder pbBinModel = new ProcessBuilder(Arrays.asList(build_bin_model_cmd));
        pbBinModel.inheritIO();
        Process buildBinaryModel = pbBinModel.start();
        buildBinaryModel.waitFor();
        if (buildBinaryModel.exitValue() != 0) {
            throw new Exception("Language model building exception, transfer command did not return 0.");
        }

        modelsPersistence.registerNewDetectedModelFile(ModelFile.LANG_MODEL_ARPA, outputArpaModelPath);
        modelsPersistence.registerNewDetectedModelFile(ModelFile.LANG_MODEL_BIN, outputBinModelPath);
        Utilities.printMessage("Finished building language model.");
    }
}
