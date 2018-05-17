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
            renameFile(this.englishFilePath, this.englishCorpusSideFile.getName() + "." + this.englishFileNameSuffix);
            renameFile(this.foreignFilePath, this.foreignCorpusSideFile.getName() + "." + this.foreignFileNameSuffix);
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

    public String getEnglishNonParallerCorpusPath() {
        return englishNonParallerCorpusPath;
    }

    public String getPathToModelsFolder() {
        return pathToModelsFolder;
    }

    public void tokenize() throws Exception {
        File engResult = tokenizeFile(englishCorpusSideFile);
        File forResult = tokenizeFile(foreignCorpusSideFile);

        //Replace englishCorpusSideFile with engResult
        //    and foreignCorpusSideFile with forResult
        // 0) Prepare necessary info about output file
        String engFileName = englishCorpusSideFile.getName();
        String forFileName = foreignCorpusSideFile.getName();
        String engFilePath = englishCorpusSideFile.getCanonicalPath();
        String forFilePath = foreignCorpusSideFile.getCanonicalPath();

        // 1) Remove old files
        englishCorpusSideFile.delete();
        foreignCorpusSideFile.delete();

        // 2) Give old files' names to new files
        renameFile(engResult.getCanonicalPath(), engFileName);
        renameFile(forResult.getCanonicalPath(), forFileName);

        // 3) Update references to new files
        englishCorpusSideFile = new File(engFilePath);
        foreignCorpusSideFile = new File(forFilePath);

        //Do the same for english-only corpus if necessary
        if (englishNonParallerCorpusFile != null) {
            File engCorpusResult = tokenizeFile(englishNonParallerCorpusFile);

            String fileName = englishNonParallerCorpusFile.getName();
            String filePath = englishNonParallerCorpusFile.getCanonicalPath();

            englishNonParallerCorpusFile.delete();
            renameFile(engCorpusResult.getCanonicalPath(), fileName);
            englishNonParallerCorpusFile = new File(filePath);
        }
    }

    private File tokenizeFile(File inputFile) {
        File outputFileDirectory = inputFile.getParentFile();
        String outputFileName = inputFile.getName()+".temp";

        File outputFile = new File(outputFileDirectory, outputFileName);

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile.getCanonicalPath()));
             PrintWriter out = new PrintWriter(outputFile)) {

            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                //process currentLine
                currentLine = Utilities.cleanTextBeforeProcessing(currentLine);

                //write to output
                out.write(currentLine+"\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputFile;
    }
}
