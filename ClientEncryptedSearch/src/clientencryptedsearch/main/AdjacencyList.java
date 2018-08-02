package clientencryptedsearch.main;

import javafx.util.Pair;

import java.util.*;

public class AdjacencyList {

    private  final HashMap<String,HashMap<String,Integer>> adjacencyList;
    private  HashMap<String,HashMap<String,Integer>> exitedVertexHasMap= new HashMap<String,HashMap<String,Integer>>();
    private  HashMap<String,Integer> exitedEdgeHasMap = new HashMap<String, Integer>();

    public AdjacencyList(int vertices) {
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
