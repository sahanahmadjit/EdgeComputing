/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A very basic class that holds the information needed for an abstract index.
 * @author Jason
 */
public class AbstractIndex {
    //a name, a link to the cluster it belongs to.
    public String name;
    //The list of terms
    public ArrayList<String> terms;
    // The score given to it from ranking the query
    public float rankScore;
    
    public AbstractIndex() {
        terms = new ArrayList<>();
    }
    
    public AbstractIndex(String name) {
        terms = new ArrayList<>();
        this.name = name;
    }
    
    public void addTerm(String term) {
        terms.add(term);
    }
    
    /**
     * Decrypt the terms in the abstract.
     * The terms will come in Deterministically encrypted, so we need to decrypt them.
     */
    public void decryptAbstract() {
        
    }
    
    /**
     * Write this abstract index to a file in the specified folder.
     * There is no weight or frequency associated with the abstract.
     * All it needs to do is write the name of it and then enumerate the terms.
     *  Name
     *  Term 1
     *  Term 2
     *  Term 3
     *  ...
     * @throws IOException 
     */
    public void writeAbstractToFile() throws IOException {
        File file = new File(Constants.abstractLocation + File.separator + "abstract_" + name + ".txt");
        
        BufferedWriter bw;
        bw = new BufferedWriter(new FileWriter(file));
        
        //first write the name
        bw.write(name);
        bw.newLine();
        
        for (String term : terms) {
            bw.write(term);
            bw.newLine();
        }
        
        bw.close();
        
    }
    
    /**
     * Reads in the abstract from the given file.
     * The file will be assumed to be located in the abstracts location.
     * The abstract will be read as:
     *  name
     *  term 1
     *  term 2
     *  term 3
     * @param absFilePath file to be read from.  Will add the path to it.
     */
    public void readAbstractFromFile(String absFilePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(absFilePath));
            
            this.name = br.readLine();
            
            String currentline;
            while ((currentline = br.readLine()) != null) {
                this.terms.add(currentline);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AbstractIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AbstractIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String toString() {
        return "Abstract Index: " + name + " has the terms " + terms;
    }
}
