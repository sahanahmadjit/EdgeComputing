package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;
import clientencryptedsearch.utilities.Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class DataCollectionForChain {


    List<String> listOfWord = new LinkedList<String >();

    public boolean StatisticsInfoOfSearch(String folderPath){

        List<String> files = Util.getAbsoluteFilePathsFromFolder(folderPath);

        for(String file:files){
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String previousSearch=null;
                String currentSearch = null;
                while ((currentSearch = br.readLine()) != null) {
                    listOfWord.add(line);
                    System.out.println(line);
                }
            }

            catch (IOException e){
                System.out.println("Can't read the file from batch uploader, file name:"+ file);
            }
        }


        return true;
    }


}
