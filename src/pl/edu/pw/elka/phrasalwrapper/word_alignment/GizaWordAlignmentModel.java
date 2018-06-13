package pl.edu.pw.elka.phrasalwrapper.word_alignment;

import org.apache.commons.io.FileUtils;
import pl.edu.pw.elka.phrasalwrapper.ModelsOutputDirectory;
import pl.edu.pw.elka.phrasalwrapper.ParallelCorpus;
import pl.edu.pw.elka.phrasalwrapper.Utilities;

import java.io.File;
import java.io.IOException;

public class GizaWordAlignmentModel {

    private ParallelCorpus parallelCorpus;
    private String outputFolder;
    private String forToEngAlignmentFilePath;
    private String engToForAlignmentFilePath;

    public GizaWordAlignmentModel(ParallelCorpus parallelCorpus, ModelsOutputDirectory modelsOutputDirectory) {
        this.parallelCorpus = parallelCorpus;
        this.outputFolder = modelsOutputDirectory.getCanonicalPathToOutputDir()+"/giza_word_aligner_output";
    }

    public void runWordAlignmentProcess() throws Exception {
        File outputDirectory = new File(this.outputFolder);
        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
        boolean dirCreated = outputDirectory.mkdir();
        if (!dirCreated) {
            throw new Exception("Cannot create output directory for giza alignment processing");
        }

        File gizaSoftDir = extractGizaSoftware(outputDirectory.getCanonicalPath());
        File forToEngAlignmentDir = new File(outputDirectory, "align_for_eng");
        File engToForAlignmentDir = new File(outputDirectory, "align_eng_for");

        File script = new File(gizaSoftDir.getCanonicalPath()+"/run_align");
        boolean fileIsExecutable = script.setExecutable(true);
        if (!fileIsExecutable) {
            throw new Exception("Giza alignment script file cannot be made executable");
        }

        String[] run_script_cmd = {"/bin/sh","-c",
                script.getAbsolutePath(),
                parallelCorpus.getForeignFilePath(),
                parallelCorpus.getEnglishFilePath(),
                forToEngAlignmentDir.getCanonicalPath(),
                engToForAlignmentDir.getCanonicalPath()};
        Runtime runtime = Runtime.getRuntime();
        Process gizaAlignProcess = runtime.exec(run_script_cmd, null, gizaSoftDir);
        gizaAlignProcess.waitFor();

        if (gizaAlignProcess.exitValue() != 0) {
            Utilities.printBashProcessOutput(gizaAlignProcess);
            throw new Exception("Giza word alignment exception, run align script command did not return 0.");
        } else {
            forToEngAlignmentFilePath = forToEngAlignmentDir.getCanonicalPath()+"/alignment.A3.final";
            engToForAlignmentFilePath = engToForAlignmentDir.getCanonicalPath()+"/alignment.A3.final";
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
