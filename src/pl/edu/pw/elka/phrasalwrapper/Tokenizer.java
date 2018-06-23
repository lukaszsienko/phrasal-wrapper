package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.*;

public class Tokenizer {

    private Tokenizer() {}

    public static String tokenizeAndCleanTextLine(String text) {
        return tokenizeUsingCoreNLP(text).toLowerCase().replaceAll("[^\\p{L}\\p{Nd}]+", " ").replaceAll(" +", " ").trim();
    }

    public static int getNumberOfTokens(String text) {
        return text.split("\\s+").length;
    }

    public static File tokenizeFile(File fileToTokenize) throws Exception {
        File tokenizeResultFile = tokenizeFileContents(fileToTokenize);
        return replaceExistingFileWithOtherFileInTheSameDirectory(fileToTokenize, tokenizeResultFile);
    }

    private static String tokenizeUsingCoreNLP(String str) {
        PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(str), new CoreLabelTokenFactory(), "normalizeParentheses=false,normalizeOtherBrackets=false,untokenizable=noneDelete");

        StringBuilder sb = new StringBuilder();
        while (ptbt.hasNext()) {
            CoreLabel label = ptbt.next();
            sb.append(label.toString());
            if (ptbt.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private static File tokenizeFileContents(File inputFile) {
        File outputFileDirectory = inputFile.getParentFile();
        String outputFileName = inputFile.getName()+".temp";

        File outputFile = new File(outputFileDirectory, outputFileName);

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile.getCanonicalPath()));
             PrintWriter out = new PrintWriter(outputFile)) {

            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                currentLine = tokenizeAndCleanTextLine(currentLine);
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
