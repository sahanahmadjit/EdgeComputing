package clientencryptedsearch.main;



import java.util.*;

public class MarkovChainImplementation {

    public int TOTAL_FREQUENCY=0;
    public int [][] transactionMatrixData;
    public double [] transactionMatrixVector;
    public double [][] transactionMatrix;

    public   HashMap<String,HashMap<String,Integer>> adjacencyList;
    public   ArrayList<String> searchedWordArrayList= new ArrayList<String>();
    private  HashMap<String,HashMap<String,Integer>> exitedVertexHasMap= new HashMap<String,HashMap<String,Integer>>();
    private  HashMap<String,Integer> exitedEdgeHasMap = new HashMap<String, Integer>();
    public HashMap<String, Double> unsortedSearchWordHashMap = new HashMap<String,Double>();
    public  Map<String,Double> sortedMap = new TreeMap<String, Double>();




    public MarkovChainImplementation() {
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
        transactionMatrixData = new int[searchedWordArrayList.size()][searchedWordArrayList.size()];
        for(int i=0;i<searchedWordArrayList.size();i++)
            for(int j=0;j<searchedWordArrayList.size();j++)
                transactionMatrixData[i][j]=0;

        //Build the Transaction Matrix
        transactionMatrixData(TOTAL_FREQUENCY,searchedWordArrayList,transactionMatrixData);


    }

    public void transactionMatrixData(int totalNumberOfSearch, ArrayList<String> searchedWordArrayList,int[][] transactionMatrixData){
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
                transactionMatrixData[verticalPositionofMatrix][horaizontalPositonofMatrix]=searchFrequency;

                //To get percentage value
              //  transactionMatrixData[verticalPositionofMatrix][horaizontalPositonofMatrix]=searchFrequency/TOTAL_FREQUENCY;
            }
        }


        transactionMatrixVector(transactionMatrixData);
        transactionMatrix(transactionMatrixData);
    }
    /**
     * Calculating TracsactionMatrixVector by calculating the sum of each column of TransactionMatrix Data
     *
     */


    public  void transactionMatrixVector(int[][]transactionMatrixData){

        int matrixLength = transactionMatrixData.length;

        transactionMatrixVector = new double [matrixLength];

        for ( int row =0; row< matrixLength; row++) {
            double sum =0;
            for (int column = 0; column < matrixLength; column++) {
                int val = transactionMatrixData[column][row];
                sum += transactionMatrixData[column][row];
            }

             sum/= TOTAL_FREQUENCY;
            transactionMatrixVector[row]= sum;
        }


        System.out.print("=======TransactionVector:=====\n\t");
        for (int i=0;i<matrixLength;i++)
            System.out.print(searchedWordArrayList.get(i)+" ="+transactionMatrixVector[i]+"\t|");
        System.out.print("\n");
    }
/*
The sum of each row in Transaction Matrix is One. That calculation is done by transactionMatrix Function.
 */



    public void transactionMatrix(int[][] transactionMatrixData){

        int matrixLength = transactionMatrixData.length;

        transactionMatrix = new double[matrixLength][matrixLength];

        for ( int row =0; row< matrixLength; row++) {
            double sum =0;
            for (int column = 0; column < matrixLength; column++) {
                sum += transactionMatrixData[row][column];
            }
            if(sum!=0) {
                for (int column = 0; column < matrixLength; column++) {
                    transactionMatrix[row][column] = transactionMatrixData[row][column] / sum;
                }
            }
        }

        System.out.print("=======TransactionMatrix:=============\t\n");
        for (int i=0;i<matrixLength;i++) {
            System.out.print(searchedWordArrayList.get(i)+"\t|");
            for (int j = 0; j < matrixLength; j++) {
                System.out.print(transactionMatrix[i][j] + "\t|");
                }
            System.out.print("\n");
        }
    }

    public  void markovImplementation(int numberOfFutureSteps){

        int matrixlength =  transactionMatrixVector.length;
        double [] intermediateVectorMatrix = new double [matrixlength];
        double sum=0;
        for(int steps = 0; steps<numberOfFutureSteps;steps++) {


                for (int j = 0; j < matrixlength; j++) { // Second matrix
                    for(int k=0;k<matrixlength;k++){
                        sum += transactionMatrixVector[k] * transactionMatrix[k][j];
                    }
                    intermediateVectorMatrix[j] = sum;
                    sum = 0;
                }


            for(int i=0;i<matrixlength;i++){
                transactionMatrixVector[i] = intermediateVectorMatrix[i];
            }

        }



        System.out.print("=========After Performing "+numberOfFutureSteps+" Steps the transaction Vector:=============\n");
        for(int i=0;i<matrixlength;i++){
            transactionMatrixVector[i] = intermediateVectorMatrix[i];
            System.out.print("\t"+transactionMatrixVector[i]+"\t|");
        }

        sortedTermForAbstract();

    }




        /*
        sort the searchword so that top items can be select for selection

         */

    public void sortedTermForAbstract(){



       for (int i= 0;i< searchedWordArrayList.size();i++){
           unsortedSearchWordHashMap.put(searchedWordArrayList.get(i),transactionMatrixVector[i]);
           }
           sortedMap = ValueComparator.sortByValue(unsortedSearchWordHashMap);

       System.out.print("\n=======After Sorting=========\n");
        Set sortedSet = sortedMap.entrySet();
        Iterator it = sortedSet.iterator();
       for(int i=0;i<sortedMap.size();i++) {
           Map.Entry mapEntry = (Map.Entry) it.next();
           System.out.println( mapEntry.getKey() +"|" + mapEntry.getValue()+"\t");
       }


    }




    public void printTransactionMatrix(){
        System.out.println("============Adjacency Matrix:==================");

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
                    System.out.print("\t"+transactionMatrixData[i][j]);
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
