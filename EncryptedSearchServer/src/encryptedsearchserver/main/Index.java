/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Constants;
import encryptedsearchserver.utilities.Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inverted Index and Auxiliary information used to represent documents for searching.
 * Contains the posting list and documents sizes table.
 * Also contains the topic based clusters
 * @author Jason
 */
class Index {

    private static File indexFile = null;
    private static File docSizesFile = null;
    private static File clustersDirectory = null; //The folder for clusters
    //The inverted index.  Maps a term to a mapping of files to frequency counts
    public HashMap<String, HashMap<String, Integer>> postingList = null;
    //Map a document to its word count
    public HashMap<String, Long> documentSizes = null;
    //Map a cluster name to its cluster
    //The clusters then contain their own subIndexes
    public HashMap<String, Cluster> clusters = null;
    
    private String indexFileLocation = Constants.utilitiesLocation + File.separator + Constants.indexFileName;
    private String docSizesFileLocation = Constants.utilitiesLocation + File.separator + Constants.docSizesFileName;
    
    //Signifies if the index has been validly partitioned.
    private boolean validPartitions;
    private boolean dirty;
    
    // A list of what clusters we've put into the posting list.  This is checked so we don't overwrite when we don't need to.
    private HashSet<String> clustersPutIntoPostingList;
    
    public Index() {
        postingList = new HashMap<>();
        clustersPutIntoPostingList = new HashSet<>();
        try {
            //Try to open the files.  If they don't exist, make new ones.
            indexFile = new File(indexFileLocation);
            docSizesFile = new File(docSizesFileLocation);
            
            if (!indexFile.exists()) {
                System.out.println("Index File Not Found.  Creating new Index.txt at " + Constants.utilitiesLocation);
                indexFile.createNewFile();
            }
            if (!docSizesFile.exists()) {
                System.out.println("Document Sizes File Note Found.  Creating new DocSizes.txt at " + Constants.utilitiesLocation);
                docSizesFile.createNewFile();
            }
            
            System.out.printf("Loading Document Sizes File... ");
            prepareDocSizesList();
            System.out.println("Done!");
            
            
        } catch (IOException ex) {
            System.out.println(this.getClass().getName() + ": Unable to create new index files.");
        }
    }
    
    /**
     * Prepare the inverted index and all clusters.
     */
    public void prepareWholeIndex() {
        System.out.print("Loading Inverted Index... ");
        preparePostingList();
        System.out.println("Done!");

        //Now worry about the clusters.
        //Check if there are files in the directory.  We only build clusters if there are.
        List<String> clusterFiles = Util.getAbsoluteFilePathsFromFolder(Constants.clusterLocation);
        if (clusterFiles.size() > 0) { //We have some clusters
            System.out.println("Clusters found.  Reading into clusters...");
            prepareClusters(clusterFiles);
            System.out.println("Done!");
        }
    }
    
    /**
     * A public accessor that just allows an outside user to compile the inverted index.
     */
    public void prepareInvertedIndex() {
        System.out.println("Loading Inverted Index...");
        preparePostingList();
        System.out.println("Done!");
    }
    
    public void prepareAllClusters() {
        //Check if there are files in the directory.  We only build clusters if there are.
        List<String> clusterFiles = Util.getAbsoluteFilePathsFromFolder(Constants.clusterLocation);
        if (clusterFiles.size() > 0) { //We have some clusters
            System.out.println("Clusters found.  Reading into clusters...");
            prepareClusters(clusterFiles);
            System.out.println("Done!");

            for (String clusterName : clusters.keySet())
                System.out.println(clusters.get(clusterName));
        } else {
            System.out.println("No clusters found.  Consider performing a partition.");
        }
    }
    
    /**
     * Loads the given cluster names into memory.
     * NOTE: The names must be in the format "cluster_NAME.txt"
     * This way we don't have to do any additional string processing here.
     * @param clusterFileNames
     */
    public void prepareSelectClusters(List<String> clusterFileNames) {
        System.out.print("\nReading selected clusters into memory...  ");
        prepareClusters(clusterFileNames);
        System.out.println("Done!\n");
    }
    
    /**
     * Puts all of the cluster info that has been loaded for search into the
     * posting list for easy search.
     * This is necessary during search, because the ranking engine has to get some
     * info from the posting list.
     * Post-conditions:
     *  The postingList will be filled with whatever sub-indices have been put in
     *  the clusters.
     */
    public void putClustersInPostingList() {
        System.out.println("\nPutting the selected clusters into the main list...");
        for (String cName : clusters.keySet()) {
            // Make sure the posting list does not yet have this cluster.
            if (!clustersPutIntoPostingList.contains(cName)) 
                postingList.putAll(clusters.get(cName).subIndex);
            else 
                System.out.println("Cluster " + cName + " was already in the main list.");
            clustersPutIntoPostingList.add(cName);
        }
        System.out.println("Done!\n");
    }

    /**
     * Reads the contents of the index file into the posting list.
     * Entries in the text file should be formatted as such:
     *  topic|.|filename|.|freq|.|filename|.|freq
     */
    private void preparePostingList() {
        
        //Read from the index file
        BufferedReader br;
        String[] lineTokens = new String[10];
        String topic = "";
        int i = 1;
        try {
            br = new BufferedReader(new FileReader(indexFile.getAbsolutePath()));
            String currentLine;
            
            while ((currentLine = br.readLine()) != null) {
                lineTokens = currentLine.split(Constants.regexIndexDelimiter);
                topic = lineTokens[0];
                // Get rid of those unwanted terms
                if (topic == "WARC"  || topic == "conversion") {
                    System.out.print(" Got rid of " + topic + "... ");
                    continue;
                }
                
                //Get the file names and frequencies
                for (i = 1; i < lineTokens.length; i += 2) {
                    //Make sure we're not working with some weird case that will kill us
                    if (i+1 >= lineTokens.length) continue;
                    String fileName = lineTokens[i];
                    int frequency = Integer.parseInt(lineTokens[i+1]);
                    //Actually put the data in
                    addToPostingList(topic, fileName, frequency);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println(this.getClass().getName() + ": Index File Note Found!");
        } catch (IOException ex) {
            System.out.println(this.getClass().getName() + ": Error reading index file!");
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println(this.getClass().getName() + ": Array out of bounds!  Size of array = " + lineTokens.length + " with i = " + i + " with topic = " + topic);
        }
    }
    
    /**
     * Add Info To The Posting List.
     * Adds the given file and frequency to the topic.
     * Will make a new hash map if there is none in the place of the topic.
     * @param topic
     * @param fileName
     * @param frequency 
     */
    public void addToPostingList(String topic, String fileName, int frequency) {
        HashMap<String, Integer> files = postingList.get(topic);
        //Check if we need to make a new map.
        if (files == null || files.isEmpty()) {
            files = new HashMap<>();
        }
        
        files.put(fileName, frequency);
        postingList.put(topic, files);
    }

    
    /**
     * Prepare the List of Document Sizes.
     * Goes through DocSizes.txt and puts the file names and their appropriate 
     * sizes into a hash map.
     * 
     * The file should be organized as follows:
     *  DocumentName|.|size
     */
    private void prepareDocSizesList() {
        documentSizes = new HashMap<>();
        BufferedReader br;
        String currentLine;
        
        try {
            //Open the file for reading
            br = new BufferedReader(new FileReader(docSizesFile.getAbsolutePath()));
            
            while ((currentLine = br.readLine()) != null) {
                //Get the file name and length
                String[] tokens = currentLine.split(Constants.regexIndexDelimiter);
                String fileName = tokens[0];
                long wordCount = Long.parseLong(tokens[1]);
                
                addToDocSizes(fileName, wordCount);
            }
            
            br.close();
        } catch (FileNotFoundException ex) {
            System.err.println(this.getClass().getName() + ": DocSizes.txt file not found");
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error Reading DocSizes.txt file");
        }
    }

    /**
     * Add the given data to the document sizes map.
     * @param fileName
     * @param wordCount 
     */
    public void addToDocSizes(String fileName, long wordCount) {
        documentSizes.put(fileName, wordCount);
    }
    
    /**
     * Load in data from the cluster files into our cluster list.
     * Entries in the cluster files should be formatted the same as the postingList.
     * @param clusterFileNames The relative names of the files in the clusters directory
     */
    public void prepareClusters(List<String> clusterFileNames) {
        clusters = new HashMap<>();
        
        //Read from the files, one at a time
        BufferedReader br;
        try {
            for (String fileName : clusterFileNames) {
                //Setup to read a single file.
                //Important to note that we don't know which file til we read the first line and find the name.
                br = new BufferedReader(new FileReader(fileName));
                //Gotta create the new cluster to store that new data
                Cluster cluster = new Cluster();
                
                String name;
                name = br.readLine();
                cluster.name = name;
                
                String currentLine;
                
                while ((currentLine = br.readLine()) != null) {
                    String[] lineTokens = currentLine.split(Constants.regexIndexDelimiter);
                    String topic = lineTokens[0];
                
                    //Get the file names and frequencies
                    for (int i = 1; i < lineTokens.length; i += 2) {
                        String file = lineTokens[i];
                        int frequency = Integer.parseInt(lineTokens[i+1]);
                        //Actually put the data in
                        addToCluster(cluster, topic, file, frequency);
                    }
                }
                
                //At this point the cluster should be all set up.  Now put it in the map.
                clusters.put(name, cluster);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Add data to a cluster.
     * Pretty much the same as adding to the posting list.  Just you need to 
     * specify the cluster
     * @param cluster
     * @param topic
     * @param fileName
     * @param frequency 
     */
    public void addToCluster(Cluster cluster, String topic, String fileName, int frequency) {
        HashMap<String, Integer> files = cluster.subIndex.get(topic);
        //Check if we need to make a new map.
        if (files == null || files.isEmpty()) {
            files = new HashMap<>();
        }
        
        files.put(fileName, frequency);
        cluster.subIndex.put(topic, files);
    }
    
    
    // ---------- WRITING TO FILES ------------
    
    /**
     * Writes the contents of the posting list back to the original index file.
     * Should write in the same style that it was read from.
     */
    public void writePostingListToIndexFile() {
        BufferedWriter bw;
        
        try {
            bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream(indexFile)));
            
            //Iterate through all keys (topics, writing all of their values to the file.
            for (String topic : postingList.keySet()) {
                //Use string builder to save space
                StringBuilder lineSB = new StringBuilder();
                lineSB.append(topic);
                
                //Get and add all files and frequencies associated with that topic
                HashMap<String, Integer> files = postingList.get(topic);
                for (String file : files.keySet()) {
                    lineSB.append(Constants.indexDelimiter).append(file).append(Constants.indexDelimiter).append(files.get(file));
                }
                
                bw.write(lineSB.toString());
                bw.newLine();
            }
            
            bw.close();
        } catch (FileNotFoundException ex) {
            System.err.println(this.getClass().getName() + ": Index.txt file now found");
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error writing to Index.txt");
        }
    }
    
    /**
     * Writes the contents of the docSizes map back to the original DocSizes file.
     * Should write in the same style that it was read from.
     */
    public void writeDocSizesToFile() {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream(docSizesFile)));
            
            //Iterate through all files
            for (String file : documentSizes.keySet()) {
                String line = file + Constants.indexDelimiter + documentSizes.get(file);
                bw.write(line);
                bw.newLine();
            }
            
            bw.close();
        } catch (FileNotFoundException ex) {
            System.err.println(this.getClass().getName() + ": DocSizes.txt file now found");
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error writing to DocSizes.txt");
        }
    }
    
    @Override
    public String toString() {
        return "IndexFile{" + "postingList=" + postingList + '}' + "\nDoc Sizes={" + documentSizes + ')';
    }
}
