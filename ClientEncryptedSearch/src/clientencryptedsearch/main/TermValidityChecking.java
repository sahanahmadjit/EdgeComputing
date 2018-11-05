package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TermValidityChecking implements  Runnable {


    private  String term;
    private  long sleepTime = 5;


    String currentSearch = null;
    public boolean termValidityChecking (String folderPath){
        List<String> files = Util.getAbsoluteFilePathsFromFolder(folderPath);

        for(String file:files){
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {

                while ((currentSearch = br.readLine()) != null) {
                    Thread thread = new Thread();
                    ClientSearcher termSearch = new ClientSearcher(currentSearch);
                    termSearch.constructQuery();
                    termSearch.searchTermInCluster();
                    thread.run();
                    ProcessTermSearchResult processResult = new ProcessTermSearchResult();
                    if(processResult.acceptResultForTermSearch()){ // Check the result if the searched term exist on the cloud or not
                        processResult.writeBestTermSearchHistoryANDAVG();
                    }

                }

            }

            catch (IOException e){
                System.out.println("Can't read the file from batch uploader, file name:"+ file);
            }
        }
        return true;
    }


    @Override
    public void run() {

        try{
            TimeUnit.SECONDS.sleep(sleepTime);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

    }
}
