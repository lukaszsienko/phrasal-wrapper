package pl.edu.pw.elka.phrasalwrapper;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by lsienko on 26.04.18.
 */
public class ParallerCorpus {

    private String englishFilePath;
    private String foreignFilePath;
    private String englishFileNameSuffix;
    private String foreignFileNameSuffix;
    private File englishCorpusSideFile;
    private File foreignCorpusSideFile;

    private String pathToModelsFolder;

    public ParallerCorpus(String englishFilePath, String foreignFilePath) throws IOException {
        this.englishFilePath = englishFilePath.trim();
        this.foreignFilePath = foreignFilePath.trim();

        this.englishCorpusSideFile = new File(englishFilePath);
        if (this.englishCorpusSideFile.exists() == false) {
            throw new FileNotFoundException("Cannot find the file of English-side paraller corpus at specified path.");
        }
        this.foreignCorpusSideFile = new File(foreignFilePath);
        if (this.foreignCorpusSideFile.exists() == false) {
            throw new FileNotFoundException("Cannot find the file of foreign-side paraller corpus at specified path.");
        }

        // Detect files suffixes. Add suffixes when no suffix detected.
        englishFileNameSuffix = FilenameUtils.getExtension(this.englishFilePath);
        foreignFileNameSuffix = FilenameUtils.getExtension(this.foreignFilePath);
        if (englishFileNameSuffix == "" || foreignFileNameSuffix == "" || englishFileNameSuffix == foreignFileNameSuffix) {
            englishFileNameSuffix = "eng";
            foreignFileNameSuffix = "for";
            renameFile(this.englishFilePath, this.englishCorpusSideFile.getName() + "." + this.englishFileNameSuffix);
            renameFile(this.foreignFilePath, this.foreignCorpusSideFile.getName() + "." + this.foreignFileNameSuffix);
            this.englishFilePath = this.englishFilePath + "."+ englishFileNameSuffix;
            this.foreignFilePath = this.foreignFilePath + "."+ foreignFileNameSuffix;
            this.englishCorpusSideFile = new File(this.englishFilePath);
            this.foreignCorpusSideFile = new File(this.foreignFilePath);
        }

        pathToModelsFolder = englishCorpusSideFile.getParent() + "/models";
        new File(pathToModelsFolder).mkdir();
    }

    private void renameFile(String absoluteFilePath, String newName) throws IOException {
        Path source = Paths.get(absoluteFilePath);
        Files.move(source, source.resolveSibling(newName));
    }

    public String getEnglishFilePath() {
        return englishFilePath;
    }

    public String getForeignFilePath() {
        return foreignFilePath;
    }

    public String getEnglishFileNameSuffix() {
        return englishFileNameSuffix;
    }

    public String getForeignFileNameSuffix() {
        return foreignFileNameSuffix;
    }

    public String getPathToModelsFolder() {
        return pathToModelsFolder;
    }

    public void tokenize() throws IOException, InterruptedException {
        File srcTokFile = new File(getClass().getResource("/tokenizer/tokenizer.perl").getPath());
        File srcLowFile = new File(getClass().getResource("/tokenizer/lowercase.perl").getPath());
        File prefixFile = new File(getClass().getResource("/tokenizer/nonbreaking_prefixes/nonbreaking_prefix.en").getPath());

        File dstTokFile = new File(this.englishCorpusSideFile.getParent() + "/tokenizer.perl");
        File dstLowFile = new File(this.englishCorpusSideFile.getParent() + "/lowercase.perl");
        File dstPrefixesDir = new File(this.englishCorpusSideFile.getParent() + "/nonbreaking_prefixes");
        File dstPrefixesFile = new File(this.englishCorpusSideFile.getParent() + "/nonbreaking_prefixes/nonbreaking_prefix.en");

        dstTokFile.delete();
        dstLowFile.delete();
        dstPrefixesFile.delete();
        dstPrefixesDir.delete();
        Files.copy(srcTokFile.toPath(), dstTokFile.toPath());
        Files.copy(srcLowFile.toPath(), dstLowFile.toPath());
        dstPrefixesDir.mkdir();
        Files.copy(prefixFile.toPath(), dstPrefixesFile.toPath());

        String outputEnglishFilePath = englishCorpusSideFile.getParent() + "/" + englishCorpusSideFile.getName() + ".tok" + "." + englishFileNameSuffix;
        String outputForeignFilePath = foreignCorpusSideFile.getParent() + "/" + foreignCorpusSideFile.getName() + ".tok" + "." + foreignFileNameSuffix;

        String englishCmd = "cat" + " " + this.englishCorpusSideFile.getAbsolutePath() + " | " + dstTokFile.getAbsolutePath() + " -l " + "en" + " | " + dstLowFile.getAbsolutePath() + " > " + outputEnglishFilePath;
        String foreignCmd = "cat" + " " + this.foreignCorpusSideFile.getAbsolutePath() + " | " + dstTokFile.getAbsolutePath() + " -l " + "en" + " | " + dstLowFile.getAbsolutePath() + " > " + outputForeignFilePath;

        System.err.println(englishCmd);
        System.err.println(foreignCmd);

        Runtime runtime = Runtime.getRuntime();



        String[] eng_cmd = {"/bin/sh","-c",englishCmd};
        String[] for_cmd = {"/bin/sh","-c",foreignCmd};

        Process engProcess = runtime.exec(eng_cmd);
        Process forProcess = runtime.exec(for_cmd);

        engProcess.waitFor();
        forProcess.waitFor();


        //TODO remove debug information output
//////////////////////////////////
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(engProcess.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(engProcess.getErrorStream()));

// read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

// read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
//////////////////////////////////








        System.err.println("Exit status=" + engProcess.exitValue());

        dstTokFile.delete();
        dstLowFile.delete();
        dstPrefixesFile.delete();
        dstPrefixesDir.delete();

        englishFilePath = outputEnglishFilePath;
        foreignFilePath = outputForeignFilePath;
        englishCorpusSideFile = new File(englishFilePath);
        foreignCorpusSideFile = new File(foreignFilePath);
    }
}
