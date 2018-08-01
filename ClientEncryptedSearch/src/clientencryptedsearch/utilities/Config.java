/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration Info.
 * Simple static class that should just load in desired config info.
 * @author jason
 */
public class Config {
    public static boolean splitKeywords; //Split key phrases in the upload process
    public static boolean countFrequencies; //Count frequencies or leave them at 1 in the upload process
    public static boolean encryptKeyPhrases; //Do the deterministic encryption on key files or not
    public static boolean uploadSideSemantics; //Get semantic terms on the upload or not
    public static boolean subdivideQuery; //Split the query into its adjacent subsets
    public static boolean encryptQuery; //Encrypt the query when searching.  Should be the same value as encrypt key phrases
    public static boolean calcMetrics; //Calculate performance metrics at the appropriate times
    public static boolean uploadTxts; //Upload the txt files with the key files
    public static boolean writeClusterChoices; //Write the names of the clusters we'll search
    public static String cloudIP; //IP address for the cloud for upload or search
    public static String dataSize; //The amount of data in megs we're searching over.
    public static String s; //The value of s for the current search.
    public static String fileTransferType; //How files should be transferred: local or networked
    public static int socketPort; //Port to open up a socket on
    public static int numSearchedAbstracts; // The number of abstracts we're going to search over
    
    public static void loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(Constants.configLocation + "config.properties"));
            
            splitKeywords = ("true".equals(properties.getProperty("splitKeywords")));
            countFrequencies = ("true".equals(properties.getProperty("countFrequencies")));
            encryptKeyPhrases = ("true".equals(properties.getProperty("encryptKeyPhrases")));
            uploadSideSemantics = ("true".equals(properties.getProperty("uploadSideSemantics")));
            subdivideQuery = ("true".equals(properties.getProperty("subdivideQuery")));
            calcMetrics = ("true".equals(properties.getProperty("subdivideQuery")));
            uploadTxts = ("true".equals(properties.getProperty("uploadTxts")));
            writeClusterChoices = ("true".equals(properties.getProperty("writeClusterChoices")));
            cloudIP = properties.getProperty("cloudIP");
            dataSize = properties.getProperty("dataSize");
            s = properties.getProperty("s");
            fileTransferType = properties.getProperty("fileTransferType");
            encryptQuery = encryptKeyPhrases;
            socketPort = Integer.parseInt(properties.getProperty("socketPort"));
            numSearchedAbstracts = Integer.parseInt(properties.getProperty("numSearchedAbstracts"));
        } catch (IOException e) {
            System.err.println("Config: " + e.getMessage());
        }
    }
}
