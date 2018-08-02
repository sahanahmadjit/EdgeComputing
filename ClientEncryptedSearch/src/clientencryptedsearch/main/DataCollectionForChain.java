package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;
import clientencryptedsearch.utilities.Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


public class DataCollectionForChain {

    public static final int NUMBER_OF_WORDS_IN_FILE = 2;
    Vector<String> listOfWord = new Vector<String>();
    Vector<Integer> cost  = new Vector<Integer>();

    AdjacencyList smallMarkovChainForOneFile = new AdjacencyList(NUMBER_OF_WORDS_IN_FILE);

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
        return true;
    }


}
