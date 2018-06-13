package pl.edu.pw.elka.phrasalwrapper;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import pl.edu.pw.elka.phrasalwrapper.translation_model.TranslationModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ModelsOutputDirectory {

    private String canonicalPathToOutputDir;
    private File kenLMextractedLibrary;

    public ModelsOutputDirectory(String pathToModels, boolean modelDirExists) throws Exception {
        if (modelDirExists) {
            loadExisitingModelsDirectory(pathToModels);
        } else {
            createModelsDirectory(pathToModels);
        }
        kenLMextractedLibrary = extractAndLoadKenLMLibrary();
    }

    public String getCanonicalPathToOutputDir() {
        return canonicalPathToOutputDir;
    }

    public File getKenLMextractedLibrary() {
        return kenLMextractedLibrary;
    }

    public void reloadKenLMexecutables() throws Exception {
        extractAndLoadKenLMLibrary();
    }

    private void loadExisitingModelsDirectory(String path) throws IOException {
        File outputFolder = new File(path);
        if (!outputFolder.exists()) {
            throw new FileNotFoundException("Cannot find specified file: "+path);
        }

        if (directoryContainsModelsContent(outputFolder)) {
            canonicalPathToOutputDir = outputFolder.getCanonicalPath();
        } else {
            String modelsFolderPath = outputFolder.getCanonicalPath() + "/models";
            File modelsFolder = new File(modelsFolderPath);
            if (modelsFolder.exists() && directoryContainsModelsContent(modelsFolder)) {
                canonicalPathToOutputDir = modelsFolder.getCanonicalPath();
            } else {
                throw new FileNotFoundException("Cannot find model directory here: "+outputFolder.getCanonicalPath()+" and here: "+modelsFolder.getCanonicalPath());
            }
        }
    }

    private boolean directoryContainsModelsContent(File dir) throws IOException {
        try (Stream<Path> directoryContents = Files.walk(dir.toPath())) {
            boolean translationModelPresent = directoryContents.filter(path -> path.toFile().getName().equals(TranslationModel.MODEL_DIR_NAME))
                    .findAny().isPresent();
            return translationModelPresent;
        }
    }

    private void createModelsDirectory(String path) throws IOException {
        File outputFolder = new File(path);
        if (!outputFolder.exists()) {
            throw new FileNotFoundException("Cannot find specified file: "+path);
        }

        File modelsDir = new File(outputFolder.getCanonicalPath() + "/models");
        if (!modelsDir.exists()) {
            boolean success = modelsDir.mkdir();
            if (success) {
                canonicalPathToOutputDir = modelsDir.getCanonicalPath();
            } else {
                throw new IOException("Cannot create file at specified path: "+modelsDir.getCanonicalPath());
            }
        } else {
            throw new FileExistsException("Cannot create file because file already exists here: "+modelsDir.getCanonicalPath());
        }
    }

    private File extractAndLoadKenLMLibrary() throws Exception {
        File dest = new File(canonicalPathToOutputDir+"/kenLanguageModel");
        if (dest.exists()) {
            FileUtils.deleteDirectory(dest);
        }
        Utilities.extractDirectory("/kenLanguageModel", dest.getParentFile().getCanonicalPath());

        loadLibrary(dest.getCanonicalPath());
        return dest;
    }

    @Deprecated
    private void loadLibrary(String libraryCanonicalPath) throws Exception {
        if (!System.getProperty("java.library.path").contains(libraryCanonicalPath)) {
            System.setProperty("java.library.path", System.getProperty("java.library.path")+":"+libraryCanonicalPath);
            Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
            fieldSysPath.setAccessible( true );
            fieldSysPath.set( null, null );
        }
    }
}
