/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.utilities;

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
    public static int socketPort;
    public static int abstractIndexCount;
    public static int k;
    public static int numClusterRepititions;
    public static float k1;
    public  static float b;
    public static int numSearchResults;
    public static int centroidFileMin; // The minimum number of files a term can have to be considered for centroid.
    public static int clusterRatio;
    public static float clusterOverlappingThreshold;
    public static boolean calcMetrics;
    public static boolean suppressText;
    public static boolean overlapClusters;
    
    
    public static void loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(Constants.configLocation + "config.properties"));
            
            socketPort = Integer.parseInt(properties.getProperty("socketPort"));
            abstractIndexCount = Integer.parseInt(properties.getProperty("abstractIndexCount"));
            k = Integer.parseInt(properties.getProperty("k"));
            numClusterRepititions = Integer.parseInt(properties.getProperty("numClusterRepetitions"));
            k1 = Float.parseFloat(properties.getProperty("k1"));
            b = Float.parseFloat(properties.getProperty("b"));
            numSearchResults = Integer.parseInt(properties.getProperty("numSearchResults"));
            centroidFileMin = Integer.parseInt(properties.getProperty("centroidFileMin"));
            clusterRatio = Integer.parseInt(properties.getProperty("clusterRatio"));
            clusterOverlappingThreshold = Float.parseFloat(properties.getProperty("clusterOverlappingThreshold"));
            calcMetrics = ("true").equals(properties.getProperty("calcMetrics"));
            suppressText = ("true").equals(properties.getProperty("suppressText"));
            overlapClusters = ("true").equals(properties.getProperty("overlapClusters"));
        } catch (IOException e) {
            System.err.println("Configuration file not found.\nFrom: Config");
        }
    }
}
