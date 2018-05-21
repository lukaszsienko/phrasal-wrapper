import java.io.*;

public class Main {

    private final static int EVERY_N_TH_GOES_TO_TUNING_SET = 10;
    private final static String englishFilePath = "/home/lsienko/Pobrane/test/europarl-v7.pl-en.en";
    private final static String foreignFilePath = "/home/lsienko/Pobrane/test/europarl-v7.pl-en.pl";

    public static void main(String[] args) {
        File engFile = new File(englishFilePath);
        File forFile = new File(foreignFilePath);
        if (!engFile.exists() || !forFile.exists()) {
            System.err.println("Input file(s) not exist at specified path.");
            System.exit(0);
        }

        File outputFilesDirectory = engFile.getParentFile();

        String engTrainFileName = engFile.getName()+".train-eng";
        String engTuneFileName = engFile.getName()+".tune-eng";
        File engTrainFile = new File(outputFilesDirectory, engTrainFileName);
        File engTuneFile = new File(outputFilesDirectory, engTuneFileName);

        String forTrainFileName = forFile.getName()+".train-for";
        String forTuneFileName = forFile.getName()+".tune-for";
        File forTrainFile = new File(outputFilesDirectory, forTrainFileName);
        File forTuneFile = new File(outputFilesDirectory, forTuneFileName);

        int lineCounter = 1;
        try (BufferedReader engIn = new BufferedReader(new FileReader(engFile.getCanonicalPath()));
             BufferedReader forIn = new BufferedReader(new FileReader(forFile.getCanonicalPath()));
             PrintWriter engTrainOut = new PrintWriter(engTrainFile);
             PrintWriter engTuneOut = new PrintWriter(engTuneFile);
             PrintWriter forTrainOut = new PrintWriter(forTrainFile);
             PrintWriter forTuneOut = new PrintWriter(forTuneFile)) {

            String engCurrentLine;
            String forCurrentLine;
            while ((engCurrentLine = engIn.readLine()) != null
                    && (forCurrentLine = forIn.readLine()) != null) {
                if (lineCounter == 0) {
                    /*goes to tune set*/
                    engTuneOut.write(engCurrentLine+"\n");
                    forTuneOut.write(forCurrentLine+"\n");
                } else {
                    /*goes to train set*/
                    engTrainOut.write(engCurrentLine+"\n");
                    forTrainOut.write(forCurrentLine+"\n");
                }

                lineCounter = (lineCounter + 1) % EVERY_N_TH_GOES_TO_TUNING_SET;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

