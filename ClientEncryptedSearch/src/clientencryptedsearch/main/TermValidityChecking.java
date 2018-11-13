package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;
import clientencryptedsearch.utilities.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TermValidityChecking  {


    private  String term;
    private  long sleepTime = 5;


    String currentSearch = null;
    public boolean termValidityChecking (String folderPath,boolean allCloudSearch){
        List<String> files = Util.getAbsoluteFilePathsFromFolder(folderPath);

        for(String file:files){
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {

                while ((currentSearch = br.readLine()) != null) {
                    Thread thread = new Thread();
                    ClientSearcher termSearch = new ClientSearcher(currentSearch);
                    termSearch.constructQuery();
                    termSearch.searchTermInCluster(allCloudSearch);
                  //  thread.run();
                    ProcessTermSearchResult processResult = new ProcessTermSearchResult();
                    if(processResult.acceptResultForTermSearch()){ // Check the result if the searched term exist on the cloud or not
                        processResult.writeBestTermSearchHistoryANDAVG();
                    }
                    else
                    {
                        UserInterest userInterestCal = new UserInterest();
                        userInterestCal.setTotalSearchHistory();
                        File folder = new File(Constants.noMatchRecordLocation);
                        File[] listOfFiles = folder.listFiles();
                        boolean fileExisted = false;


                        for(int i =0;i<listOfFiles.length;i++){
                            if(listOfFiles[i].getName().equals(Constants.noMatchRecordFileName)){
                                fileExisted = true;
                                Path path = Paths.get(Constants.noMatchRecordLocation+ File.separator+ Constants.noMatchRecordFileName);
                                List <String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                                for(String number: lines){
                                    int previousNoMatchRecord = Integer.parseInt(number);
                                    previousNoMatchRecord ++;
                                    lines.clear();
                                    lines.add(String.valueOf(previousNoMatchRecord));
                                    Files.write(path,lines, StandardCharsets.UTF_8);

                                }

                            }

                        }

                        if(!fileExisted) {
                            File noMatchFileCreation = new File(Constants.noMatchRecordLocation + File.separator + Constants.noMatchRecordFileName);
                            BufferedWriter output = null;
                            try {
                                output = new BufferedWriter(new FileWriter(noMatchFileCreation));
                                output.write(String.valueOf(1));
                                output.flush();
                                output.close();
                            } catch (IOException e) {
                                System.out.print(this.getClass().getName() + "Can't write term to the No match File record file in the location");
                                e.printStackTrace();
                            }
                        }



                    }

                }

            }

            catch (IOException e){
                System.out.println("Can't read the file from batch uploader, file name:"+ file);
                System.out.println("OR can't write No match Record Text file");
                e.printStackTrace();
            }
        }
        return true;
    }


  /*  @Override
    public void run() {

        try{
            TimeUnit.SECONDS.sleep(sleepTime);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

    }*/
}
