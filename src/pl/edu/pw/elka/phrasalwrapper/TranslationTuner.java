package pl.edu.pw.elka.phrasalwrapper;

import edu.stanford.nlp.mt.tune.OnlineTuner;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelDirectory;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelFile;
import pl.edu.pw.elka.phrasalwrapper.model_persistence.ModelsPersistence;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class TranslationTuner {

    private ParallelCorpus tuningCorpus;
    private ModelsPersistence modelsPersistence;
    private String outputDirectoryPath;

    public TranslationTuner(ParallelCorpus tuningCorpus, ModelsPersistence modelsPersistence) {
        this.tuningCorpus = tuningCorpus;
        this.modelsPersistence = modelsPersistence;

        outputDirectoryPath = ModelDirectory.generateCanonicalPathToWholeModelDirectory(modelsPersistence, ModelDirectory.TUNER_MODEL);
    }

    public void runTuning() throws Exception {
        Utilities.extractAndLoadKenLMLibrary(modelsPersistence);

        File outputDirectory = Utilities.createDirectoryRemovingOldIfExisits(this.outputDirectoryPath);

        String iniFilePath = buildPhrasalIniFile();

        String exampleBinwtsFilePath = outputDirectory.getCanonicalPath()+"/example.binwts";
        File exampleBinwtsFile = new File(exampleBinwtsFilePath);
        if (exampleBinwtsFile.exists()) {
            exampleBinwtsFile.delete();
        }
        exampleBinwtsFile.createNewFile();

        String[] tuning_args = new String[19];
        tuning_args[0] = tuningCorpus.getForeignFilePath();
        tuning_args[1] = tuningCorpus.getEnglishFilePath();
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

        File tunerFinalWeightsFile = new File(System.getProperty("user.dir")+"/tuning.online.final.binwts");
        modelsPersistence.registerNewDetectedModelFile(ModelFile.TUNER_WEIGHTS, tunerFinalWeightsFile.getCanonicalPath());
    }

    private String buildPhrasalIniFile() throws IOException {
        Map<String, String> parameters = getParametersMap();
        String iniFilePath = outputDirectoryPath+"/phrasal.ini";

        try (PrintWriter output = new PrintWriter(iniFilePath)){
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

    private Map<String, String> getParametersMap() throws IOException {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("ttable-file", modelsPersistence.getDetectedModelFilePath(ModelFile.TRANSLATION_PHRASE_TABLE));
        parameters.put("lmodel-file", "kenlm:"+modelsPersistence.getDetectedModelFilePath(ModelFile.LANG_MODEL_BIN));
        parameters.put("ttable-limit", "20");
        parameters.put("distortion-limit", "5");
        parameters.put("reordering-model", "hierarchical\n"+modelsPersistence.getDetectedModelFilePath(ModelFile.TRANSLATION_REORDERING_MODEL)+"\nmsd2-bidirectional-fe\nhierarchical\nhierarchical\nbin");
        parameters.put("threads", "2");
        return parameters;
    }
}