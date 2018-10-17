package clientencryptedsearch.main;

import clientencryptedsearch.utilities.ClientMetrics;
import clientencryptedsearch.utilities.Config;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessTermSearchResult implements Serializable {

    Socket sock;

    Map<String,Float> sortedTermWeightHashMap = new HashMap<String,Float>();
    HashMap<String,ArrayList<String>> termClusterList = new HashMap<String, ArrayList<String>>();

    public  void sortedTermMap(Map<String,Float> sortedTermHashMap){
        sortedTermWeightHashMap = sortedTermHashMap;
    }



    public void  acceptResultForTermSearch(){
        System.out.println("Waiting for file list from server...");

        int numSearchResults = 0;
        String term;
        DataInputStream dis = null;
        int numberOfFiles=0;
        ArrayList<String> clusterList;
        // Scan for connection

        boolean scanning = true;
        while(scanning) {
            try {
                sock = new Socket(Config.cloudIP, Config.socketPort);
                dis = new DataInputStream(sock.getInputStream());
                numSearchResults = dis.readInt();
                for(int termNumber = 0; termNumber<numSearchResults;termNumber++){
                    term = dis.readUTF();
                    numberOfFiles = dis.readInt();
                    clusterList = new ArrayList<String>();
                    for(int i=0;i<numberOfFiles;i++){
                        String clusterNumber = dis.readUTF();
                        String[] cluterNumberArray = clusterNumber.split("_");
                        clusterList.add(cluterNumberArray[0]);
                        }
                    termClusterList.put(term,clusterList);
                }




                scanning = false;
            }
            catch (IOException ex) {
                System.err.println("Connect failed, waiting and will try again.");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(ClientSearcher.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }

            for(String termPrint:termClusterList.keySet()){
                System.out.println("Search Term: "+termPrint);
                ArrayList<String> tempList = termClusterList.get(termPrint);
                for(int i=0;i<tempList.size();i++){
                System.out.println("Term Find In Cluster Number: " +tempList.get(i));
                }
            }
        }
    }
}
