package pl.edu.pw.elka.phrasalwrapper.word_alignment;

import edu.berkeley.nlp.wordAlignment.Main;
import org.apache.commons.io.FileUtils;
import pl.edu.pw.elka.phrasalwrapper.ModelsOutputDirectory;
import pl.edu.pw.elka.phrasalwrapper.ParallerCorpus;
import pl.edu.pw.elka.phrasalwrapper.Utilities;

import java.io.File;
import java.io.IOException;

public class BerkeleyWordAlignmentModel {

    private String inputFolderPath;
    private String outputFolderPath;
    private String englishFileNameSuffix;
    private String foreignFileNameSuffix;

    public BerkeleyWordAlignmentModel(ParallerCorpus parallerCorpus, ModelsOutputDirectory modelsOutputDirectory) throws IOException {
        this.inputFolderPath = new File(parallerCorpus.getEnglishFilePath()).getParentFile().getCanonicalPath();
        this.outputFolderPath = modelsOutputDirectory.getCanonicalPathToOutputDir()+"/aligner_output";
        this.englishFileNameSuffix = parallerCorpus.getEnglishFilenameExtension();
        this.foreignFileNameSuffix = parallerCorpus.getForeignFilenameExtension();
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

        String [] defaultArgs = new String[46];
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
        defaultArgs[19] = "5";
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
        defaultArgs[44] = "-Evaluator.writeGIZA";
        defaultArgs[45] = "true";

        Utilities.forbidSystemExitCall();

        try {
            if (userArgs != null) {
                Main.main(userArgs);
            } else {
                Main.main(defaultArgs);
            }
        } catch (Utilities.ExitTrappedException exp) {
            // Do nothing, just continue.
        }

        Utilities.enableSystemExitCall();
    }

    public static void printAllOptions() {
        Utilities.forbidSystemExitCall();

        try {
            String [] args = new String[] {"-help"};
            Main.main(args);
        } catch (Utilities.ExitTrappedException exp) {
            // Do nothing, just continue.
        }

        Utilities.enableSystemExitCall();
    }

    public String getOutputFolderPath() {
        return outputFolderPath;
    }
}
