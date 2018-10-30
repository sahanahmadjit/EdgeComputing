/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.utilities;

import java.io.File;

/**
 * Constants.
 * Static variables needed to be accessed throughout the whole project's context.
 * @author jason
 */
public class Constants {
    //Options telling Maui what to do.
    public static String[] mauiKeyOptions = {
		"-l", "data/tmp/", "-m", "keyphrextr", "-t", "PorterStemmer", "-v", "none"
    };
    
    //Folder path for upload
    public static String uploadLocation = ".." + File.separator + "cloud" + File.separator + 
            "cloudserver" + File.separator + "watch";

    //Folder path for Batch Upload

    public static  String batchUploadLocation = ".." + File.separator + "cloud" + File.separator +
            "cloudserver" + File.separator + "batchUpload" ;

    public  static String searchHistoryFileLocation = ".."+ File.separator +"EdgeStore"+ File.separator+ "searchHistory";
    public  static String searchHistoryFileName = "searchRecord.txt";
    public  static String clusterAvgSimDistanceLocation = ".." + File.separator + "EdgeStore" + File.separator + "SimilarityMeasure";
    //Path for config props
    // IDE Version
    public static String configLocation = "src" + File.separator + "clientencryptedsearch" + File.separator + "utilities" + File.separator;
    // Build version
    //public static String configLocation = "utilities" + File.separator;
    
    //Folder path for output from retrieving the requested file
    public static String outputLocation = "data" + File.separator + "output";
    
    //Folder path for stopwords files
    public static String stopwordsLocation = "data" + File.separator + "stopwords";
    
    //Folder path for the storage space for encryption keys
    public static String encryptionKeysLocation = "data" + File.separator + "keys";
    
    //Folder path for the storage space for the abstracts
    public static String abstractLocation = "data" + File.separator + "abstracts";
    
    //Index file name.  This may not be necessary.
    public static String indexFileName = "Index.txt";
    
    
    
    //Encryption Key.
    //TODO this should be randomized at first start, then stored.
    public static String cipherKey = "SemanticSearch";
    
    //Temporary folder for prepping files for upload
    public static String tempLocation = "data" + File.separator + "tmp";
    //Temporary folder for prepping files for batch Upload
    public static String tempLocationBatchUpload = "data" + File.separator + "tmpBatch";
    
    //Name of the system.  Referenced in writing to the evaluation file.
    public static String systemName = "SmartShard";
    
    //Name of the evaluation file.
    public static String evaluationFileName = ".." + File.separator + ".." + File.separator + "evaluation.txt";
    
    public static String modelName = "SmartShard";
    
    public static String metricsFileName = "data" + File.separator + "metrics" + File.separator + "search_metrics.txt";
    
    //Name of the file to write cluster choices to.
    public static String clusterChoiceFileName = "data" + File.separator + "metrics" + File.separator + "ClusterChoices";
    
    //Delimiters for splitting things we write.
    public static String regexIndexDelimiter = "\\|.\\|";
    public static String indexDelimiter = "|.|";


    public static  int TOTAL_NUMBER_OF_CLUSTER = 10;

    /**
     * Get Maui Extraction Options.
     * Returns the options array that represents the appropriate Maui options
     * for extracting keywords from the file(s) in the provided path.
     * @param path Location of the desired file(s)
     * @return The array of Maui options.
     */
    public static String[] getMauiExtractionOptions(String path) {
        String [] options = new String[mauiKeyOptions.length];
        for (int i = 0; i < mauiKeyOptions.length; i++) {
            options[i] = new String(mauiKeyOptions[i]);
        }
        
        options[1] = path;
        return options;
    }
    
    
}
