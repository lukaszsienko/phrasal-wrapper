package pl.edu.pw.elka.phrasalwrapper;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Created by lsienko on 26.04.18.
 */
public class LanguageModel {

    private String ngram;
    private String englishSideOfCorpusFilePath;
    private String outputFolder;
    private String modelFileName;

    public LanguageModel(int ngram, ParallerCorpus parallerCorpus) {
        this.ngram = String.valueOf(ngram);
        this.englishSideOfCorpusFilePath = parallerCorpus.getEnglishFilePath();
        this.outputFolder = parallerCorpus.getPathToFolder();
        this.modelFileName = this.ngram + "gm";
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
            String buildCommand = dest.getAbsolutePath()+"/kenlm/bin/lmplz -o " + ngram + " < " + englishSideOfCorpusFilePath + " > "+textModelPath;
            Process buildTextModel = runtime.exec(buildCommand);
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
