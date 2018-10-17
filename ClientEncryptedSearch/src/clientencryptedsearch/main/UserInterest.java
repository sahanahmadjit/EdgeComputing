package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class UserInterest {

    public  int getTotalSearchHistory(){
    return 0;
    }

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

    }

    public  int  individualSearchHistory(String clusterNumber){
        return  0;
    }

    public void userIntersetSearchCalculation(){

    }

}
