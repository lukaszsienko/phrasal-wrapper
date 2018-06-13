package pl.edu.pw.elka.phrasalwrapper;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class LanguageModel {

    private String ngram;
    private String englishCorpusFilesPaths;
    private File kenLMextractedLibrary;
    private String modelFileName;
    private String outputFolder;

    public LanguageModel(int ngram, ParallerCorpus parallerCorpus, TextCorpus additionalText, ModelsOutputDirectory modelsOutputDirectory) {
        this(ngram, parallerCorpus, modelsOutputDirectory);
        this.englishCorpusFilesPaths = this.englishCorpusFilesPaths + " " + additionalText.getCorpusFilePath();
    }

    public LanguageModel(int ngram, ParallerCorpus parallerCorpus, ModelsOutputDirectory modelsOutputDirectory) {
        this.ngram = String.valueOf(ngram);
        this.englishCorpusFilesPaths = parallerCorpus.getEnglishFilePath();
        this.modelFileName = this.ngram + "gm";
        this.outputFolder = modelsOutputDirectory.getCanonicalPathToOutputDir()+"/language_model";
        this.kenLMextractedLibrary = modelsOutputDirectory.getKenLMextractedLibrary();
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

            String textModelFileName = modelFileName+".arpa";
            String textModelPath = outputFolder+"/"+textModelFileName;

            File lmplzExecutable = new File(this.kenLMextractedLibrary.getCanonicalPath()+"/kenlm/bin/lmplz");
            File buildBinaryExecutable = new File(this.kenLMextractedLibrary.getCanonicalPath()+"/kenlm/bin/build_binary");

            lmplzExecutable.setExecutable(true);
            buildBinaryExecutable.setExecutable(true);

            String buildCommand = lmplzExecutable.getCanonicalPath()+" -o " + ngram + " < " + englishCorpusFilesPaths + " > "+textModelPath;
            String transferCommand = buildBinaryExecutable.getCanonicalPath()+" trie "+outputFolder+"/"+textModelFileName+" "+outputFolder+"/"+getModelBinaryFileName();

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
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
