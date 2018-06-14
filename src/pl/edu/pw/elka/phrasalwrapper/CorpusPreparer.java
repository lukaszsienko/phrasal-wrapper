package pl.edu.pw.elka.phrasalwrapper;

import java.io.*;

public class CorpusPreparer {

    private File forSideOfParallelCorpus;
    private File engSideOfParallelCorpus;

    private ParallelCorpus trainingCorpus;
    private ParallelCorpus tuningCorpus;

    public CorpusPreparer(String foreignPartFilePath, String englishPartFilePath) throws FileNotFoundException {
        forSideOfParallelCorpus = new File(foreignPartFilePath.trim());
        engSideOfParallelCorpus = new File(englishPartFilePath.trim());
        if (!forSideOfParallelCorpus.exists() ) {
            throw new FileNotFoundException("CorpusPreparer: Cannot find a file at specified path: " + foreignPartFilePath.trim());
        } else if (!engSideOfParallelCorpus.exists()) {
            throw new FileNotFoundException("CorpusPreparer: Cannot find a file at specified path: " + englishPartFilePath.trim());
        }
    }

    public void splitCorpusIntoTrainAndTuneParts(final int EVERY_N_TH_GOES_TO_TUNING_SET) throws IOException {
        Utilities.printMessage("Preparing train and tune corpuses...");
        File outputFilesDirectory = forSideOfParallelCorpus.getParentFile();

        File trainDir = makeNewDirectory(outputFilesDirectory, "train_corpus");
        File forTrainFile = new File(trainDir, "training.for");
        File engTrainFile = new File(trainDir, "training.eng");

        File tuneDir = makeNewDirectory(outputFilesDirectory, "tune_corpus");
        File forTuneFile = new File(tuneDir, "tuning.for");
        File engTuneFile = new File(tuneDir, "tuning.eng");

        int lineCounter = 1;
        try (BufferedReader forIn = new BufferedReader(new FileReader(forSideOfParallelCorpus.getCanonicalPath()));
             BufferedReader engIn = new BufferedReader(new FileReader(engSideOfParallelCorpus.getCanonicalPath()));
             PrintWriter forTrainOut = new PrintWriter(forTrainFile);
             PrintWriter forTuneOut = new PrintWriter(forTuneFile);
             PrintWriter engTrainOut = new PrintWriter(engTrainFile);
             PrintWriter engTuneOut = new PrintWriter(engTuneFile)) {

            Integer addedTrain = 0;
            Integer addedTune = 0;
            Integer rejected = 0;
            Integer linenr = 0;

            String forCurrentLine;
            String engCurrentLine;
            while ((forCurrentLine = forIn.readLine()) != null &&
                    (engCurrentLine = engIn.readLine()) != null) {
                linenr++;

                forCurrentLine = Tokenizer.cleanText(forCurrentLine);
                engCurrentLine = Tokenizer.cleanText(engCurrentLine);
                int forLineWordsNum = Tokenizer.getNumberOfTokens(forCurrentLine);
                int engLineWordsNum = Tokenizer.getNumberOfTokens(engCurrentLine);

                if (forCurrentLine.isEmpty() || engCurrentLine.isEmpty()
                        || forLineWordsNum >= 256 || engLineWordsNum >= 256) {
                    rejected++;
                    continue;
                }

                if (lineCounter == 0) {
                    /*goes to tune set*/
                    forTuneOut.write(forCurrentLine+"\n");
                    engTuneOut.write(engCurrentLine+"\n");
                    addedTune++;
                } else {
                    /*goes to train set*/
                    forTrainOut.write(forCurrentLine+"\n");
                    engTrainOut.write(engCurrentLine+"\n");
                    addedTrain++;
                }

                lineCounter = (lineCounter + 1) % EVERY_N_TH_GOES_TO_TUNING_SET;
            }

            Utilities.printMessage("Finished. Statistics info: ");
            System.out.println("Number of all pairs of lines: "+linenr);
            System.out.println("Accepted pairs of line: "+(addedTrain+addedTune));
            System.out.println("- added to train: "+addedTrain);
            System.out.println("- added to tune: "+addedTune);
            System.out.println("Rejected line pairs: "+rejected+" which is approx. "+Math.round(100*(rejected/linenr))+"% of entire corpus.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        trainingCorpus = new ParallelCorpus(forTrainFile.getCanonicalPath(), engTrainFile.getCanonicalPath(), "training");
        tuningCorpus = new ParallelCorpus(forTuneFile.getCanonicalPath(), engTuneFile.getCanonicalPath(), "tuning");
    }

    private File makeNewDirectory(File location, String dirName) throws IOException {
        File directory = new File(location, dirName);
        boolean success = directory.mkdir();
        if (!success) {
            throw new IOException("Cannot create directory name: "+dirName+" at location: "+location.getCanonicalPath());
        }
        return directory;
    }

    public ParallelCorpus getTrainingCorpus() {
        return trainingCorpus;
    }

    public ParallelCorpus getTuningCorpus() {
        return tuningCorpus;
    }
}
