/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Config;
import java.util.HashMap;

/**
 * Partitioning Thread.
 * Computes distances from all terms in the index to a cluster.
 * Then puts it into a distances
 * @author Jason
 */
public class PartitionThread implements Runnable {
    
    // One instance is meant to run calculations from one centroid
    // Thus we need access to a centroid.  Access to the index as well.
    private Index index;
    private Centroid centroid;
    // This is an accessor to the distances 
    private HashMap<String, HashMap<Centroid, Float>> distances;
    
    PartitionThread(Index i, Centroid c, HashMap<String, HashMap<Centroid, Float>> d) {
        index = i;
        centroid = c;
        distances = d;
    }
    
    /**
     * Run (AKA, compute distances).
     * Goes through the entire index, computing distances 
     */
    @Override
    public void run() {
        System.out.println("Computing for centroid: " + centroid.term);
        for (String term : index.postingList.keySet()) {
            //If there doesn't already exist a map in this term's slot in the distances map, make one
            if (distances.get(term) == null)
                distances.put(term, new HashMap<Centroid, Float>());
            //Now get the distance for this centroid/term pair
            distances.get(term).put(centroid, computeDistance(centroid, term));
        }
        System.out.println("Finished computing for centroid: " + centroid.term);
    }
    
    /**
     * Compute the distance between the given centroid and term.
     * Uses co-occurrence of files as a measure of distance.
     * NOTE: we don't really need to pass in the centroid since we have access to it.  This is a relic of non-parallelized versions.
     * @param centroid
     * @param term
     * @return 
     */
    private Float computeDistance(Centroid centroid, String term) {
        // Get all the files to go through ( f in set I[T] )
        HashMap<String, Integer> files = index.postingList.get(term);
        //We also need the total sum of times this term has appeared in any file
        int termCount = 0; //This is |I[T]|
        for (String file : files.keySet()) {
            termCount += files.get(file);
        }
        
        //We also need the total sum of times the centroid has appeared in a file
        //NOTE: Not a true measure of how many times the centroid term has appeared, as it may not be an actual value from the postingList
        int centroidCount = 0; //This is |I[Ci]|
        for (String file : centroid.files.keySet()) {
            centroidCount += centroid.files.get(file);
        }
        
        float distance = 0;
        
        for (String file : files.keySet()) {
            float ratioOfFileForTerm; // c(f, I[T]) / |I[T]|
            ratioOfFileForTerm = (float)files.get(file) / (float)termCount; //Gets the count of the term in this file divided by the total number of times that term appeared.
            
            float ratioOfFileForBoth;
            ratioOfFileForBoth = ((float)files.get(file) + (float)centroid.files.getOrDefault(file, 0)) / ((float)termCount + (float)centroidCount);
            
            double logPart = Math.log(ratioOfFileForTerm / ratioOfFileForBoth);
            
            if (!Config.suppressText)
                System.out.println("Centroid: " + centroid.term + ", Term: " + term + ", Ratio: " + ratioOfFileForTerm + ", Log Part: " + ratioOfFileForTerm + " / " + ratioOfFileForBoth);
            
            distance += ratioOfFileForTerm * logPart;
        }
        
        if (!Config.suppressText)
            System.out.println("Centroid: " + centroid.term + ", Term: " + term + ", Distance: " + distance);
        
        return distance;
    }
}
