package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Constants;

import java.io.*;
import java.util.HashMap;

public class ClusterSizeInfo {


    HashMap<String,Integer> clusterInfoMap = new HashMap<String, Integer>();

    public  void CalculateTermOnCluster() {
        File folder = new File(Constants.clusterLocation);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            String clusterFile = file.getName();

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
            System.out.println(fileName + " Total Term In Cluster: " + (clusterInfoMap.get(fileName)-1));


    }


    public void sendClusterInfo(){

    }



}
