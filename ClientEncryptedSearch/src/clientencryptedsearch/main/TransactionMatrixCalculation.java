package clientencryptedsearch.main;

import javafx.util.Pair;

import java.util.*;

public class TransactionMatrixCalculation {

    public int TOTAL_FREQUENCY=0;
    public int [][] transactionMatrix;

    public   HashMap<String,HashMap<String,Integer>> adjacencyList;
    public   ArrayList<String> searchedWordArrayList= new ArrayList<String>();
    private  HashMap<String,HashMap<String,Integer>> exitedVertexHasMap= new HashMap<String,HashMap<String,Integer>>();
    private  HashMap<String,Integer> exitedEdgeHasMap = new HashMap<String, Integer>();



    public TransactionMatrixCalculation() {
        adjacencyList = new HashMap<String, HashMap<String, Integer>>();

    }


    public  void addEdge(String startVertex, String endVertex, int weight) {

        //Check the vertex already in the list or not


        if(adjacencyList.containsKey(startVertex)) {
            exitedEdgeHasMap =  adjacencyList.get(startVertex);

            //Check the vertex already contain the target edge
             if(exitedEdgeHasMap.containsKey(endVertex)){
                 int previousWeight = exitedEdgeHasMap.get(endVertex);
               exitedEdgeHasMap.put(endVertex,previousWeight+weight);
             }
             else {
                 exitedEdgeHasMap.put(endVertex,weight);
             }
            adjacencyList.put(startVertex,exitedEdgeHasMap);
        }

        else {
            HashMap<String ,Integer> edgeHashMap= new HashMap();
            edgeHashMap.put(endVertex,weight);
            adjacencyList.put(startVertex,edgeHashMap);
        }
    }


    public  void transactionMatrixStructureInfo(){

     Set vertexSet = adjacencyList.entrySet();
     Iterator it = vertexSet.iterator();
        while (it.hasNext()){
            HashMap<String ,Integer> edgeHashMap = new HashMap<String ,Integer>();

            Map.Entry vertexEntry = (Map.Entry) it.next();
            //Check the term already in the list or not
            if(!searchedWordArrayList.contains(vertexEntry.getKey())){
                searchedWordArrayList.add((String) vertexEntry.getKey());
            }

            edgeHashMap =(HashMap<String, Integer>) vertexEntry.getValue();


            Set edgeSet= edgeHashMap.entrySet();
            Iterator itEdge = edgeSet.iterator();
            while (itEdge.hasNext()){
                Map.Entry edgeEntry = (Map.Entry) itEdge.next();
                //Check the term already in the list or not
                if(!searchedWordArrayList.contains(edgeEntry.getKey())){
                    searchedWordArrayList.add((String)edgeEntry.getKey());
                }
                TOTAL_FREQUENCY +=(int) edgeEntry.getValue();
            }
        }
        System.out.println("Total number of Terms:"+searchedWordArrayList.size());
        System.out.println("Number of Total Searches:"+ TOTAL_FREQUENCY);


        // Initialize Transaction Matrix to avoid null issue
        transactionMatrix = new int[searchedWordArrayList.size()][searchedWordArrayList.size()];
        for(int i=0;i<searchedWordArrayList.size();i++)
            for(int j=0;j<searchedWordArrayList.size();j++)
                transactionMatrix[i][j]=0;

        //Build the Transaction Matrix
        transactionMatrixData(TOTAL_FREQUENCY,searchedWordArrayList,transactionMatrix);


    }

    public void transactionMatrixData(int totalNumberOfSearch, ArrayList<String> searchedWordArrayList,int[][] transactionMatrix){
        int verticalPositionofMatrix, horaizontalPositonofMatrix,searchFrequency;
        Set vertexSet = adjacencyList.entrySet();
        Iterator it = vertexSet.iterator();
        while (it.hasNext()){
            HashMap<String ,Integer> edgeHashMap = new HashMap<String ,Integer>();
            Map.Entry vertexEntry = (Map.Entry) it.next();
            verticalPositionofMatrix = searchedWordArrayList.indexOf(vertexEntry.getKey());
            edgeHashMap =(HashMap<String, Integer>) vertexEntry.getValue();
            Set edgeSet= edgeHashMap.entrySet();
            Iterator itEdge = edgeSet.iterator();
            while (itEdge.hasNext()){
                Map.Entry edgeEntry = (Map.Entry) itEdge.next();
                horaizontalPositonofMatrix = searchedWordArrayList.indexOf(edgeEntry.getKey());
                searchFrequency =(int) edgeEntry.getValue();
                //Mark the entry to the Transaction Matrix
                transactionMatrix[verticalPositionofMatrix][horaizontalPositonofMatrix]=searchFrequency;

                //To get percentage value
              //  transactionMatrix[verticalPositionofMatrix][horaizontalPositonofMatrix]=searchFrequency/TOTAL_FREQUENCY;
            }
        }
    }



    public void printTransactionMatrix(){

        for(int i=0;i<searchedWordArrayList.size();i++){
            //Print the row search term
            System.out.print("\t"+searchedWordArrayList.get(i)+"\t");
        }
        System.out.print("\n");
        for(int i=0;i<searchedWordArrayList.size();i++){
            //Print the column search term
            System.out.print(searchedWordArrayList.get(i));
            for (int j=0;j<searchedWordArrayList.size();j++){
            //Print the frequency now
                    System.out.print("\t"+transactionMatrix[i][j]);
            }
            System.out.print("\n");
        }


    }





    public void printAdjacencyList() {

        Set vertexSet= adjacencyList.entrySet();
        Iterator it = vertexSet.iterator();

        while (it.hasNext()){
            HashMap<String ,Integer> edgeHashMap = new HashMap<String ,Integer>();

            Map.Entry vertexEntry = (Map.Entry) it.next();
            System.out.print(vertexEntry.getKey()+"->");

            edgeHashMap =(HashMap<String, Integer>) vertexEntry.getValue();


            Set edgeSet= edgeHashMap.entrySet();
            Iterator itEdge = edgeSet.iterator();
            while (itEdge.hasNext()){
                Map.Entry edgeEntry = (Map.Entry) itEdge.next();
                System.out.print(edgeEntry.getKey() +"\t |" + edgeEntry.getValue()+"|\t");
            }
            System.out.print("\n");
        }
    }


}
