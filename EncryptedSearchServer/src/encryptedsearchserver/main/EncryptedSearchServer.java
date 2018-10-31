/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Config;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author Jason
 */
public class EncryptedSearchServer {

    Index index;
    
    /**
     * @param args Arguments for what mode the server should launch into
     */
    public static void main(String[] args) {
        
        if (args.length == 0) {
            args = getUserInput();
        }
        
        EncryptedSearchServer ess = new EncryptedSearchServer(args);
    }
    
    public EncryptedSearchServer(String[] args) {
        //Load properties
        Config.loadProperties();
        
        //Switch based on what the user wants to do
        switch(args[0]) {
            case "-u":
                upload(args[1]);
                break;
            case "-s":
                search();
                break;
            case "-p":
                partition();
                break;
            case "-a":
                sendAbstracts();
            case "-c":
                searchTermInCluster();
                break;
            case "-i":
                callculateClusterSizeInfo();
                break;
            default:
                System.out.println("Unsupported operation requested");
        }
    }

    private static String[] getUserInput() {
        String[] args = new String[2];
        System.out.println("Welcome to the Threaded S3C server");
        System.out.print("What would you like to do?  Options: \n"
                + "\tRetrieve Uploaded Documents (store and merge into index) -u\n"
                + "\tRetrieve and perform search query -s\n"
                + "\tPartition Index -p\n"
                + "\tSend Abstracts -a\n"
                +"\tSearch Term In Cluster -c\n"
                +"\tSend Cluster Size Information -i\n"
                + "Choice: ");

        //Get input
        Scanner scan;
        scan = new Scanner(System.in);

        String choice = scan.nextLine();
        args[0] = choice;

        switch (choice) {
            case "-u":
                System.out.println("How are files being uploaded on the client? File -f or network -n");
                args[1] = scan.nextLine();
                break;
            default:
                args[1] = "";
                break;
        }

        return args;
    }

    private void upload(String uploadType) {
        System.out.println("Loading the whole index into memory...");
        index = new Index();
        index.prepareWholeIndex();
        System.out.println();

        RetrieveUploadedFiles retriever = new RetrieveUploadedFiles(index);
        retriever.retrieve(uploadType);

        //Now write this all back to the file
        System.out.println("Writing the doc sizes back to file...");
        index.writeDocSizesToFile();
        System.out.println("Now writing the index back to file...");
        index.writePostingListToIndexFile();
        System.out.println("Done writing!");
    }

    private void search() {
        System.out.println("Loading a blank index into memory.  The clusters will be added at run time.");
        index = new Index();
        //Search indefinitely
        CloudSearcher searcher = new CloudSearcher(index);

        //do {
            System.out.println("\nNow Awaiting Shard Picks... ");
            searcher.ReceiveClusterNames();
            System.out.println("\nNow awaiting Search...");
            searcher.ReceiveQuery();
            //System.out.println(searcher.rankRelatedFiles());
            searcher.rankRelatedFiles();
            searcher.sendResultsToClient();
        //} while (true);
    }

    private void searchTermInCluster(){
        //Take the input once at time then process
        while (true){
            System.out.println("\nNow Awaiting for Term to Search In Cluster");
            SearchTermInCluster clusterSearcher = new SearchTermInCluster();
            clusterSearcher.acceptTermSearchInformationForCluster();
            clusterSearcher.prepareDataForCluster();
            clusterSearcher.sendResultToClient();
        }

    }






    private void partition() {
        System.out.println("Loading the whole index into memory...");
        index = new Index();
        index.prepareWholeIndex();
        System.out.println();
        
        System.out.println("Starting partition attempt...");
        //Create a partitioner object, pass it the index
        Partitioner part = new Partitioner(index);
        //Set how many clusters we'll create
        part.setNumberOfClusters(Config.k);
        //Create the clusters
        part.partition();
        //Create the abstracts
        part.createAbstractIndices();
        
        System.out.println();
        
        //Send said abstract indices to the client
        part.sendAbstractIndicesToClient();
        
        System.out.println();
        
        //Now the server has the clusters and the client has the abstracts
        //We need to write the clusters to files so we remember them
        part.writeClustersToFile();
    }
    
    private void sendAbstracts() {
        System.out.println("Loading the whole index into memory");
        index = new Index();
        index.prepareWholeIndex();
        
        System.out.println("Getting the abstracts from the clusters...");
        
        Partitioner part = new Partitioner(index);
        part.setNumberOfClusters(Config.k);
        part.createAbstractIndicesFromIndex();
        part.sendAbstractIndicesToClient();
    }


    private void callculateClusterSizeInfo(){
        System.out.println("Preparing Cluster Size Information..");
        ClusterSizeInfo clusterSize = new ClusterSizeInfo();
        clusterSize.CalculateTermOnCluster();
    }
}
