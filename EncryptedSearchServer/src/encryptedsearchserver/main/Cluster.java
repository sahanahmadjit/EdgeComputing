/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Constants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * A bean that represents a cluster during the partition process.
 * Helps to identify clusters and write them back to files.
 * @author Jason
 */
public class Cluster {
    public String name;
    //The sub index associated with this cluster
    HashMap<String, HashMap<String, Integer>> subIndex;
    //The centroid (average) for this cluster.
    Centroid centroid;
    
    public Cluster() {
        subIndex = new HashMap<>();
    }
    
    /**
     * Writes this cluster to a file in the specified clusters location.
     * Specifically, the clusters subIndex will be written to a file.
     * The only other data that will be written to it is the "name" which links
     * it to its abstract.
     * This is important, as this is how we'll store the clusters on startup.
     * Essentially, the file should look like this
     *  name
     *  topic|.|term|.|freq|.|term|.|freq
     *  topic|.|term|.|freq
     * 
     * @param name What this cluster should be called (after "cluster")
     */
    public void writeClusterToFile(String name) throws IOException {
        //Create the cluster file
        File clusterFile = new File(Constants.clusterLocation + File.separator + "cluster_" + name + ".txt");
        if (!clusterFile.exists()) {
            clusterFile.createNewFile();
        }
        
        //Setup to write to it
        BufferedWriter bw;
        bw = new BufferedWriter(new FileWriter(clusterFile));
        
        //First write the name
        bw.write(name);
        bw.newLine();
        //Then write all of the sub index info
        for (String topic : subIndex.keySet()) {
            //Use string builder to save space
                StringBuilder lineSB = new StringBuilder();
                lineSB.append(topic);
                
                //Get and add all files and frequencies associated with that topic
                HashMap<String, Integer> files = subIndex.get(topic);
                for (String file : files.keySet()) {
                    lineSB.append(Constants.indexDelimiter).append(file).append(Constants.indexDelimiter).append(files.get(file));
                }
                
                bw.write(lineSB.toString());
                bw.newLine();
        }
        
        bw.close();
    }
    
    @Override
    public String toString() {
        return "Cluster " + name + " contains sub index: " + subIndex;
    }
}
