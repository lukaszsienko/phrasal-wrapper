import java.io.*;
import java.util.Arrays;
import java.util.List;

public class MonolongualCorpusBuilder {

    private static List<String> corpusesNames = Arrays.asList("Europarl", "EUbookshop", "GNOME", "KDE4", "Ubuntu", "Wikipedia");
    private static int sentNum = 0;

    public static void main(String[] args) throws FileNotFoundException {
        String dirPath = "/home/lsienko/Pobrane/NOWE_KORPUSY_ORG/monolingual_eng/extracted";
        File outputFileEng = new File(dirPath, "monolingual.en");
        PrintWriter engTrainOut = new PrintWriter(outputFileEng);
        for (String corpusName: corpusesNames) {
            int corpusSentNum = 0;
            File engFile = new File(dirPath, corpusName + ".en");
            try (BufferedReader engIn = new BufferedReader(new FileReader(engFile.getCanonicalPath()));) {
                String engCurrentLine;
                while ((engCurrentLine = engIn.readLine()) != null) {
                    engTrainOut.write(engCurrentLine+"\n");
                    sentNum = sentNum + 1;
                    corpusSentNum = corpusSentNum + 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Liczba zdań korpusu: " + corpusName + " to: "+corpusSentNum);
        }
        engTrainOut.close();
        System.out.println("Liczba zdań to: "+sentNum);
    }

}

