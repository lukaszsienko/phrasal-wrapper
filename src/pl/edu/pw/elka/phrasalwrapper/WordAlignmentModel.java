package pl.edu.pw.elka.phrasalwrapper;

import edu.berkeley.nlp.wordAlignment.Main;

import java.io.File;

/**
 * Created by lsienko on 26.04.18.
 */
public class WordAlignmentModel {

    private String inputFolder;
    private String outputFolder;
    private String englishFileNameSuffix;
    private String foreignFileNameSuffix;

    public WordAlignmentModel(ParallerCorpus parallerCorpus) {
        this.inputFolder = new File(parallerCorpus.getEnglishFilePath()).getParent();
        this.outputFolder = parallerCorpus.getPathToModelsFolder();
        this.englishFileNameSuffix = parallerCorpus.getEnglishFileNameSuffix();
        this.foreignFileNameSuffix = parallerCorpus.getForeignFileNameSuffix();
    }

    public void runWordAlignmentProcess() {
        String [] args = new String[44];
        args[0] = "-Main.forwardModels";
        args[1] = "MODEL1";
        args[2] = "HMM";
        args[3] = "-Main.reverseModels";
        args[4] = "MODEL1";
        args[5] = "HMM";
        args[6] = "-Main.mode";
        args[7] = "JOINT";
        args[8] = "JOINT";
        args[9] = "-Main.iters";
        args[10] = "2";
        args[11] = "2";
        args[12] = "-exec.execDir";
        args[13] = outputFolder;
        args[14] = "-exec.create";
        args[15] = "true";
        args[16] = "-Main.saveParams";
        args[17] = "true";
        args[18] = "-EMWordAligner.numThreads";
        args[19] = "1";
        args[20] = "-log.msPerLine";
        args[21] = "10000";
        args[22] = "-Main.alignTraining";
        args[23] = "true";
        args[24] = "-Data.foreignSuffix";
        args[25] = foreignFileNameSuffix;
        args[26] = "-Data.englishSuffix";
        args[27] = englishFileNameSuffix;
        args[28] = "-Data.lowercaseWords";
        args[29] = "true";
        args[30] = "-Data.trainSources";
        args[31] = inputFolder;
        args[32] = "-Data.sentences";
        args[33] = "MAX";
        args[34] = "-Data.testSources";
        args[35] = outputFolder;
        args[36] = "-Data.maxTestSentences";
        args[37] = "MAX";
        args[38] = "-Data.offsetTestSentences";
        args[39] = "0";
        args[40] = "-Main.competitiveThresholding";
        args[41] = "true";
        args[42] = "-exec.overwriteExecDir";
        args[43] = "true";

        Main.main(args);
    }

    public static void printAllOptions() {
        String [] args = new String[] {"-help"};
        Main.main(args);
    }

    public String getOutputFolder() {
        return outputFolder;
    }
}
