package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Config;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchTermInCluster {

    private ServerSocket serv;
    private Socket sock;
    private ArrayList<String> searchedClusterNames;
    private HashMap<String,Float> termToSearchInClsuter = new HashMap<String, Float>();

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
               termToSearchInClsuter.put(term,weight);
            }

            System.out.print(termToSearchInClsuter.size());
            dis.close();
            sock.close();
            serv.close();
        } catch(IOException e) {
            System.err.println(CloudSearcher.class.getName() + "Error getting input from client.");
        }
    }

}
