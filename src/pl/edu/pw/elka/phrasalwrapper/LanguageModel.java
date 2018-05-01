package pl.edu.pw.elka.phrasalwrapper;

import org.apache.commons.io.FileUtils;

import java.io.File;

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
        this.outputFolder = parallerCorpus.getPathToModelsFolder();
    }

    public String getModelBinaryFileName() {
        return modelFileName + ".bin";
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void buildLanguageModel() {
        try {
            File src = new File(getClass().getResource("/kenLanguageModel").getPath());
            File dest = new File(outputFolder+"/kenLanguageModel");
            FileUtils.copyDirectory(src, dest);

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

            FileUtils.deleteDirectory(dest);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
