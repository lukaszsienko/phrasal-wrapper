package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.tune.OnlineTuner;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class TranslationTuner {

    private String englishPartOfParallerTuningCorpusPath;
    private String foreignPartOfParallerTuningCorpusPath;
    private LanguageModel languageModel;
    private TranslationModel translationModel;

    private File englishPartOfParallerTuningCorpusFile;
    private File foreignPartOfParallerTuningCorpusFile;

    private String outputDirectoryPath;
    private String tunerFinalWeightsFilePath;

    public TranslationTuner(String englishPartOfParallerTuningCorpusPath, String foreignPartOfParallerTuningCorpusPath, ParallerCorpus trainingCorpus, LanguageModel languageModel, TranslationModel translationModel) throws IOException {
        this.englishPartOfParallerTuningCorpusFile = new File(englishPartOfParallerTuningCorpusPath.trim());
        if (this.englishPartOfParallerTuningCorpusFile.exists() == false) {
            System.out.println("English part of paraller tuning corpus file path: "+englishPartOfParallerTuningCorpusPath.trim());
            throw new FileNotFoundException("Cannot find the file of english part of paraller tuning corpus at specified path. Check specified file path and name.");
        }
        this.englishPartOfParallerTuningCorpusPath = this.englishPartOfParallerTuningCorpusFile.getCanonicalPath();

        this.foreignPartOfParallerTuningCorpusFile = new File(foreignPartOfParallerTuningCorpusPath.trim());
        if (this.foreignPartOfParallerTuningCorpusFile.exists() == false) {
            System.out.println("Foreign part of paraller tuning corpus file path: "+foreignPartOfParallerTuningCorpusPath.trim());
            throw new FileNotFoundException("Cannot find the file of foreign part of paraller tuning corpus at specified path. Check specified file path and name.");
        }
        this.foreignPartOfParallerTuningCorpusPath = this.foreignPartOfParallerTuningCorpusFile.getCanonicalPath();

        this.languageModel = languageModel;
        this.translationModel = translationModel;

        outputDirectoryPath = trainingCorpus.getPathToModelsFolder() + "/tuner_output";
    }

    public void tokenizeTuningCorpus() throws Exception {
        Tokenizer.tokenizeFile(englishPartOfParallerTuningCorpusFile);
        Tokenizer.tokenizeFile(foreignPartOfParallerTuningCorpusFile);
    }

    public void runTuning() throws Exception {
        languageModel.extractAndLoadKenLMLibrary();

        File outputDirectory = new File(this.outputDirectoryPath);
        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
        outputDirectory.mkdir();

        String iniFilePath = buildPhrasalIniFile();

        String exampleBinwtsFilePath = this.outputDirectoryPath+"/example.binwts";
        File exampleBinwtsFile = new File(exampleBinwtsFilePath);
        if (exampleBinwtsFile.exists()) {
            exampleBinwtsFile.delete();
        }
        exampleBinwtsFile.createNewFile();

        String[] tuning_args = new String[19];
        tuning_args[0] = foreignPartOfParallerTuningCorpusPath;
        tuning_args[1] = englishPartOfParallerTuningCorpusPath;
        tuning_args[2] = iniFilePath;
        tuning_args[3] = exampleBinwtsFile.getCanonicalPath();
        tuning_args[4] = "-n";
        tuning_args[5] = "tuning";
        tuning_args[6] = "-e";
        tuning_args[7] = "8";
        tuning_args[8] = "-ef";
        tuning_args[9] = "20";
        tuning_args[10] = "-b";
        tuning_args[11] = "20";
        tuning_args[12] = "-uw";
        tuning_args[13] = "-m";
        tuning_args[14] = "bleu-smooth";
        tuning_args[15] = "-o";
        tuning_args[16] = "pro-sgd";
        tuning_args[17] = "-of";
        tuning_args[18] = "1,5000,50,0.5,Infinity,0.02,adagradl1f,0.1";

        OnlineTuner.main(tuning_args);

        tunerFinalWeightsFilePath = System.getProperty("user.dir")+"/tuning.online.final.binwts";
    }

    /**
     *
     * @return canonical path to phrasal.ini file
     * @throws FileNotFoundException
     */
    private String buildPhrasalIniFile() throws FileNotFoundException {
        Map<String, String> parameters = getParametersMap();
        String iniFilePath = outputDirectoryPath+"/phrasal.ini";

        try (PrintWriter output = new PrintWriter(iniFilePath);){
            for (Map.Entry<String, String> entry : parameters.entrySet())
            {
                output.write("\n");
                output.write("["+entry.getKey()+"]"+"\n");
                output.write(entry.getValue());
                output.write("\n");
            }
        }

        return iniFilePath;
    }

    private Map<String, String> getParametersMap() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("ttable-file", translationModel.getOutputFolder()+"/phrase-table.gz");
        parameters.put("lmodel-file", "kenlm:"+languageModel.getOutputFolder()+"/"+languageModel.getModelBinaryFileName());
        parameters.put("ttable-limit", "20");
        parameters.put("distortion-limit", "5");
        parameters.put("reordering-model", "hierarchical\n"+translationModel.getOutputFolder()+"/lo-hier.msd2-bidirectional-fe.gz"+"\nmsd2-bidirectional-fe\nhierarchical\nhierarchical\nbin");
        parameters.put("threads", "2");
        return parameters;
    }

    public String getTunerFinalWeightsFilePath() {
        return tunerFinalWeightsFilePath;
    }
}