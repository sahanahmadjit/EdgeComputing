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
    HashMap<String,ArrayList<String>>termClusterList = new HashMap<String,ArrayList<String>>();
    Map <String,Float> termWeightMap = new HashMap<String, Float>();

    public  void sortedTermMap(Map<String,Float> sortedTermHashMap){
        sortedTermWeightHashMap = sortedTermHashMap;
    }



    public void  acceptResultForTermSearch(){
        System.out.println("Waiting for file list from server...");

        int numSearchResults = 0;
        String term;
        String MAX_TERM;
        Float weight;
        DataInputStream dis = null;
        int numberOfFiles=0;
        ArrayList<String> clusterList;
        // Scan for connection
        double MAX_VALUE=0.000000;
        boolean scanning = true;
        while(scanning) {
            try {
                sock = new Socket(Config.cloudIP, Config.socketPort);
                dis = new DataInputStream(sock.getInputStream());
                numSearchResults = dis.readInt();
                for(int termNumber = 0; termNumber<numSearchResults;termNumber++){
                    term = dis.readUTF();
                    weight = dis.readFloat();
                    System.out.print("Term:" + term + " weight:"+weight +" ");
                    numberOfFiles = dis.readInt();
                    clusterList = new ArrayList<String>();
                    for(int i=0;i<numberOfFiles;i++){
                        String clusterNumber = dis.readUTF();
                        String[] cluterNumberArray = clusterNumber.split("_");
                        System.out.print(" Cluster Number:"+ cluterNumberArray[0] +" ");
                        clusterList.add(cluterNumberArray[0]);
                        }
                        termClusterList.put(term,clusterList);
                        termWeightMap.put(term,weight);
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
        }
    }

    public void heightWeithTermInCluster(){

        Map<String,Float> bestTermByWeight = new HashMap<String,Float>();
        bestTermByWeight = ValueSortHashMap.sortHashMap(termWeightMap,false);//ASE

        System.out.print("\n");
        System.out.println("---- Best Match-----");
        Map.Entry<String, Float> entry = bestTermByWeight.entrySet().iterator().next();
        System.out.print("Term: "+ entry.getKey() +" Weight: " + entry.getValue());
        System.out.print(" Cluster File Number");
        ArrayList<String> temp = new ArrayList<String>();
        temp = termClusterList.get(entry.getKey());

        for (String clusterFileNumber: temp)
            System.out.print(" "+clusterFileNumber);


    }


}
