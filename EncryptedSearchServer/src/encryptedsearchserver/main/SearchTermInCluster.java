package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Config;
import encryptedsearchserver.utilities.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Boolean.TRUE;

public class SearchTermInCluster implements  Serializable{

    private ServerSocket serv;
    private Socket sock;
    private ArrayList<String> searchedClusterNames;
    private ArrayList<String> termToSearchInClsuter = new ArrayList<String>();
    public HashMap<String,ArrayList<String>> searchResultMap;
    public  HashMap<String,Float> receiveDataMap = new HashMap<String, Float>();



    public void  acceptTermSearchInformationForCluster(){

        searchedClusterNames = new ArrayList<String>();

        // Open up the sockets and get the number of and names of shards
        try {
            serv = new ServerSocket(Config.socketPort);
            sock = serv.accept();
            sock.setKeepAlive(true);
            sock.setSoTimeout(10000);
        } catch (IOException ex) {
            System.err.println(CloudSearcher.class.getName() + ": Error opening port");
        }

        try {
            DataInputStream dis = new DataInputStream(sock.getInputStream());
            int numShards = dis.readInt();

            for (int i = 0; i < numShards; i++) {
                String clusterNumber = dis.readUTF();
                searchedClusterNames.add(clusterNumber);

            }

            int numOfTerms = dis.readInt();

            for(int i=0;i<numOfTerms;i++){
               String term = dis.readUTF();
               Float weight = dis.readFloat();
               termToSearchInClsuter.add(term);
               receiveDataMap.put(term,weight);
            }

            System.out.print("Number Of Total Term Will be Searched In Cluster:"+termToSearchInClsuter.size());
            dis.close();
            sock.close();
            serv.close();
        } catch(IOException e) {
            System.err.println(CloudSearcher.class.getName() + "Error getting input from client.");
        }


    }


    // Makes calls to load the clusters the client picked into memory.
    public void prepareDataForCluster() {
        List<String> clusterFileNames = new ArrayList<String>();
        searchResultMap = new HashMap<String, ArrayList<String>>();

        int trackPosition=0;
        int trackTermPositon = 0;
        String clusterFileName;

        for(String term:termToSearchInClsuter){

            for(;trackPosition<searchedClusterNames.size();trackPosition++){
                if(searchedClusterNames.get(trackPosition).equals("|")){
                    trackPosition++;
                    break;
                }
                else {
                    clusterFileName = Constants.clusterLocation + File.separator + "cluster_" + searchedClusterNames.get(trackPosition) + ".txt";
                    if(searchTermInCluster(clusterFileName,term)){
                        ArrayList<String> clusterList = new ArrayList<String>();
                        if(searchResultMap.containsKey(term)){

                        clusterList = searchResultMap.get(term);
                        clusterList.add(searchedClusterNames.get(trackPosition));
                        searchResultMap.put(term,clusterList);
                        }

                        else {
                            clusterList.add(searchedClusterNames.get(trackPosition));
                            searchResultMap.put(term,clusterList);
                        }

                        break; //One term can't be appear in two cluster
                    }


                }
            }

        }

    }





    public boolean searchTermInCluster(String clusterFileName, String term){
            boolean matchfind=false;
            File file = new File(clusterFileName);
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String st;
                while ((st = br.readLine()) != null) {

                    String [] spiltWords =  st.split("\\|.\\|");
                    if(spiltWords[0].equals(term)){
                        System.out.println("\nMatch Find on Cluster File!");
                  //      System.out.println(st);
                        System.out.println("Term: " + term + " clusterFile: "+ clusterFileName);
                        matchfind = true;
                        break;
                        //return matchfind;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return  matchfind;
    }


    public void sendResultToClient(){
        System.out.println("\nSending search results to client...");
        // Uses the same connection from before.  Maybe bad?
        try {
            serv = new ServerSocket(Config.socketPort);
            sock = serv.accept();
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

            int numSearchResults = Math.min(Config.numSearchResults, searchResultMap.size());
            dos.writeInt(numSearchResults);

            for(String term: searchResultMap.keySet()) {

                Float weight = receiveDataMap.get(term);
                dos.writeUTF(term);
                dos.writeFloat(weight);
                //The ArrayList that contain cluster number for any term
                ArrayList<String> clusterNumberList = new ArrayList<String>();
                clusterNumberList = searchResultMap.get(term);
                //Send Number of Cluster file for each term
                dos.writeInt(clusterNumberList.size());
                for(int i=0;i<clusterNumberList.size();i++) {
                dos.writeUTF(clusterNumberList.get(i));

                }

            }


            if(searchResultMap.size()==0){ //if no match find on the cluster
                System.out.println("No Match Find On Cluster");
            }
            // If we're taking metrics, send the time info back to the client.
            dos.close();
            sock.close();
            serv.close();
        } catch (IOException ex) {
            Logger.getLogger(CloudSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
