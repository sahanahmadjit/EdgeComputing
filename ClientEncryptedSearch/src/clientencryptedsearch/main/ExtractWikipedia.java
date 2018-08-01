package clientencryptedsearch.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import clientencryptedsearch.utilities.Constants;
import clientencryptedsearch.utilities.StopwordsRemover;

public class ExtractWikipedia {
    ExtractKeyPhrases wikiExtractKeyPhrases;
    final String endPoint = "http://en.wikipedia.org/wiki/";
    String[] opts = {"-l", "data/tmp/", "-m", "keyphrextr", "-t", "PorterStemmer", "-v", "none"};
    StopwordsRemover stop = new StopwordsRemover("wiki_stopwords_en.txt");
    
    /**
     * Download the txt file of the wikipedia page for the given word.
     * @param word Desired search term.
     * @return Nonnegative if the term was found.
     */
    public int downloadWikiContent(String word) {
        System.out.println("extracting wiki");
        String data = "";
        // url for wikipedia
        StringTokenizer st = new StringTokenizer(word);
        String key = "";
        File file = new File(Constants.tempLocation + File.separator + word + ".txt");
        if(file.exists()) file.delete();
        if (st.countTokens() > 1) {
            key = st.nextToken();
            while (st.hasMoreTokens()) {
                    key = key + "_" + st.nextToken();
            }
        } else {
            key = word;
        }
        try {
            // download WIkipedia article and extract keyphrases
            URL url = new URL(endPoint + key);
            data = Jsoup.parse(url, 10000).text();
            System.out.println(url.toExternalForm());
            PrintWriter writer = new PrintWriter(Constants.tempLocation + File.separator + word + ".txt", "UTF-8");
            writer.print(data);
            writer.close();
        } catch (IOException e) {
            System.err.println("Invalid wiki url for search string : " + word + " url : " + endPoint + key);
            // e.printStackTrace();
            return -1;
        }
        return data.trim().length();
    }

    
    /**
     * Get Wiki Topics.
     * Extracts keyphrases from all downloaded wikipedia files.
     * 
     * Preconditions: All desired txt files files for the serach must be set up and in 
     *   the data/tmp folder
     * Postconditions: All .key and .txt and .key files are removed are removed 
     * and all key topics are extracted and put in an arraylist that is returned
     * FIXME: .key files won't delete
     * @param querySize number of terms in the query.  Used for assessing importance.
     * @return  The key topics with their associated importance value
     */
    public ArrayList<String> getWikiTopics(int querySize){
        ArrayList<String> keyphrases = new ArrayList<>();
        
        //Perform MAUI extraction
        wikiExtractKeyPhrases = new ExtractKeyPhrases();
        wikiExtractKeyPhrases.extract(Constants.getMauiExtractionOptions(Constants.tempLocation));
        
        //Get a list of all file names in the directory
        ArrayList<String> files = getFiles();
        
        //Go through each file in the folder
        files.stream().forEach((fileName) -> {
            //If it's a txt file, it's a downloaded wiki page and should be deleted
            if (fileName.endsWith(".txt")) {
                File file = new File(fileName);
                file.delete();
            }
            //If it's a key file, we can process it and get its members
            else if (fileName.endsWith(".key")) {
                ArrayList<String> fileKeys = processKeyFile(fileName);
                File file = new File(fileName);
                file.delete();
                
                keyphrases.addAll(fileKeys);
            }
        });
        
        return keyphrases;
    }
    
    /**
     * Get Files.
     * Creates a list of all filenames in the supplied directory in this class.
     * 
     * NOTE: This is a direct copy of the method from UPloader.  Maybe should fix that.
     * @return A list of file names.
     */
    private ArrayList<String> getFiles() {
        File dir = new File(Constants.tempLocation);
        ArrayList<String> files;
        files = new ArrayList<>();
        
        //Make sure this is a folder, not just a file
        if (dir.isDirectory()) {
            //Get an array of relative file names in the folder
            String[] contents = dir.list();
            for (String file : contents) {
                files.add(Constants.tempLocation + File.separator + file); //Give each file its full pathname.
            }
        } else { //if it's just one file, return its path name
            files.add(Constants.tempLocation);
        }
        
        return files;
    }
    
    /**
     * Get wiki terms using the weights hash map method.
     * @param weights The map where the wiki data will get placed.
     * @param parentWeight The weight of the term this group of wiki terms is associated with.
     */
    public void getWikiTopics(HashMap<String, Float> weights, float parentWeight) {
        ArrayList<String> keyphrases = new ArrayList<>();
        
        // Make sure that there is something for Maui to grab.
        // There won't be anything if the downloader didn't find something.
        if (new File(Constants.tempLocation).list().length < 1) {
            System.out.println("No wiki document found.");
            return;
        }
        
        //Perform MAUI extraction
        wikiExtractKeyPhrases = new ExtractKeyPhrases();
        wikiExtractKeyPhrases.extract(Constants.getMauiExtractionOptions(Constants.tempLocation));
        
        //Get a list of all file names in the directory
        ArrayList<String> files = getFiles();
        
        //Go through each key file with the wiki topics
        for (String filename : files) {
            //If it's a .txt file, it's a downloaded page and should be deleted
            if (filename.endsWith(".txt")) {
                File file = new File(filename);
                file.delete();
            }
            //If it's a .key file, we should process it and get its terms
            else if (filename.endsWith(".key")) {
                ArrayList<String> keys = processKeyFile(filename);
                File keyFile = new File(filename);
                keyFile.delete();
                
                //Now that we have the list of terms, we need to strip them of stopwords and add them to the weights
                stop.remove(keys);
                for (String key : keys) {
                    weights.put(key.toLowerCase(), parentWeight / keys.size());
                }
            }
        }
    }

    
    /**
     * Process Key File.
     * Reads the keywords from the .key file and puts them in a list.
     * @param fileName 
     * @param querySize
     * @return The list of words in the file
     */
    private ArrayList<String> processKeyFile(String fileName)  {
        BufferedReader br;
        ArrayList<String> fileKeys = new ArrayList<>(); //Keyphrases of the file
        try {
            br = new BufferedReader( new FileReader(fileName));
            String key;
            while ((key = br.readLine()) != null) {
                //TODO: Check if it's a filler word
                fileKeys.add(key);
                //TODO: Add importance value in
            }
            br.close();
        } catch(IOException e) {
            System.err.println("File " + fileName + " not found");
        }
        
        return fileKeys;
    }
}
