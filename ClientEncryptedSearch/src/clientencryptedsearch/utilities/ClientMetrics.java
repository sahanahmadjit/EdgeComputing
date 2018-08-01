/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class full of static methods meant to write various performance and
 * overhead metrics throughout.
 * Can calculate:
 *  Local storage amount
 *  Overall Search Time
 *  Query Processing Time
 * 
 * Will write all of these metrics to metrics.txt file.
 */
public class ClientMetrics {
    
    /**
     * Write the time it took to search to the metrics file.
     * Measures From the time the query is entered to the time the results are given.
     * 
     * Appends to the file without regard to repeats.  
     * 
     * Writes in the following style:
     *  ModelName-SearchTime-TimeInSeconds
     * @param milliseconds Seconds it took to search
     */
    public static void writeSearchTime(long milliseconds, String query) {
        System.out.println("Writing the search time to the metrics file");
        File file = new File(Constants.metricsFileName);
        
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)))) {
            out.println(Config.s + "-" + Config.dataSize + "-Search Time-" + query + "-" +  milliseconds);
        } catch(IOException e) {
            e.printStackTrace();
            System.err.println("Error writing to metrics file");
        }
    }
    
    /**
     * Write the time it took to rank abstracts to the metrics file.
     * @param milliseconds
     * @param query 
     */
    public static void writeAbstractTime(long milliseconds, String query) {
        System.out.println("Writing the time to rank abstracts to the metrics file.");
        File file = new File(Constants.metricsFileName);
        
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)))) {
            out.println(Config.s + "-" + Config.dataSize + "-Abstracts Time-" + query + "-" +  milliseconds);
        } catch(IOException e) {
            e.printStackTrace();
            System.err.println("Error writing to metrics file");
        }
    }
    
    /**
     * Write the time it took to rank abstracts to the metrics file.
     * @param milliseconds
     * @param query 
     */
    public static void writeCloudTime(long milliseconds, String query) {
        System.out.println("Writing the time the cloud took to the metrics file.");
        File file = new File(Constants.metricsFileName);
        
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)))) {
            out.println(Config.s + "-" + Config.dataSize + "-Cloud Time-" + query + "-" +  milliseconds);
        } catch(IOException e) {
            e.printStackTrace();
            System.err.println("Error writing to metrics file");
        }
    }
    
    /**
     * Write time it took to process the query to the metrics file.
     * Measures from the time the user enters the query to the time it's done
     * processing.
     * 
     * Appends to the file.  Repeats are intentional, as they will be averaged.
     * 
     * Writes in the following style:
     *  ModelName-QueryTime-Query-TimeValue
     * @param milliseconds
     * @param query
     */
    public static void writeQueryTime(long milliseconds, String query ) {
        System.out.println("Writing the query processing time to the metrics file");
        File file = new File(Constants.metricsFileName);
        
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)))) {
            out.println(Config.s + "-" + Config.dataSize + "-Query Time-" + query + "-" + milliseconds);
        } catch(IOException e) {
            System.err.println("Error writing to metrics file");
        }
    }
    
    /**
     * Write the names for the clusters picked by a search.
     * This is to analyze the cluster sizes later on.
     * File is named after the data size and cluster numbers.
     * @param abstractNames 
     */
    public static void writeClusterChoice(ArrayList<String> abstractNames, String query) {
        System.out.println("Writing the choices of clusters to a file.");
        File file = new File(Constants.clusterChoiceFileName + "-" + Config.dataSize + "-" + Config.s + ".txt");
        
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ClientMetrics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)))) {
            out.println(query);
            for (String name : abstractNames) 
                out.println(name);
        } catch(IOException e) {
            System.err.println("Error writing to clusters choice file");
        }
    }
}
