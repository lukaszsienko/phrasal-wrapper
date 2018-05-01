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
            throw new FileNotFoundException("Cannot find the file of English-side paraller corpus at specified path. Check file name (might have been changed to cover the requirements).");
        }
        this.foreignCorpusSideFile = new File(foreignFilePath);
        if (this.foreignCorpusSideFile.exists() == false) {
            throw new FileNotFoundException("Cannot find the file of foreign-side paraller corpus at specified path. Check file name (might have been changed to cover the requirements).");
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
        File modelsDir = new File(pathToModelsFolder);
        if (!modelsDir.exists()) {
            modelsDir.mkdir();
        }
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

    public void tokenize() throws Exception {
        Path srcTokFilePath = Utilities.getResourcePath("/tokenizer/tokenizer.perl");
        Path srcLowFilePath = Utilities.getResourcePath("/tokenizer/lowercase.perl");
        Path prefixFilePath = Utilities.getResourcePath("/tokenizer/nonbreaking_prefixes/nonbreaking_prefix.en");

        File dstTokFile = new File(this.englishCorpusSideFile.getParent() + "/tokenizer.perl");
        File dstLowFile = new File(this.englishCorpusSideFile.getParent() + "/lowercase.perl");
        File dstPrefixesDir = new File(this.englishCorpusSideFile.getParent() + "/nonbreaking_prefixes");
        File dstPrefixesFile = new File(this.englishCorpusSideFile.getParent() + "/nonbreaking_prefixes/nonbreaking_prefix.en");

        dstTokFile.delete();
        dstLowFile.delete();
        dstPrefixesFile.delete();
        dstPrefixesDir.delete();
        Files.copy(srcTokFilePath, dstTokFile.toPath());
        Files.copy(srcLowFilePath, dstLowFile.toPath());
        dstPrefixesDir.mkdir();
        Files.copy(prefixFilePath, dstPrefixesFile.toPath());

        String englishCorpusSideFileName = englishCorpusSideFile.getName();
        String foreignCorpusSideFileName = foreignCorpusSideFile.getName();

        String outputEnglishFilePath = englishCorpusSideFile.getParent() + "/" + englishCorpusSideFileName + ".tok" + "." + englishFileNameSuffix;
        String outputForeignFilePath = foreignCorpusSideFile.getParent() + "/" + foreignCorpusSideFileName + ".tok" + "." + foreignFileNameSuffix;

        String englishCmd = "cat" + " " + this.englishCorpusSideFile.getAbsolutePath() + " | " + dstTokFile.getAbsolutePath() + " -l " + "en" + " | " + dstLowFile.getAbsolutePath() + " > " + outputEnglishFilePath;
        String foreignCmd = "cat" + " " + this.foreignCorpusSideFile.getAbsolutePath() + " | " + dstTokFile.getAbsolutePath() + " -l " + "en" + " | " + dstLowFile.getAbsolutePath() + " > " + outputForeignFilePath;

        Runtime runtime = Runtime.getRuntime();

        String[] eng_cmd = {"/bin/sh","-c",englishCmd};
        String[] for_cmd = {"/bin/sh","-c",foreignCmd};
        Process engProcess = runtime.exec(eng_cmd);
        Process forProcess = runtime.exec(for_cmd);

        engProcess.waitFor();
        forProcess.waitFor();

        if (engProcess.exitValue() != 0) {
            throw new Exception("English-corpus side tokenization exception, command did not return 0.");
        }
        if (forProcess.exitValue() != 0) {
            throw new Exception("Foreign-corpus side tokenization exception, command did not return 0.");
        }

        //Firstly remove originals
        englishCorpusSideFile.delete();
        foreignCorpusSideFile.delete();
        //Then rename tokenized to original names.
        renameFile(outputEnglishFilePath, englishCorpusSideFileName);
        renameFile(outputForeignFilePath, foreignCorpusSideFileName);

        englishCorpusSideFile = new File(englishFilePath);
        foreignCorpusSideFile = new File(foreignFilePath);

        dstTokFile.delete();
        dstLowFile.delete();
        dstPrefixesFile.delete();
        dstPrefixesDir.delete();
    }
}
