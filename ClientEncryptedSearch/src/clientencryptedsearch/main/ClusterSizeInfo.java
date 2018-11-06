package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Config;
import clientencryptedsearch.utilities.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClusterSizeInfo {

    private Socket sock;
    HashMap<String,Integer> clusterInfoMap = new HashMap<String, Integer>();

    public void acceptClusterSizeInfo(){
        System.out.println("Waiting for Cluster Size Info from server...");

        DataInputStream dis = null;
        int numberOfFiles=0;

        // Scan for connection

        boolean scanning = true;
        while(scanning) {
            try {
                sock = new Socket(Config.cloudIP, Config.socketPort);
                dis = new DataInputStream(sock.getInputStream());

                numberOfFiles = dis.readInt();

                for(int i=0;i<numberOfFiles;i++){
                    String clusterNumber = dis.readUTF();
                    int totalNumberOfTermInCluster = dis.readInt();
                    clusterInfoMap.put(clusterNumber,totalNumberOfTermInCluster);
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


    public void writeClusterInfo(){
        try {
            // input the file content to the StringBuffer "input"
            File file;

            file =new File(Constants.clusterSizeInfoLocation+ File.separator+ Constants.clusterSizeInfoFileName);
            BufferedWriter bw;
            bw = new BufferedWriter(new FileWriter(file));

            for(String clusterNumber: clusterInfoMap.keySet()){
                bw.write(clusterNumber);
                bw.write("|.|");
                int termNumberIncluster = clusterInfoMap.get(clusterNumber);
                bw.write(String.valueOf(termNumberIncluster));
                bw.write("\n");
            }
                bw.flush();
                bw.close();
        } catch (FileNotFoundException e) {
            System.out.print(this.getClass().getName() + "Can't find Clsuter size Info file in location");
        } catch (IOException e) {
            System.out.print(this.getClass().getName() + " Can't read Clsuter size info  file");
        }
    }


    public  double  getClusterSizeInfo(String clusterNumber){
        int termNumber=0;
        int totalTermNumber = 0;
        double percentage =0;

        try {
            // input the file content to the StringBuffer "input"
            Path path = Paths.get(Constants.clusterSizeInfoLocation+File.separator+Constants.clusterSizeInfoFileName);
            List <String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);

            for(String line:lines){
                String [] stringArray = line.split("\\|.\\|");
                if(stringArray[0].equals(clusterNumber)){
                    termNumber = Integer.parseInt(stringArray[1]); //use this array to extract search Number
                    totalTermNumber +=termNumber;
                }
                else {
                    totalTermNumber +=totalTermNumber;
                }

            }


            //Now Calculate the percentage

            double avgClusterSearch = totalTermNumber/lines.size();
            double gap = termNumber - avgClusterSearch;
             percentage = gap/avgClusterSearch;


        } catch (FileNotFoundException e) {
            System.out.print(this.getClass().getName() + "Can't find search History file in location");
        } catch (IOException e) {
            System.out.print(this.getClass().getName() + " Can't read search History file");
        }





        return  percentage;
    }


}
