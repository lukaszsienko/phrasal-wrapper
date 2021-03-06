package pl.edu.pw.elka.phrasalwrapper.word_alignment;

import org.apache.commons.io.FileUtils;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelDirectory;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelFile;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;
import pl.edu.pw.elka.phrasalwrapper.ParallelCorpus;
import pl.edu.pw.elka.phrasalwrapper.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class GizaWordAlignmentModel {

    private ParallelCorpus parallelCorpus;
    private String outputFolder;
    private String forToEngAlignmentFilePath;
    private String engToForAlignmentFilePath;
    private ModelsPersistence modelsPersistence;

    public GizaWordAlignmentModel(ParallelCorpus parallelCorpus, ModelsPersistence modelsPersistence) {
        this.parallelCorpus = parallelCorpus;
        this.outputFolder = ModelDirectory.generateCanonicalPathToWholeModelDirectory(modelsPersistence, ModelDirectory.GIZA_WORD_ALIGNMENT);
        this.modelsPersistence = modelsPersistence;
    }

    public void runWordAlignmentProcess() throws Exception {
        Utilities.printMessage("Started word alignment using GIZA++ aligner...");
        File outputDirectory = Utilities.createDirectoryRemovingOldIfExists(this.outputFolder);

        File gizaSoftDir = extractGizaSoftware(outputDirectory.getCanonicalPath());
        File forToEngAlignmentDir = new File(outputDirectory, "align_for_eng");
        File engToForAlignmentDir = new File(outputDirectory, "align_eng_for");
        boolean succForEng = forToEngAlignmentDir.mkdir();
        boolean succEngFor = engToForAlignmentDir.mkdir();
        if (!succForEng || !succEngFor) {
            throw new Exception("Cannot create output folders for giza alignment process.");
        }

        File script = new File(gizaSoftDir.getCanonicalPath()+"/run_align");
        boolean fileIsExecutable = script.setExecutable(true);
        if (!fileIsExecutable) {
            throw new Exception("Giza alignment script file cannot be made executable");
        }

        String[] run_script_cmd = {"/bin/sh",
                script.getAbsolutePath(),
                parallelCorpus.getForeignFilePath(),
                parallelCorpus.getEnglishFilePath(),
                forToEngAlignmentDir.getCanonicalPath(),
                engToForAlignmentDir.getCanonicalPath()};

        ProcessBuilder pb = new ProcessBuilder(Arrays.asList(run_script_cmd));
        pb.directory(gizaSoftDir);
        pb.inheritIO();

        Process gizaAlignProcess = pb.start();
        gizaAlignProcess.waitFor();

        if (gizaAlignProcess.exitValue() != 0) {
            throw new Exception("Giza word alignment exception, run align script command did not return 0.");
        } else {
            forToEngAlignmentFilePath = forToEngAlignmentDir.getCanonicalPath()+"/alignment.A3.final";
            engToForAlignmentFilePath = engToForAlignmentDir.getCanonicalPath()+"/alignment.A3.final";
            modelsPersistence.registerNewDetectedModelFile(ModelFile.GIZA_FOR_TO_ENG_ALIGNMENT, forToEngAlignmentFilePath);
            modelsPersistence.registerNewDetectedModelFile(ModelFile.GIZA_ENG_TO_FOR_ALIGNMENT, engToForAlignmentFilePath);
            Utilities.printMessage("Finished word alignment using GIZA++ aligner.");
        }
    }

    private File extractGizaSoftware(String pathToDestParentFolder) throws IOException {
        File dest = new File(pathToDestParentFolder + "/giza_software");
        if (dest.exists()) {
            FileUtils.deleteDirectory(dest);
        }
        Utilities.extractDirectory("/giza_software", pathToDestParentFolder);

        return dest;
    }


    public String getForToEngWordAlignmentFilePath() {
        return forToEngAlignmentFilePath;
    }

    public String getEngToForWordAlignmentFilePath() {
        return engToForAlignmentFilePath;
    }
}
