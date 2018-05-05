package pl.edu.pw.elka.phrasalwrapper;

import edu.berkeley.nlp.wordAlignment.Main;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by lsienko on 26.04.18.
 */
public class WordAlignmentModel {

    private String inputFolderPath;
    private String outputFolderPath;
    private String englishFileNameSuffix;
    private String foreignFileNameSuffix;

    public WordAlignmentModel(ParallerCorpus parallerCorpus) throws IOException {
        this.inputFolderPath = new File(parallerCorpus.getEnglishFilePath()).getParentFile().getCanonicalPath();
        this.outputFolderPath = parallerCorpus.getPathToModelsFolder()+"/aligner_output";
        this.englishFileNameSuffix = parallerCorpus.getEnglishFileNameSuffix();
        this.foreignFileNameSuffix = parallerCorpus.getForeignFileNameSuffix();
    }

    public void runWordAlignmentProcess() throws IOException {
        this.runWordAlignmentProcess(null);
    }

    public void runWordAlignmentProcess(String [] userArgs) throws IOException {
        File outputDirectory = new File(this.outputFolderPath);
        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
        outputDirectory.mkdir();

        String [] defaultArgs = new String[44];
        defaultArgs[0] = "-Main.forwardModels";
        defaultArgs[1] = "MODEL1";
        defaultArgs[2] = "HMM";
        defaultArgs[3] = "-Main.reverseModels";
        defaultArgs[4] = "MODEL1";
        defaultArgs[5] = "HMM";
        defaultArgs[6] = "-Main.mode";
        defaultArgs[7] = "JOINT";
        defaultArgs[8] = "JOINT";
        defaultArgs[9] = "-Main.iters";
        defaultArgs[10] = "2";
        defaultArgs[11] = "2";
        defaultArgs[12] = "-exec.execDir";
        defaultArgs[13] = outputDirectory.getCanonicalPath();
        defaultArgs[14] = "-exec.create";
        defaultArgs[15] = "true";
        defaultArgs[16] = "-Main.saveParams";
        defaultArgs[17] = "true";
        defaultArgs[18] = "-EMWordAligner.numThreads";
        defaultArgs[19] = "1";
        defaultArgs[20] = "-log.msPerLine";
        defaultArgs[21] = "10000";
        defaultArgs[22] = "-Main.alignTraining";
        defaultArgs[23] = "true";
        defaultArgs[24] = "-Data.foreignSuffix";
        defaultArgs[25] = foreignFileNameSuffix;
        defaultArgs[26] = "-Data.englishSuffix";
        defaultArgs[27] = englishFileNameSuffix;
        defaultArgs[28] = "-Data.lowercaseWords";
        defaultArgs[29] = "true";
        defaultArgs[30] = "-Data.trainSources";
        defaultArgs[31] = inputFolderPath;
        defaultArgs[32] = "-Data.sentences";
        defaultArgs[33] = "MAX";
        defaultArgs[34] = "-Data.testSources";
        defaultArgs[35] = outputFolderPath;
        defaultArgs[36] = "-Data.maxTestSentences";
        defaultArgs[37] = "MAX";
        defaultArgs[38] = "-Data.offsetTestSentences";
        defaultArgs[39] = "0";
        defaultArgs[40] = "-Main.competitiveThresholding";
        defaultArgs[41] = "true";
        defaultArgs[42] = "-exec.overwriteExecDir";
        defaultArgs[43] = "true";

        if (userArgs != null) {
            Main.main(userArgs);
        } else {
            Main.main(defaultArgs);
        }
    }

    public static void printAllOptions() {
        String [] args = new String[] {"-help"};
        Main.main(args);
    }

    public String getOutputFolderPath() {
        return outputFolderPath;
    }
}
