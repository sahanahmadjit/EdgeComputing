package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingEngine {


 public  double  getSemanticDistanceRadiusForTerm(String clusterNumber){

     double avgSimDistance,userInterest,clusterSize,semanticRadius;

     CalculateAverageSimilarityDistance avgSimObj = new CalculateAverageSimilarityDistance();
     UserInterest usrInrstObj = new UserInterest();
     ClusterSizeInfo clsSizeObj = new ClusterSizeInfo();

     avgSimDistance =avgSimObj.getAVGSimilartiyDistanceOfCluster(clusterNumber);
     userInterest = usrInrstObj.getUserIntersetSearchCalculation(Integer.valueOf(clusterNumber), Constants.TOTAL_NUMBER_OF_CLUSTER);
     clusterSize = clsSizeObj.getClusterSizeInfo(clusterNumber);


     semanticRadius = avgSimDistance + userInterest +clusterSize;
 return  semanticRadius;



 }

    /**
     * Read from Edge Abstract Candidate. For each cluster get the semmetric radius and then update the abstract item in EdgeStore/Abstract location
     */
 public  void  addReplaceAbstractItem(){





     File folder = new File(Constants.markovAbastractCandidate);
     File[] listOfFiles = folder.listFiles();



     for(int i =0;i<listOfFiles.length;i++){
         boolean fileExisted = false;
         String clusterFileName = listOfFiles[i].getName();
         String abstractFileName = clusterFileName.replaceAll("cluster","Abstract");
         String[] parseFileNumber = clusterFileName.split("_");
         double semetricRadius = getSemanticDistanceRadiusForTerm(parseFileNumber[1]);
         HashMap<String,Double> abstractTermWeightMap = new HashMap<String, Double>();




         // Abstract Candidate Load to the HashMap
         BufferedReader br;
         try {
             File abstractCandiateFile = new File(Constants.markovAbastractCandidate+File.separator + clusterFileName);
             br = new BufferedReader(new FileReader(abstractCandiateFile));
             String st;
             while ((st = br.readLine()) != null){

                 String [] parseTermWeight = st.split("\\|.\\|");
                 abstractTermWeightMap.put(parseTermWeight[0],Double.parseDouble(parseTermWeight[1]));

             }
         } catch (FileNotFoundException e) {
             System.out.println("Can't find Markov Abstract Candidate from File");
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
             System.out.println("Can't read Markov Abstract Candidate from File");
         }


         //check abstract file exist or not. If not exit create the file

         Path path = Paths.get(Constants.markovAbstractLocation+File.separator+abstractFileName);
         File abstractFile = new File(Constants.markovAbstractLocation+File.separator+abstractFileName);
         try {
             abstractFile.createNewFile(); // If exist then It will do nothing
         } catch (IOException e) {
             e.printStackTrace();
             System.out.println("Can't Create New Abstract File on EdgeStore");
         }




         try {


             //Now Compare with main abstract with radius.

           for(String absCandidate: abstractTermWeightMap.keySet()){

               //Load all the current Abstract element to List
               List<String> lines = null;
               lines = Files.readAllLines(path, StandardCharsets.UTF_8);

               if(!lines.contains(absCandidate)){ //new candidate for abstract item

                   ArrayList<String> removalPresentAbsItemList = new ArrayList<String>();

                   //Now check every Current Abstract Item

                    for(String crntAbs: lines){
                        if(semetricRadius>=(computeWUP(crntAbs,absCandidate))){ // between or in radius
                            //now check the weight
                            double crntAbsWeight =  abstractTermWeightMap.get(crntAbs);
                            double absCandidateWeight =  abstractTermWeightMap.get(absCandidate);

                            if(crntAbsWeight<absCandidateWeight){ // Less weight remove
                                    removalPresentAbsItemList.add(crntAbs);//Add the current item to remove List
                                    lines.add(absCandidate); // new Candidate outperform Existing items ! so add it to the Abstract

                            }

                        }
                    }

                    for(String removeTerm : removalPresentAbsItemList){
                        lines.remove(removeTerm);
                    }

                   // now write down the  abstract again
                   Files.write(path, lines, StandardCharsets.UTF_8);
               }


           }



         } catch (IOException e) {
             e.printStackTrace();
         }









    /*     if(listOfFiles[i].getName().equals("cluster_"+cluster_File_Number)){
             fileExisted = true;
             try {
                 File file;
                 file = listOfFiles[i];
                 Path path = Paths.get(Constants.clusterAvgSimDistanceLocation+File.separator+"cluster_"+cluster_File_Number);
                 List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

                 if(lines.contains(term)){
                     break;
                 }
                 else {

                     lines.add(term); // If new term add in the last line
                     Files.write(path, lines, StandardCharsets.UTF_8);
                 }
             } catch (FileNotFoundException e) {
                 System.out.print(this.getClass().getName() + "Can't find the AVG Similarity Cluster file in the location");
                 e.printStackTrace();
             } catch (IOException e) {
                 System.out.print(this.getClass().getName() + " Can't read the AVG Similarity Cluster file in the location");
                 e.printStackTrace();
             }
         }
     }


     if(!fileExisted){
         File file = new File(Constants.clusterAvgSimDistanceLocation+File.separator+"cluster_"+cluster_File_Number);
         BufferedWriter output = null;
         try {
             output = new BufferedWriter(new FileWriter(file));
             output.write(term);
             output.flush();
             output.close();
         } catch (IOException e) {
             System.out.print(this.getClass().getName()+"Can't write term to the AVG Similarity cluster file in the location");
             e.printStackTrace();
         }
*/
     }



 }

    private static ILexicalDatabase db = new NictWordNet();
    private double computeWUP (String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
        return s;
    }

}
