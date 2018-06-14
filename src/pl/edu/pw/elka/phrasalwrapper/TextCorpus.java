package pl.edu.pw.elka.phrasalwrapper;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public class TextCorpus {

    private String filePath;
    private File file;
    private String filenameExtension;

    public TextCorpus(String filePath) throws IOException {
        this.file = new File(filePath.trim());
        if (!file.exists()) {
            System.out.println("TextCorpus: Cannot find a file at specified path: "+filePath.trim());
            throw new FileNotFoundException("TextCorpus: Cannot find a file at specified path: "+filePath.trim());
        }

        this.filePath = file.getCanonicalPath();
        this.filenameExtension = FilenameUtils.getExtension(filePath);
    }

    public TextCorpus(String filePath, String newFilenameToBeSet) throws IOException {
        this(filePath);
        if (!file.getName().equals(newFilenameToBeSet)) {
            Path pathToNewCorpusFile = Utilities.renameFile(this.filePath, newFilenameToBeSet);
            this.file = pathToNewCorpusFile.toFile();
            this.filePath = this.file.getCanonicalPath();
            this.filenameExtension = FilenameUtils.getExtension(this.filePath);
        }
    }

    public void tokenize() throws Exception {
        file = Tokenizer.tokenizeFile(file);
    }

    public String getCorpusFilePath() {
        return filePath;
    }

    public File getCorpusFile() {
        return file;
    }

    public String getCorpusFilenameExtension() {
        return filenameExtension;
    }


}
