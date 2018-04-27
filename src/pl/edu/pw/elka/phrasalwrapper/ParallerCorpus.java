package pl.edu.pw.elka.phrasalwrapper;

import java.io.*;
import java.nio.file.Files;

/**
 * Created by lsienko on 26.04.18.
 */
public class ParallerCorpus {

    private String pathToFolder; //TODO calculate
    private String englishFilePath;
    private String foreignFilePath;
    private String englishFileNameSuffix;//TODO calculate
    private String foreignFileNameSuffix;//TODO calculate
    private File englishCorpusSideFile;
    private File foreignCorpusSideFile;

    public ParallerCorpus(String englishFilePath, String foreignFilePath) throws FileNotFoundException {
        this.englishFilePath = englishFilePath;
        this.foreignFilePath = foreignFilePath;
        this.englishFileNameSuffix = ""; //TODO calculate
        this.foreignFileNameSuffix = ""; //TODO calculate
        //TODO rename file if there is no suffix

        this.englishCorpusSideFile = new File(englishFilePath);
        if (this.englishCorpusSideFile.exists() == false) {
            throw new FileNotFoundException("Cannot find the file of English-side paraller corpus at specified path.");
        }
        this.foreignCorpusSideFile = new File(foreignFilePath);
        if (this.foreignCorpusSideFile.exists() == false) {
            throw new FileNotFoundException("Cannot find the file of foreign-side paraller corpus at specified path.");
        }
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

    public void tokenize() {
        File srcTokFile = new File(getClass().getResource("/tokenizer/tokenizer.perl").getPath());
        File srcLowFile = new File(getClass().getResource("/tokenizer/lowercase.perl").getPath());
        File prefixFile = new File(getClass().getResource("/tokenizer/nonbreaking_prefixes/nonbreaking_prefix.en").getPath());

        File dstTokFile = new File(this.englishCorpusSideFile.getParent() + "/tokenizer.perl");
        File dstLowFile = new File(this.englishCorpusSideFile.getParent() + "/lowercase.perl");
        File dstPrefixesDir = new File(this.englishCorpusSideFile.getParent() + "/nonbreaking_prefixes");
        File dstPrefixesFile = new File(this.englishCorpusSideFile.getParent() + "/nonbreaking_prefixes/nonbreaking_prefix.en");

        try {
            dstTokFile.delete();
            dstLowFile.delete();
            dstPrefixesFile.delete();
            dstPrefixesDir.delete();
            Files.copy(srcTokFile.toPath(), dstTokFile.toPath());
            Files.copy(srcLowFile.toPath(), dstLowFile.toPath());
            dstPrefixesDir.mkdir();
            Files.copy(prefixFile.toPath(), dstPrefixesFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String outputEnglishFilePath = this.englishCorpusSideFile.getParent() + "/" + this.englishCorpusSideFile.getName() + ".tok" + "." + englishFileNameSuffix;
        String outputForeignFilePath = this.foreignCorpusSideFile.getParent() + "/" + this.foreignCorpusSideFile.getName() + ".tok" + "." + foreignFileNameSuffix;

        String englishCmd = "cat" + " " + this.englishCorpusSideFile.getAbsolutePath() + " | " + dstTokFile.getAbsolutePath() + " -l " + "en" + " | " + dstLowFile.getAbsolutePath() + " > " + outputEnglishFilePath;
        String foreignCmd = "cat" + " " + this.foreignCorpusSideFile.getAbsolutePath() + " | " + dstTokFile.getAbsolutePath() + " -l " + "en" + " | " + dstLowFile.getAbsolutePath() + " > " + outputForeignFilePath;

        System.err.println(englishCmd);
        System.err.println(foreignCmd);

        try {
            Runtime runtime = Runtime.getRuntime();

            //TODO check
            Process engProcess = runtime.exec(englishCmd);
            Process forProcess = runtime.exec(foreignCmd);

            engProcess.waitFor();
            forProcess.waitFor();

            System.err.println("Exit status=" + engProcess.exitValue());

        } catch (Throwable t) {
            t.printStackTrace();
        }

        dstTokFile.delete();
        dstLowFile.delete();
        dstPrefixesFile.delete();
        dstPrefixesDir.delete();

        englishFilePath = outputEnglishFilePath;
        foreignFilePath = outputForeignFilePath;
    }

    public String getPathToFolder() {
        return pathToFolder;
    }
}
