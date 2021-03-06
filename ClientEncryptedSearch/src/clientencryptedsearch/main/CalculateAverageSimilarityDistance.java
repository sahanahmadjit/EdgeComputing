package clientencryptedsearch.main;


import clientencryptedsearch.utilities.Constants;
import clientencryptedsearch.utilities.StopwordsRemover;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import sun.util.locale.provider.AvailableLanguageTags;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CalculateAverageSimilarityDistance {


    double AVG_SIM_DISTANCE_VALUE;
    double SUM_AVG_SIM_DISTANCE_VALUE;
    double precisionVal = 10000000000.0;
    StopwordsRemover stop = new StopwordsRemover("wiki_stopwords_en.txt");

    public  void writeTermToClusterFile( String term, String cluster_File_Number){


        File folder = new File(Constants.clusterAvgSimDistanceLocation);
        File[] listOfFiles = folder.listFiles();
        boolean fileExisted = false;


        for(int i =0;i<listOfFiles.length;i++){
            if(listOfFiles[i].getName().equals("cluster_"+cluster_File_Number)){
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

        }


    }

    public  void writeTermTOMarkovDataFile (String term, String cluster_File_Number){
        File folder = new File(Constants.markovDataFileLocation);
        File[] listOfFiles = folder.listFiles();
        boolean fileExisted = false;


        for(int i =0;i<listOfFiles.length;i++){
            if(listOfFiles[i].getName().equals("cluster_"+cluster_File_Number)){
                fileExisted = true;
                try {
                    File file;
                    file = listOfFiles[i];
                    Path path = Paths.get(Constants.markovDataFileLocation+File.separator+"cluster_"+cluster_File_Number);
                    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                        lines.add(term); // If new term add in the last line
                        Files.write(path, lines, StandardCharsets.UTF_8);

                } catch (FileNotFoundException e) {
                    System.out.print(this.getClass().getName() + "Can't find the markov Data file in the location");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.print(this.getClass().getName() + " Can't read the markov Data file in the location");
                    e.printStackTrace();
                }
            }
        }


        if(!fileExisted){
            File file = new File(Constants.markovDataFileLocation+File.separator+"cluster_"+cluster_File_Number);
            BufferedWriter output = null;
            try {
                output = new BufferedWriter(new FileWriter(file));
                output.write(term);
                output.flush();
                output.close();
            } catch (IOException e) {
                System.out.print(this.getClass().getName()+"Can't write term to the markov Data file in the location");
                e.printStackTrace();
            }

        }

    }



    public double getAVGSimilartiyDistanceOfCluster(String cluster_File_Number){


        File folder = new File(Constants.clusterAvgSimDistanceLocation);
        File[] listOfFiles = folder.listFiles();
        boolean fileExisted = false;
        double temp= 0.0;

        for(int i =0;i<listOfFiles.length;i++){
            if(listOfFiles[i].getName().equals("cluster_"+cluster_File_Number)){
                try {
                    File file;
                    file = listOfFiles[i];
                    Path path = Paths.get(Constants.clusterAvgSimDistanceLocation+File.separator+"cluster_"+cluster_File_Number);
                    List <String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);

                    /*
                    arr = {a,b,c}  // didn't measure similarity of two word two times.
                    for (i=0;i<len;i++)
                     for(j=i+1;j<len;j++)
                       sim(arr[i],arr[j]

                       a b c d  e
                       ab ac ad ae
                          bc bd be
                                de

                     */
                    double numberOfTerm,totalNumberOfValidWordInWordNet=0;

                    numberOfTerm=0;
                    AVG_SIM_DISTANCE_VALUE = 0.0;

                    for(int firstTerm = 0; firstTerm<lines.size();firstTerm++){



                        for (int secondTerm = firstTerm+1; secondTerm<lines.size();secondTerm++){

                            String[] firstTermArray = lines.get(firstTerm).split("\\s");
                            String [] secondTermArray = lines.get(secondTerm).split("\\s");
                            ArrayList<String> firstTermList = new ArrayList<String>();
                            ArrayList<String> secondTermList = new ArrayList<String>();

                            for(String term:firstTermArray){
                                firstTermList.add(term);
                            }

                            for(String term: secondTermArray){
                                secondTermList.add(term);
                            }
                            stop.remove(firstTermList);
                            stop.remove(secondTermList);


                            for(String firstWord: firstTermList){
                                for(String secondWord:secondTermList){
                                  double  similarityDistance =  (computeWUP(firstWord,secondWord));
                                  double roundSimilarityDistance;
                                  if(similarityDistance>1.00){

                                        roundSimilarityDistance = 1.0; // Exact Same word comparison gives 1.7976931348623157E308 don't know why wu palmer shows such value. It should be one
                                  }
                                  else {
                                       roundSimilarityDistance = Math.round( similarityDistance * precisionVal) / precisionVal;
                                  }



                                    if(roundSimilarityDistance>0){ //if word not present then the value will be return by -1. The word net range 0 to 1
                                        AVG_SIM_DISTANCE_VALUE+= roundSimilarityDistance;
                                        numberOfTerm++;
                                    }
                                }
                            }

                       /*    double similarityDistance = computeWUP(lines.get(firstTerm),lines.get(secondTerm));

                             if(similarityDistance>0){ //if word not present then the value will be return by -1. The word net range 0 to 1
                                 AVG_SIM_DISTANCE_VALUE+= similarityDistance;
                                 numberOfTerm++;
                                     }*/

                        }
                  /*      if(numberOfTerm !=0){ //number of term value will be 0 for word didn't find on word net. the divide operation will make 0 result

                            temp += AVG_SIM_DISTANCE_VALUE;
                            System.out.println(temp + "number of Term:" + numberOfTerm);

                           AVG_SIM_DISTANCE_VALUE /= numberOfTerm;
                            totalNumberOfValidWordInWordNet++;
                        }*/


                    }


                  //  AVG_SIM_DISTANCE_VALUE/= totalNumberOfValidWordInWordNet;

                    System.out.println("\nTotal Number of Term Compared:" + numberOfTerm + " Total Sim Distance Value: " + AVG_SIM_DISTANCE_VALUE);
                    System.out.print("Average Value: ");
                    AVG_SIM_DISTANCE_VALUE /= numberOfTerm;

                } catch (FileNotFoundException e) {
                    System.out.print(this.getClass().getName() + "Can't find search History file in location");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.print(this.getClass().getName() + " Can't read search History file");
                    e.printStackTrace();
                }

            }
        }
        return Math.round( AVG_SIM_DISTANCE_VALUE * precisionVal ) / precisionVal;

    }



    public  double getAllClusterAVGDistanceValue(int totalNumberOfCluster){

        SUM_AVG_SIM_DISTANCE_VALUE = 0.0;

        for(int i=0;i<totalNumberOfCluster;i++){
            SUM_AVG_SIM_DISTANCE_VALUE += getAVGSimilartiyDistanceOfCluster(String.valueOf(i));
        }

        SUM_AVG_SIM_DISTANCE_VALUE/= totalNumberOfCluster;

        return SUM_AVG_SIM_DISTANCE_VALUE;

    }




    private static ILexicalDatabase db = new NictWordNet();
    private double computeWUP (String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
        return s;
    }


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
