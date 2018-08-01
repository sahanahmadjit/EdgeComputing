/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.main;

import java.util.HashMap;

/**
 * Averager Thread.
 * This thread will take in a cluster, and using the distances map, find the
 * average term within the cluster's subIndex.
 * @author Jason
 */
public class AveragerThread implements Runnable {
    
    //One instace runs a calculation for one cluster, meaning we need access to 
    //the cluster (which gives us its centroid) and the distances map.
    private Cluster cluster;
    private HashMap<String, HashMap<Centroid, Float>> distances;
    private String averageTerm;
    
    AveragerThread(Cluster c, HashMap<String, HashMap<Centroid, Float>> d) {
        distances = d;
        cluster = c;
    }
    
    /**
     * Run (AKA, Compute average)
     * Computing the average means (heh) finding the average distance value for
     * terms in its subIndex, then going through the distances and seeing which 
     * term has the closest distance to it.
     */
    @Override
    public void run() {
        System.out.println("Starting averaging for cluster: " + cluster.name);
        float distanceSum = 0;
        // Run through each term in this cluster
        for (String term : cluster.subIndex.keySet()) {
            // add the distance from this cluster's centroid
            if (cluster == null) System.err.println("Distances are null");
            HashMap<Centroid, Float> d = distances.get(term);
            distanceSum += d.getOrDefault(cluster.centroid, 0.0f);
        }
        float average = distanceSum / cluster.subIndex.keySet().size();
        
        // Go through the distances again, finding which one is the closest to the average
        String closestTerm = "";
        float closestDistance = Float.MAX_VALUE;
        for (String term : cluster.subIndex.keySet()) {
            float distance = distances.get(term).getOrDefault(cluster.centroid, 0.0f);
            if (Math.abs(distance - average) < closestDistance) {
                closestDistance = distance;
                closestTerm = term;
            }
        }
        
        averageTerm = closestTerm;
        
        System.out.println("Finished averaging for cluster " + cluster.name + ". New centroid is : " + averageTerm);
        Centroid newCent = new Centroid();
        newCent.term = averageTerm; newCent.files = cluster.subIndex.get(averageTerm);
        cluster.centroid = newCent;
    }
    
    public String getAverageTerm() {
        return averageTerm;
    }
}
