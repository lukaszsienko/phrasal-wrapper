package pl.edu.pw.elka.phrasalwrapper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by lsienko on 26.04.18.
 */
public class LanguageModel {

    private String ngram;
    private String englishSideOfCorpusFilePath;
    private String modelFileName;
    private String outputFolder;

    public LanguageModel(int ngram, ParallerCorpus parallerCorpus) {
        this.ngram = String.valueOf(ngram);
        this.englishSideOfCorpusFilePath = parallerCorpus.getEnglishFilePath();
        this.modelFileName = this.ngram + "gm";
        this.outputFolder = parallerCorpus.getPathToModelsFolder()+"/language_model";
    }

    public String getModelBinaryFileName() {
        return modelFileName + ".bin";
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void buildLanguageModel() {
        try {
            File outputDirectory = new File(this.outputFolder);
            if (outputDirectory.exists()) {
                FileUtils.deleteDirectory(outputDirectory);
            }
            outputDirectory.mkdir();

            File dest = extractAndLoadKenLMLibrary();

            String textModelFileName = modelFileName+".arpa";
            String textModelPath = outputFolder+"/"+textModelFileName;

            String chmodCommand = "chmod +x "+dest.getAbsolutePath()+"/kenlm/bin/lmplz"+" "+dest.getAbsolutePath()+"/kenlm/bin/build_binary";
            String buildCommand = dest.getAbsolutePath()+"/kenlm/bin/lmplz -o " + ngram + " < " + englishSideOfCorpusFilePath + " > "+textModelPath;
            String transferCommand = dest.getAbsolutePath()+"/kenlm/bin/build_binary trie "+outputFolder+"/"+textModelFileName+" "+outputFolder+"/"+getModelBinaryFileName();

            Runtime runtime = Runtime.getRuntime();
            Process chmodProcess = runtime.exec(chmodCommand);
            chmodProcess.waitFor();
            if (chmodProcess.exitValue() != 0) {
                throw new Exception("Language model building exception, chmod command did not return 0.");
            }

            String[] build_cmd = {"/bin/sh","-c", buildCommand};
            Process buildTextModel = runtime.exec(build_cmd);
            buildTextModel.waitFor();
            if (buildTextModel.exitValue() != 0) {
                throw new Exception("Language model building exception, build command did not return 0.");
            }

            Process transferToBinaryModel = runtime.exec(transferCommand);
            transferToBinaryModel.waitFor();
            if (transferToBinaryModel.exitValue() != 0) {
                throw new Exception("Language model building exception, transfer command did not return 0.");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public File extractAndLoadKenLMLibrary() throws Exception {
        File dest = new File(outputFolder+"/kenLanguageModel");
        if (dest.exists()) {
            FileUtils.deleteDirectory(dest);
        }
        Utilities.extractDirectory("/kenLanguageModel", dest.getParent());

        loadLibrary(dest.getAbsolutePath());
        return dest;
    }

    @Deprecated
    private void loadLibrary(String libraryAbsolutePath) throws Exception {
        if (!System.getProperty("java.library.path").contains(libraryAbsolutePath)) {
            System.setProperty("java.library.path", System.getProperty("java.library.path")+":"+libraryAbsolutePath);
            Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
            fieldSysPath.setAccessible( true );
            fieldSysPath.set( null, null );
        }
    }
}
