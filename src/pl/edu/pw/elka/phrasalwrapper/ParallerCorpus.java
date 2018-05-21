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
    private String englishNonParallerCorpusPath;
    private File englishCorpusSideFile;
    private File foreignCorpusSideFile;
    private File englishNonParallerCorpusFile;

    private String pathToModelsFolder;

    public ParallerCorpus(String englishFilePath, String foreignFilePath, String englishNonParallerCorpusPath) throws IOException {
        this(englishFilePath, foreignFilePath);

        this.englishNonParallerCorpusFile = new File(englishNonParallerCorpusPath.trim());
        if (this.englishNonParallerCorpusFile.exists() == false) {
            System.out.println("English-only/non-paraller corpus file path: "+englishNonParallerCorpusPath.trim());
            throw new FileNotFoundException("Cannot find the file of English-only/non-paraller corpus at specified path. Check specified file path and name.");
        }
        this.englishNonParallerCorpusPath = this.englishNonParallerCorpusFile.getCanonicalPath();
    }

    public ParallerCorpus(String englishFilePath, String foreignFilePath) throws IOException {
        this.englishCorpusSideFile = new File(englishFilePath.trim());
        if (this.englishCorpusSideFile.exists() == false) {
            System.out.println("English-side specified path: "+englishFilePath.trim());
            throw new FileNotFoundException("Cannot find the file of English-side paraller corpus at specified path. Check file name (might have been changed to cover the requirements).");
        }
        this.foreignCorpusSideFile = new File(foreignFilePath.trim());
        if (this.foreignCorpusSideFile.exists() == false) {
            System.out.println("Foreign-side specified path: "+foreignFilePath.trim());
            throw new FileNotFoundException("Cannot find the file of foreign-side paraller corpus at specified path. Check file name (might have been changed to cover the requirements).");
        }

        this.englishFilePath = englishCorpusSideFile.getCanonicalPath();
        this.foreignFilePath = foreignCorpusSideFile.getCanonicalPath();

        // Detect files suffixes. Add suffixes when no suffix detected.
        englishFileNameSuffix = FilenameUtils.getExtension(this.englishFilePath);
        foreignFileNameSuffix = FilenameUtils.getExtension(this.foreignFilePath);
        if (englishFileNameSuffix == "" || foreignFileNameSuffix == "" || englishFileNameSuffix == foreignFileNameSuffix) {
            englishFileNameSuffix = "eng";
            foreignFileNameSuffix = "for";
            Utilities.renameFile(this.englishFilePath, this.englishCorpusSideFile.getName() + "." + this.englishFileNameSuffix);
            Utilities.renameFile(this.foreignFilePath, this.foreignCorpusSideFile.getName() + "." + this.foreignFileNameSuffix);
            this.englishFilePath = this.englishFilePath + "."+ englishFileNameSuffix;
            this.foreignFilePath = this.foreignFilePath + "."+ foreignFileNameSuffix;
            this.englishCorpusSideFile = new File(this.englishFilePath);
            this.foreignCorpusSideFile = new File(this.foreignFilePath);
        }

        File modelsDir = new File(englishCorpusSideFile.getParentFile().getCanonicalPath() + "/models");
        if (!modelsDir.exists()) {
            modelsDir.mkdir();
        }
        pathToModelsFolder = modelsDir.getCanonicalPath();
    }

    public void tokenize() throws Exception {
        englishCorpusSideFile = Tokenizer.tokenizeFile(englishCorpusSideFile);
        foreignCorpusSideFile = Tokenizer.tokenizeFile(foreignCorpusSideFile);

        if (englishNonParallerCorpusFile != null) {
            englishNonParallerCorpusFile = Tokenizer.tokenizeFile(englishNonParallerCorpusFile);
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

    public String getEnglishNonParallerCorpusPath() {
        return englishNonParallerCorpusPath;
    }

    public String getPathToModelsFolder() {
        return pathToModelsFolder;
    }
}
