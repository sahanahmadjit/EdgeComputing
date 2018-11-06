package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Config;
import encryptedsearchserver.utilities.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClusterSizeInfo {
    private ServerSocket serv;
    private Socket sock;

    HashMap<String,Integer> clusterInfoMap = new HashMap<String, Integer>();

    public  void CalculateTermOnCluster() {
        File folder = new File(Constants.clusterLocation);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            String clusterFile = file.getName();
            String [] clusterFileArray = clusterFile.split("_");
            clusterFile = clusterFileArray[1];
            clusterFile = clusterFile.replaceAll(".txt","");

            if (file.isFile()) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file));
                    int lines = 0;
                    while (reader.readLine() != null) lines++;
                    reader.close();

                    clusterInfoMap.put(clusterFile,lines);
                } catch (FileNotFoundException e) {
                    System.out.println(this.getClass().getName() + ": Unable to Find Cluster File.");
                } catch (IOException e) {
                    System.out.println(this.getClass().getName() + ": Unable to Read Cluster File.");
                }

            }
        }

        for(String fileName: clusterInfoMap.keySet())
            System.out.println("Total Term In Cluster: " + fileName +" "+ (clusterInfoMap.get(fileName)-1));


    }


    public void sendClusterInfo(){
        System.out.println("\nSending search results to client...");
        // Uses the same connection from before.  Maybe bad?
        try {
            serv = new ServerSocket(Config.socketPort);
            sock = serv.accept();
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

            int numSearchResults = clusterInfoMap.size();
            dos.writeInt(numSearchResults);


            for(String clusterFileName: clusterInfoMap.keySet()){
                dos.writeUTF(clusterFileName);
                int totalNumberOfTerm = (clusterInfoMap.get(clusterFileName));
                dos.writeInt(totalNumberOfTerm);
            }
            dos.close();
            sock.close();
            serv.close();
        } catch (IOException ex) {
            Logger.getLogger(CloudSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }

    }



}
