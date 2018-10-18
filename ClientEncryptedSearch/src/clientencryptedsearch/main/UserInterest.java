package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class UserInterest {


    public void setTotalSearchHistory(){
        try {
            // input the file content to the StringBuffer "input"
            File file;

                file =new File(Constants.searchHistoryFileLocation+ File.separator+ Constants.searchHistoryFileName);
                /*
                check if file is empty of not, Total Search should be upadated first before Individual cluster Info

                * */

                if(file.length()==0){
                BufferedWriter bw;
                bw = new BufferedWriter(new FileWriter(file));
                bw.write("TotalSearch");
                bw.write("|.|");
                bw.write(String.valueOf(1));
                bw.close();
                }
                else {
                        Path path = Paths.get(Constants.searchHistoryFileLocation+File.separator+Constants.searchHistoryFileName);
                        List <String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);

                        int lineNumber =0;
                        for(String line:lines){

                            String [] stringArray = line.split("\\|.\\|");
                            if(stringArray[0].equals("TotalSearch")){
                                int searchNumber = Integer.parseInt(stringArray[1]);
                                searchNumber ++;
                                lines.set(lineNumber,"TotalSearch|.|"+String.valueOf(searchNumber));
                                Files.write(path,lines, StandardCharsets.UTF_8);
                                break;
                            }
                            lineNumber++;

                        }
                    }



            } catch (FileNotFoundException e) {
                System.out.print(this.getClass().getName() + "Can't find search History file in location");
            } catch (IOException e) {
                System.out.print(this.getClass().getName() + " Can't read search History file");
            }

    }

    public void  setIndividualSearchHistory(String clusterNumber){

        try {
            // input the file content to the StringBuffer "input"
            File file;
            boolean firstEntryInCluster = true;//to track for first entry of Individual Cluster

            file =new File(Constants.searchHistoryFileLocation+ File.separator+ Constants.searchHistoryFileName);
                /*
                check if file is empty of not, Total Search should be upadated first before Individual cluster Info

                * */

                Path path = Paths.get(Constants.searchHistoryFileLocation+File.separator+Constants.searchHistoryFileName);
                List <String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);

                int lineNumber =0;
                for(String line:lines){

                    /*
                    Skip the first line for total Search
                     */
                    if(lineNumber==0){
                        lineNumber++;
                        continue;
                    }
                    String [] stringArray = line.split("\\|.\\|");
                    String[] clusterFileNameArray = stringArray[0].split("_");//to get cluster Number
                    if(clusterFileNameArray[1].equals(clusterNumber)){
                        firstEntryInCluster = false;
                        int searchNumber = Integer.parseInt(stringArray[1]);
                        searchNumber ++;
                        lines.set(lineNumber,"Cluster_"+clusterNumber+"|.|"+String.valueOf(searchNumber));
                        Files.write(path,lines, StandardCharsets.UTF_8);
                        break;
                    }
                    lineNumber++;

                }

            /*
            for first Entry of Individual cluster. append text at the end of a file
             */

            if(firstEntryInCluster){

                lines.add(lineNumber,"Cluster_"+clusterNumber+"|.|"+String.valueOf(1));
                Files.write(path,lines, StandardCharsets.UTF_8);
            }




        } catch (FileNotFoundException e) {
            System.out.print(this.getClass().getName() + "Can't find search History file in location");
        } catch (IOException e) {
            System.out.print(this.getClass().getName() + " Can't read search History file");
        }

    }

    public  int  getIndividualSearchHistory(String clusterNumber){
        int searchNumber=0;

        try {
            // input the file content to the StringBuffer "input"
            File file;
            file =new File(Constants.searchHistoryFileLocation+ File.separator+ Constants.searchHistoryFileName);
            Path path = Paths.get(Constants.searchHistoryFileLocation+File.separator+Constants.searchHistoryFileName);
            List <String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);

            int lineNumber =0;
            for(String line:lines){
                    /*
                    Skip the first line for total Search
                     */
                if(lineNumber==0){
                    lineNumber++;
                    continue;
                }
                String [] stringArray = line.split("\\|.\\|");
                String[] clusterFileNameArray = stringArray[0].split("_");//to get cluster Number
                if(clusterFileNameArray[1].equals(clusterNumber)){
                     searchNumber = Integer.parseInt(stringArray[1]); //use this array to extract search Number
                    break;
                }
                lineNumber++;

            }
        } catch (FileNotFoundException e) {
            System.out.print(this.getClass().getName() + "Can't find search History file in location");
        } catch (IOException e) {
            System.out.print(this.getClass().getName() + " Can't read search History file");
        }

        return  searchNumber;
    }

    public int getTotalSearchHistory(){

        int searchNumber=0;
        try {
            // input the file content to the StringBuffer "input"
            File file;
            file =new File(Constants.searchHistoryFileLocation+ File.separator+ Constants.searchHistoryFileName);


                Path path = Paths.get(Constants.searchHistoryFileLocation+File.separator+Constants.searchHistoryFileName);
                List <String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);
                for(String line:lines){

                    String [] stringArray = line.split("\\|.\\|");
                    if(stringArray[0].equals("TotalSearch")){
                        searchNumber = Integer.parseInt(stringArray[1]);
                        break;
                    }
                }

        } catch (FileNotFoundException e) {
            System.out.print(this.getClass().getName() + "Can't find search History file in location");
        } catch (IOException e) {
            System.out.print(this.getClass().getName() + " Can't read search History file");
        }

        return searchNumber;
    }

    public double userIntersetSearchCalculation(int clusterNumberOfInterest,int totalNumberOfCluster){

        int totalSearch = getTotalSearchHistory();
        int individualClusterSearch= getIndividualSearchHistory(String.valueOf(clusterNumberOfInterest));

        double avgClusterSearch = totalSearch/totalNumberOfCluster;
        double gap = individualClusterSearch - avgClusterSearch;
        double percentage = gap/avgClusterSearch;

        return percentage;
    }

}
