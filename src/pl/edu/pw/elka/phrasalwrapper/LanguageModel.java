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

    public void buildLanguageModel() {
        try {
            File src = new File(getClass().getResource("/kenLanguageModel").getPath());
            File dest = new File(outputFolder+"/kenLanguageModel");

            FileUtils.copyDirectory(src, dest);

            Runtime runtime = Runtime.getRuntime();

            String textModelFileName = modelFileName+".arpa";
            String textModelPath = outputFolder+"/"+textModelFileName;
            String chmodCommand = "chmod +x "+dest.getAbsolutePath()+"/kenlm/bin/lmplz"+" "+dest.getAbsolutePath()+"/kenlm/bin/build_binary";
            String buildCommand = dest.getAbsolutePath()+"/kenlm/bin/lmplz -o " + ngram + " < " + englishSideOfCorpusFilePath + " > "+textModelPath;

            Process chmodProcess = runtime.exec(chmodCommand);
            chmodProcess.waitFor();
            System.err.println("Exit chmod status=" + chmodProcess.exitValue());

            String[] build_cmd = {"/bin/sh","-c",buildCommand};
            Process buildTextModel = runtime.exec(build_cmd);
            buildTextModel.waitFor();

            System.err.println("Exit build status=" + buildTextModel.exitValue());

            String transferCommand = dest.getAbsolutePath()+"/kenlm/bin/build_binary trie "+outputFolder+" "+getModelBinaryFileName();
            Process transferToBinaryModel = runtime.exec(transferCommand);
            transferToBinaryModel.waitFor();

            System.err.println("Exit transfer status=" + transferToBinaryModel.exitValue());

            FileUtils.deleteDirectory(dest);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public String getOutputFolder() {
        return outputFolder;
    }
}
