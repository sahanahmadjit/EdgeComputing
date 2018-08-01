/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Jason
 */
public class StopwordsRemover {
    
    private HashSet<String> stopSet;
    
    /**Constructor for the stopwords remover
     * Fills the internal stopwords set with the stopwords from the given text file.
     * The stopwords file must be a text document with a single word on each line.
     * 
     * Example Stopwords files we use:
     *  stopwords_en.txt
     *  wiki_stopwords_en.txt
     * @param filename Name of the file to load stopwords from.
     */
    public StopwordsRemover(String filename) {
        File stopwordsFile = new File(Constants.stopwordsLocation + File.separator + filename);
        stopSet = new HashSet<String>();
        
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader( new FileInputStream(stopwordsFile)));
            
            String line;
            while ((line = br.readLine()) != null) {
                //Assume each line has 1 word on it
                stopSet.add(line.toLowerCase());
            }
            
            br.close();
        } catch (IOException e) {
            System.err.println("Error reading stopwords");
        }
    }
    
    /**
     * Remove all of the loaded stopwords from the given collection.
     * Case-insensitive, as all loaded stopwords should be lowercase.
     * @param collection The collection to remove stopwords from.
     */
    public void remove(Collection<String> collection) {
        String[] strs = new String[collection.size()];
        collection.toArray(strs);
        for (String str : strs) {
            if (stopSet.contains(str.toLowerCase()))
                collection.remove(str);
        }
    }
    
    /**
     * Remove all of the loaded stopwords from the given array.
     * @param array
     */
    public String[] remove(String[] array) {
        ArrayList<String> ar = new ArrayList<>();
        for (String str : array) {
            if (!stopSet.contains(str.toLowerCase()))
                ar.add(str);
        }
        System.out.println(Arrays.toString(ar.toArray()));
        array = new String[ar.size()];
        for (int i = 0; i < ar.size(); i++)
            array[i] = ar.get(i);
        
        return array;
    }
}
