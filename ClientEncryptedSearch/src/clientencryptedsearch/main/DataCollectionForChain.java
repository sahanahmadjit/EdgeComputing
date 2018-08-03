package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;



public class DataCollectionForChain {


    TransactionMatrixCalculation smallMarkovChainForOneFile = new TransactionMatrixCalculation();

    public boolean StatisticsInfoOfSearch(String folderPath){

        List<String> files = Util.getAbsoluteFilePathsFromFolder(folderPath);

        for(String file:files){
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String previousSearch=null;
                String currentSearch = null;
                while ((currentSearch = br.readLine()) != null) {
                 if(previousSearch==null) {
                     previousSearch = currentSearch;;
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

        smallMarkovChainForOneFile.printAdjacencyList();
        smallMarkovChainForOneFile.transactionMatrixStructureInfo();
        smallMarkovChainForOneFile.printTransactionMatrix();
        return true;
    }



}
