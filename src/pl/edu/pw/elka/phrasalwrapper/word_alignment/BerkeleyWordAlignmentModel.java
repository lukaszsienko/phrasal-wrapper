package pl.edu.pw.elka.phrasalwrapper.word_alignment;

import edu.berkeley.nlp.wordAlignment.Main;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelDirectory;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelFile;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;
import pl.edu.pw.elka.phrasalwrapper.ParallelCorpus;
import pl.edu.pw.elka.phrasalwrapper.Utilities;

import java.io.File;
import java.io.IOException;

public class BerkeleyWordAlignmentModel {

    private String inputFolderPath;
    private String outputFolderPath;
    private String englishFileNameSuffix;
    private String foreignFileNameSuffix;
    private ModelsPersistence modelsPersistence;

    public BerkeleyWordAlignmentModel(ParallelCorpus parallelCorpus, ModelsPersistence modelsPersistence) throws IOException {
        this.inputFolderPath = new File(parallelCorpus.getEnglishFilePath()).getParentFile().getCanonicalPath();
        this.outputFolderPath = ModelDirectory.generateCanonicalPathToWholeModelDirectory(modelsPersistence, ModelDirectory.BERKELEY_WORD_ALIGNMENT);
        this.englishFileNameSuffix = parallelCorpus.getEnglishFilenameExtension();
        this.foreignFileNameSuffix = parallelCorpus.getForeignFilenameExtension();
        this.modelsPersistence = modelsPersistence;
    }

    public void runWordAlignmentProcess() throws IOException {
        this.runWordAlignmentProcess(null);
    }

    public void runWordAlignmentProcess(String [] userArgs) throws IOException {
        Utilities.printMessage("Started word alignment using Berkeley Aligner...");
        File outputDirectory = Utilities.createDirectoryRemovingOldIfExisits(this.outputFolderPath);

        String [] defaultArgs = new String[48];
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
        defaultArgs[44] = "-Evaluator.writeGIZA";
        defaultArgs[45] = "true";
        defaultArgs[46] = "-Data.maxTrainingLength";
        defaultArgs[47] = String.valueOf(255+1);//Max nr of words is 255 (CorpusPreparer); added +1 just in case

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

        modelsPersistence.registerNewDetectedModelFile(ModelFile.BERKELEY_ALIGNMENT_DIRECTORY, outputFolderPath);
        Utilities.printMessage("Finished word alignment using Berkeley Aligner.");
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
