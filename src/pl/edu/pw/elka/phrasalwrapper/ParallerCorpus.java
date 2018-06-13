package pl.edu.pw.elka.phrasalwrapper;

import java.io.*;

public class ParallerCorpus {

    private TextCorpus foreignSide;
    private TextCorpus englishSide;

    public ParallerCorpus(String foreignFilePath, String englishFilePath, String corpusName) throws IOException {
        foreignSide = new TextCorpus(foreignFilePath, corpusName + ".for");
        englishSide = new TextCorpus(englishFilePath, corpusName + ".eng");
        //addLangExtensionsToCorpusFiles();
    }

    /*private void addLangExtensionsToCorpusFiles() throws IOException {
        String foreignFileNameEntension = foreignSide.getCorpusFilenameExtension();
        String englishFileNameExtension = englishSide.getCorpusFilenameExtension();
        if (englishFileNameExtension.equals("") || foreignFileNameEntension.equals("") || englishFileNameExtension.equals(foreignFileNameEntension)) {
            foreignSide.changeCorpusFileExtension("eng");
            englishSide.changeCorpusFileExtension("for");
        }
    }*/

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
