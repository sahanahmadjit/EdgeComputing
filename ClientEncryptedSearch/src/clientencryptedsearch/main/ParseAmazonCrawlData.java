package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ParseAmazonCrawlData {

    public  void parseClusterFile(){


        File folder = new File(Constants.clusterFileLocatioon);
        File[] listOfFiles = folder.listFiles();
        int fileNumber = 0;


        for(int i =0;i<listOfFiles.length;i++) {

            try {
                File file;
                file = listOfFiles[i];
                Path path = Paths.get(Constants.clusterFileLocatioon + File.separator + file.getName());
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);


                int lineNumber = 1;

                ArrayList<String> fileContent= new ArrayList<String>();
                for (String st : lines) {
                    if(st.length()==1){
                        continue;
                    }
                    if (lineNumber % 10 == 0) {
                       // lineNumber = 0;
                        //write term and close the file
                        String[] parseLine = st.split("\\|.\\|");
                        fileContent.add(parseLine[0]);
                        Path inputDataPath = Paths.get(Constants.batchUploadLocation + File.separator + fileNumber + ".txt");
                        Files.write(inputDataPath, fileContent, StandardCharsets.UTF_8);
                        fileContent = new ArrayList<String>();
                        fileNumber++;
                    } else {

                        String[] parseLine = st.split("\\|.\\|");
                        fileContent.add(parseLine[0]);
                    }
                    lineNumber++;

                }
            } catch (FileNotFoundException e) {
                System.out.print(this.getClass().getName() + "Can't find the  Cluster file in the location");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.print(this.getClass().getName() + " Can't read the Cluster file in the location");
                e.printStackTrace();
            }
        }

    }


}




