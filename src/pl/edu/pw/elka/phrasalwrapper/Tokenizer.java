package pl.edu.pw.elka.phrasalwrapper;

import java.io.*;

public class Tokenizer {

    private Tokenizer() {}

    public static String cleanText(String text) {
        return text.toLowerCase().replaceAll("\\p{P}", " ").replaceAll(" +", " ").trim();
    }

    public static int getNumberOfTokens(String text) {
        return text.split("\\s+").length;
    }

    public static File tokenizeFile(File fileToTokenize) throws Exception {
        File tokenizeResultFile = tokenizeFileContents(fileToTokenize);
        return replaceExistingFileWithOtherFileInTheSameDirectory(fileToTokenize, tokenizeResultFile);
    }

    private static File tokenizeFileContents(File inputFile) {
        File outputFileDirectory = inputFile.getParentFile();
        String outputFileName = inputFile.getName()+".temp";

        File outputFile = new File(outputFileDirectory, outputFileName);

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile.getCanonicalPath()));
             PrintWriter out = new PrintWriter(outputFile)) {

            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                currentLine = cleanText(currentLine);
                out.write(currentLine+"\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputFile;
    }

    private static File replaceExistingFileWithOtherFileInTheSameDirectory(File existingFile, File newFile) throws Exception {
        String existingFileName = existingFile.getName();
        String existingFilePath = existingFile.getCanonicalPath();

        existingFile.delete();

        Utilities.renameFile(newFile.getCanonicalPath(), existingFileName);

        return new File(existingFilePath);
    }
}
