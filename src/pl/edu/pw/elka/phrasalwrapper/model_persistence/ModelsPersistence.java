package pl.edu.pw.elka.phrasalwrapper.model_persistence;

import pl.edu.pw.elka.phrasalwrapper.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelsPersistence {

    public static ModelsPersistence createEmptyModelsDirectory(String pathToModelsDirectoryParent, String modelsDirectoryName) throws IOException {
        File parentDirectory = new File(pathToModelsDirectoryParent);
        if (!parentDirectory.exists() || !parentDirectory.isDirectory()) {
            throw new IOException("Cannot create empty model directory. Specified parent path is wrong or leads to a file instead of a directory.");
        }

        File modelsDirectory = new File(parentDirectory, modelsDirectoryName);
        boolean dirCreated = modelsDirectory.mkdir();
        if (!dirCreated) {
            throw new IOException("Cannot create new directory.\nDir name: "+modelsDirectoryName+"\nDir location: "+parentDirectory.getCanonicalPath());
        }

        ModelsPersistence modelsPersistence = new ModelsPersistence(modelsDirectory.getCanonicalPath());

        Utilities.printMessage("Created new empty model directory.");
        return modelsPersistence;
    }

    public static ModelsPersistence loadModels(String pathToPersistedModelsDirectory) throws IOException {
        File modelsDirectory = new File(pathToPersistedModelsDirectory);
        if (!modelsDirectory.exists() || !modelsDirectory.isDirectory()) {
            throw new IOException("Cannot open model directory. Specified directory does not exist or path leads to a file.");
        }

        ModelsPersistence modelsPersistence = new ModelsPersistence(modelsDirectory.getCanonicalPath());
        modelsPersistence.detectExistingModelFiles();
        if (modelsPersistence.checkNoModelFilesWereDetected()) {
            throw new IOException("Cannot detect any model in specified model directory.");
        }

        Utilities.printMessage("Model loaded from: "+pathToPersistedModelsDirectory);
        return modelsPersistence;
    }

    private String canonicalPathToModelsDir;
    private Map<ModelFile, String> canonicalPathsToDetectedModelFiles;

    private ModelsPersistence(String canonicalPathToModelsDir) {
        this.canonicalPathToModelsDir = canonicalPathToModelsDir;
        this.canonicalPathsToDetectedModelFiles = new LinkedHashMap<>();
    }

    public String getCanonicalPathToModelsDir() {
        return canonicalPathToModelsDir;
    }

    public void registerNewDetectedModelFile(ModelFile modelFile, String canonicalPath) {
        canonicalPathsToDetectedModelFiles.put(modelFile, canonicalPath);
    }

    public String getDetectedModelFilePath(ModelFile modelFile) throws IOException {
        String path = canonicalPathsToDetectedModelFiles.get(modelFile);
        if (path == null || path.isEmpty()) {
            throw new IOException("Specified model file wasn't detected. File: " + ModelFile.generateCanonicalPathToOneModelFile(this, modelFile));
        }
        return path;
    }

    private void detectExistingModelFiles() {
        List<ModelFile> allModelFiles = Arrays.asList(ModelFile.values());
        for (ModelFile modelFile: allModelFiles) {
            String pathToFile = ModelFile.generateCanonicalPathToOneModelFile(this, modelFile);
            File file = new File(pathToFile);
            if (file.exists() && modelFile.isResourceDirectory().equals(file.isDirectory())) {
                registerNewDetectedModelFile(modelFile, pathToFile);
            }
        }
    }

    private boolean checkNoModelFilesWereDetected() {
        return canonicalPathsToDetectedModelFiles.isEmpty();
    }
}
