import java.io.*;
import java.util.Arrays;
import java.util.List;

public class ParallelCorpusBuilder {

    private static List<String> corpusesNames = Arrays.asList("Europarl", "EUbookshop", "GNOME", "KDE4", "Ubuntu", "Wikipedia");
    private static int sentNum = 0;

    public static void main(String[] args) throws FileNotFoundException {
        String dirPath = "/home/lsienko/Pobrane/NOWE_KORPUSY_ORG/parallel/test/";
        File outputFileEng = new File(dirPath, "corpus.en");
        File outputFilePl = new File(dirPath, "corpus.pl");
        PrintWriter engTrainOut = new PrintWriter(outputFileEng);
        PrintWriter plTrainOut = new PrintWriter(outputFilePl);
        for (String corpusName: corpusesNames) {
            int corpusSentNum = 0;
            File engFile = new File(dirPath, corpusName + ".en");
            File plFile = new File(dirPath, corpusName + ".pl");
            try (BufferedReader engIn = new BufferedReader(new FileReader(engFile.getCanonicalPath()));
                 BufferedReader plIn = new BufferedReader(new FileReader(plFile.getCanonicalPath()));) {
                String engCurrentLine;
                String plCurrentLine;
                while ((engCurrentLine = engIn.readLine()) != null
                        && (plCurrentLine = plIn.readLine()) != null) {
                    engTrainOut.write(engCurrentLine+"\n");
                    plTrainOut.write(plCurrentLine+"\n");
                    sentNum = sentNum + 1;
                    corpusSentNum = corpusSentNum + 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Liczba zdań korpusu: " + corpusName + " to: "+corpusSentNum);
        }
        engTrainOut.close();
        plTrainOut.close();
        System.out.println("Liczba zdań to: "+sentNum);
    }

}

