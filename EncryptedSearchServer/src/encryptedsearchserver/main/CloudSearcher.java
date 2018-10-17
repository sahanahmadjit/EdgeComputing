/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Config;
import encryptedsearchserver.utilities.Constants;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Receive search queries, initiate ranking, and send results to client.
 * @author Jason
 */
public class CloudSearcher {
    private Index index;
    private HashMap<String, Float> query;
    private ArrayList<String> searchedClusterNames;
    private ArrayList<String> searchResults;
    private ServerSocket serv;
    private Socket sock;
    private long searchTime;
    
    //Uhhhh....
    public CloudSearcher(Index i) {
        query = new HashMap<>();
        searchedClusterNames = new ArrayList<>();
        index = i;
    }
    
    /**
     * Receive the names of the shards and start loading them.
     * Post-conditions:
     *  searchedClusterNames will be filled with the clusters we need to search
     * After this, the system is set to load the given clusters into memory
     */
    public void ReceiveClusterNames() {
        // Open up the sockets and get the number of and names of shards
        try {
            serv = new ServerSocket(Config.socketPort);
            sock = serv.accept();
            sock.setKeepAlive(true);
            sock.setSoTimeout(10000);
        } catch (IOException ex) {
            System.err.println(CloudSearcher.class.getName() + ": Error opening port");
        }
        
        try {
            DataInputStream dis = new DataInputStream(sock.getInputStream());
            int numShards = dis.readInt();
            
            for (int i = 0; i < numShards; i++) {
                String name = dis.readUTF();
                searchedClusterNames.add(name);
            }
            
            dis.close();
            sock.close();
            serv.close();
        } catch(IOException e) {
            System.err.println(CloudSearcher.class.getName() + "Error getting input from client.");
        }
        
        System.out.println("Shards we'll be searching: " + searchedClusterNames);
        
        prepAndLoadClusters();
        
        //for (String s : searchedClusterNames) 
          //  System.out.println(index.clusters.get(s));
        
        
    }
    
    // Makes calls to load the clusters the client picked into memory.
    private void prepAndLoadClusters() {
        List<String> clusterFileNames = new ArrayList<String>();
        
        for (String name : searchedClusterNames)
            clusterFileNames.add(Constants.clusterLocation + File.separator + "cluster_" + name + ".txt");
        
        index.prepareSelectClusters(clusterFileNames); //This call just loads the clusters into cluster maps
        index.putClustersInPostingList(); //This call puts the cluster info into the posting list so it can be checked during search
    }
    
    /**
     * Receive the query info as sent from the client.
     * Post-conditions:
     *  query will be filled with the query and weight data
     * After this, the system is set to perform ranking.
     */
    public void ReceiveQuery() {
        //Open up the sockets and let the data flow in.
        try {
            serv = new ServerSocket(Config.socketPort);
            sock = serv.accept();
            sock.setKeepAlive(true);
            sock.setSoTimeout(10000);
        } catch (IOException ex) {
            System.err.println(CloudSearcher.class.getName() + ": Error opening port");
        }
        
        try {
            DataInputStream dis = new DataInputStream(sock.getInputStream());
            
            int numTerms = dis.readInt();
            
            for (int i = 0; i < numTerms; i++) {
                String term = dis.readUTF();
                float weight = dis.readFloat();
                query.put(term, weight);
            }
            
            dis.close();
            sock.close();
            serv.close();
        } catch (IOException ex) {
            System.err.println(CloudSearcher.class.getName() + "Error getting query from client.");
        }
        
        //Now the query holds the Q' with its weights
        
        //System.out.println("Query: " + query);
        
    }
    
    public ArrayList<String> rankRelatedFiles() {
        // Start measuring time to do ranking
        long begin = System.currentTimeMillis();
        
        // Get all files that could score more than 0.
        HashMap<String, Integer> relatedFiles = findRelatedFiles();
        
        RankingEngine rank = new RankingEngine(index);
        searchResults = rank.ScoreAllDocuments(relatedFiles, query);
        
        long end = System.currentTimeMillis();
        searchTime = end - begin;
        
        return searchResults;
    }
    
    // Checks through the related clusters the client sent and find which
    // files specifically mention any of the query words.  Also gets they're
    // frequency data
    private HashMap<String, Integer> findRelatedFiles() {
        HashMap<String, Integer> relatedFiles = new HashMap<>();
        
        //Iterate through the clusters and check the queries against them.
        for (String clusterName : searchedClusterNames) {
            Cluster cluster = index.clusters.get(clusterName);
            
            for (String queryTerm : query.keySet()) {
                if (cluster.subIndex.containsKey(queryTerm))
                    relatedFiles.putAll(cluster.subIndex.get(queryTerm));
            }
        }
        
        // Empty the list of clusters to avoid buildup with future searches.
        searchedClusterNames.clear();
        return relatedFiles;
    }
    
    // Checks through the related clusters the client sent and uses the query
    // terms to find which files specifically mention any of the query words.
    // Returns a map that maps clusters to the files and frequencies mapping.
    private HashMap<Cluster, HashMap<String, Integer>> getRelatedFiles() {
        // This maps a cluster to the files within it.
        // This way we don't have to have 
        HashMap<Cluster, HashMap<String, Integer>> relatedFiles = new HashMap<>();
        return relatedFiles;
    }
    
    
    public void sendResultsToClient() {
        System.out.println("\nSending search results to client...");
        // Uses the same connection from before.  Maybe bad?
        try {
            serv = new ServerSocket(Config.socketPort);
            sock = serv.accept();
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            
            int numSearchResults = Math.min(Config.numSearchResults, searchResults.size());
            dos.writeInt(numSearchResults);
            
            for (int i = 0; i < numSearchResults; i++) {
                dos.writeUTF(searchResults.get(i));
            }
            
            // If we're taking metrics, send the time info back to the client.
            if (Config.calcMetrics)
                dos.writeLong(searchTime);
            
            dos.close();
            sock.close();
            serv.close();
        } catch (IOException ex) {
            Logger.getLogger(CloudSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}



