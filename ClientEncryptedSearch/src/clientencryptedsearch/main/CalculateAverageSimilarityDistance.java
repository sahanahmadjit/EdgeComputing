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
import java.util.List;

public class CalculateAverageSimilarityDistance {


    double AVG_SIM_DISTANCE_VALUE;
    double SUM_AVG_SIM_DISTANCE_VALUE;


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

                        if(lines.contains(term)) {
                        break;
                        }
                        else {
                        lines.add(term); // If new term add in the last line
                             Files.write(path,lines, StandardCharsets.UTF_8);
                        }
                } catch (FileNotFoundException e) {
                    System.out.print(this.getClass().getName() + "Can't find the AVG Similarity Cluster file in the location");
                } catch (IOException e) {
                    System.out.print(this.getClass().getName() + " Can't read the AVG Similarity Cluster file in the location");
                }

                break;
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
            }

        }


    }



    public double getAVGSimilartiyDistanceOfCluster(String cluster_File_Number){

        AVG_SIM_DISTANCE_VALUE = 0.0;
        File folder = new File(Constants.clusterAvgSimDistanceLocation);
        File[] listOfFiles = folder.listFiles();
        boolean fileExisted = false;


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


                    for(int firstTerm = 0; firstTerm<lines.size();firstTerm++){
                        for (int secondTerm = firstTerm+1; secondTerm<lines.size();secondTerm++){
                          AVG_SIM_DISTANCE_VALUE+=computeWUP(lines.get(firstTerm),lines.get(secondTerm));
                        }
                    }


                    AVG_SIM_DISTANCE_VALUE/= lines.size();

                } catch (FileNotFoundException e) {
                    System.out.print(this.getClass().getName() + "Can't find search History file in location");
                } catch (IOException e) {
                    System.out.print(this.getClass().getName() + " Can't read search History file");
                }

            }
        }
        return AVG_SIM_DISTANCE_VALUE;
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

}
