package pl.edu.pw.elka.phrasalwrapper;

import java.io.*;

public class ParallelCorpus {

    private TextCorpus foreignSide;
    private TextCorpus englishSide;

    public ParallelCorpus(String foreignFilePath, String englishFilePath, String corpusName) throws IOException {
        foreignSide = new TextCorpus(foreignFilePath, corpusName + ".for");
        englishSide = new TextCorpus(englishFilePath, corpusName + ".eng");
    }

    public void tokenize() throws Exception {
        foreignSide.tokenize();
        englishSide.tokenize();
    }

    public String getForeignFilePath() {
        return foreignSide.getCorpusFilePath();
    }

    public String getEnglishFilePath() {
        return englishSide.getCorpusFilePath();
    }

    public String getForeignFilenameExtension() {
        return foreignSide.getCorpusFilenameExtension();
    }

    public String getEnglishFilenameExtension() {
        return englishSide.getCorpusFilenameExtension();
    }
}
