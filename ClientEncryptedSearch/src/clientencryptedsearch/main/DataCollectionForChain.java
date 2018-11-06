package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;
import clientencryptedsearch.utilities.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;



public class DataCollectionForChain {





    MarkovChainImplementation markovChainforOneCluster = new MarkovChainImplementation();
/*
Read the search term form the file.
Search the term in Cluster. If not found then search the nearest closest term to cluster.
 */


    public boolean StatisticsInfoOfSearch(int numberOfMarkovSteps){

        List<String> files = Util.getAbsoluteFilePathsFromFolder(Constants.markovDataFileLocation);

        for(String file:files){
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String previousSearch=null;
                String currentSearch = null;
                while ((currentSearch = br.readLine()) != null) {
                    if(previousSearch==null) {
                     previousSearch = currentSearch;
                     markovChainforOneCluster.addEdge(previousSearch,currentSearch,1);
                 }
                 else{
                     markovChainforOneCluster.addEdge(previousSearch,currentSearch,1);
                 }
                    previousSearch = currentSearch;
                }

            }

            catch (IOException e){
                System.out.println("Can't read the file from markov data folder, file name:"+ file + e.getClass().getName());
                e.printStackTrace();
            }

            markovChainforOneCluster.printAdjacencyList();
            markovChainforOneCluster.transactionMatrixStructureInfo();
            markovChainforOneCluster.printTransactionMatrix();
            markovChainforOneCluster.markovImplementation(numberOfMarkovSteps);

            // Parse the File name

            String[] fullPath = file.split("/");
            String clusterFileName = fullPath[fullPath.length-1];

            markovChainforOneCluster.writeSortedTermForAbstract(clusterFileName);
        }



        return true;
    }



}
