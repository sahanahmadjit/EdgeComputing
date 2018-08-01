/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Config;
import clientencryptedsearch.utilities.Constants;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Retrieve the Abstract Index Partitions from the client.
 * Should be able to connect to the server and retrieve a list of terms to make
 * into the abstract indices (related to clusters on the server).
 * Should be able to write these to a file for later use.
 * @author Jason
 */
class RetrievePartitions {
    private ArrayList<AbstractIndex> abstractIndices;
    
    public RetrievePartitions() {
        abstractIndices = new ArrayList<>();
    }
    
    /**
     * Retrieve the abstract Indices from the server.
     * Opens up a socket connecting to the server and reads in a list
     * of terms one by one.
     */
    public void retrieve() {
        Socket sock = null;
        DataInputStream dis = null;
        
        try {
            // --- Setup the socket
            System.out.println("Attempting to connect to server at " + Config.cloudIP + "...");
            sock = new Socket(Config.cloudIP, Config.socketPort);
            System.out.println("Connection made!");
            dis = new DataInputStream(sock.getInputStream());
            
            // --- Start Reading of indices
            int indexCount; //Number of indices we'll be storying
            indexCount = dis.readInt();
            abstractIndices.ensureCapacity(indexCount);
            
            for (int i = 0; i < indexCount; i++) {
                String name;
                name = dis.readUTF();
                System.out.println("Reading in abstract " + name + "...");
                
                abstractIndices.add(new AbstractIndex(name));
                
                int termsInIndex; //Number of terms in this index we're loading
                termsInIndex = dis.readInt();
                
                for (int j = 0; j < termsInIndex; j++) {
                    String term = dis.readUTF();
                    abstractIndices.get(i).addTerm(term);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(RetrievePartitions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Read all abstracts!  See them below");
        
        for (AbstractIndex ai : abstractIndices)
            System.out.println(ai);
    }
    
    public void writeAbstractsToFile() {
        System.out.println("Attempting to write all abstracts to a file in " + Constants.abstractLocation + "...");
        try {
            for (AbstractIndex ai : abstractIndices) {
                System.out.println("Writing abstract " + ai.name + " to file...");
                ai.writeAbstractToFile();
            }
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error writing abstracts to files...");
        }
        
        System.out.println("All abstracts written to file!");
    }
}
