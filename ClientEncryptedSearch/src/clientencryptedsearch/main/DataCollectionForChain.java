package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;



public class DataCollectionForChain {


    MarkovChainImplementation smallMarkovChainForOneFile = new MarkovChainImplementation();
/*
Read the search term form the file.
Search the term in Cluster. If not found then search the nearest closest term to cluster.
 */
    public boolean StatisticsInfoOfSearch(String folderPath){

        List<String> files = Util.getAbsoluteFilePathsFromFolder(folderPath);

        for(String file:files){
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String previousSearch=null;
                String currentSearch = null;
                while ((currentSearch = br.readLine()) != null) {
                    ClientSearcher termSearch = new ClientSearcher(currentSearch);
                    termSearch.constructQuery();
                    termSearch.searchTermInCluster();



                 if(previousSearch==null) {
                     previousSearch = currentSearch;
                     smallMarkovChainForOneFile.addEdge(previousSearch,currentSearch,1);
                 }
                 else{
                     smallMarkovChainForOneFile.addEdge(previousSearch,currentSearch,1);
                 }
                    previousSearch = currentSearch;
                }

            }

            catch (IOException e){
                System.out.println("Can't read the file from batch uploader, file name:"+ file);
            }
        }

        // This line need to be un commented after work
      /*  smallMarkovChainForOneFile.printAdjacencyList();
        smallMarkovChainForOneFile.transactionMatrixStructureInfo();
        smallMarkovChainForOneFile.printTransactionMatrix();
        smallMarkovChainForOneFile.markovImplementation(10);*/
        return true;
    }



}
