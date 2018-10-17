/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.main;

import clientencryptedsearch.utilities.CipherText;
import clientencryptedsearch.utilities.ClientMetrics;
import clientencryptedsearch.utilities.Config;
import clientencryptedsearch.utilities.Constants;
import clientencryptedsearch.utilities.StopwordsRemover;
import clientencryptedsearch.utilities.Util;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client Semantic Searcher.
 * Responsible for:
 *  Expanding the query into the modified query set.
 *  Weighting.
 *  Sending the query over to the server.
 * @author jason
 */
public class ClientSearcher {
    String originalQuery;
    HashMap<String, Float> queryWeights; //Weights for the original query and all of its individual terms
    HashMap<String, Float> synonymWeights; //Weights for all synonyms
    HashMap<String, Float> wikiWeights; //Weights for all wiki terms
    HashMap<String, Float> allWeights; //Weights for all terms, what is finally sent over.  Will either be filled by encrypt or consolidate
    
    StopwordsRemover stop;
    StopwordsRemover wikiStop;
    
    Thesaurus thesaurus;
    ExtractWikipedia wikiExtractor;
    
    CipherText cipher; //So we can encrypt the query before sending it
    
    ArrayList<AbstractIndex> abstracts; //All the abstracts, so that we can check our query against them.
    
    Socket sock;
    
    ArrayList<String> searchResults;
    ArrayList<String> searchedAbstractNames;
    
    /**
     * Constructor.
     * Sets up required objects, maps, and variables.
     * Does not do any of the semantic query modification yet.
     * @param query The original user query
     */
    public ClientSearcher(String query) {
        originalQuery = query.toLowerCase();
        queryWeights = new HashMap<>();
        synonymWeights = new HashMap<>();
        wikiWeights = new HashMap<>();
        allWeights = new HashMap<>();
        
        stop = new StopwordsRemover("stopwords_en.txt");
        wikiStop = new StopwordsRemover("wiki_stopwords_en.txt");
        
        thesaurus = new Thesaurus(); //No actual constructor
        wikiExtractor = new ExtractWikipedia();
        
        cipher = new CipherText(); //Set up keys etc...
        
        abstracts = new ArrayList<>();
        
        searchResults = new ArrayList<>();
        searchedAbstractNames = new ArrayList<>();
    }
    
    //--------------------QUERY PROCESSING-------------------------
    
    /**
     * Constructs the query vector to be sent to the server.
     * The entire process builds up the multiple weights.
     * Splits the query into its individual components and weights them.
     * Adds synonyms for each term in the queryWeights map, and weights them.
     * Adds the wikipedia terms for each term in the queryWeights map, and weights them.
     */
    public void constructQuery() {
        // Log time to process the query
        long begin = System.currentTimeMillis();
        splitQuery();
        addSynonyms();
        //need to be uncommented
      //  addWikiTerms();
        long end = System.currentTimeMillis();
        if (Config.calcMetrics)
            ClientMetrics.writeQueryTime(end-begin, originalQuery);
    }
    
    /**
     * Splits and weights the original query.
     * Adds terms to the queryWeights, splitting based on config method.
     * Pre: Original query has been set.
     * Post: queryWeights has all required data.
     */
    public void splitQuery() {
        String[] subQueries;
        
        if (Config.subdivideQuery)
            subQueries = subdivideQuery(originalQuery);
        else
            subQueries = originalQuery.split(" "); //Just split by spaces
        
        // Now subQueries holds all desired levels of query splitting.  Remove stopwords
        subQueries = stop.remove(subQueries);
        
        // Start adding weights
        queryWeights.put(originalQuery, 1.0f);
        for (String term : subQueries) {
            queryWeights.put(term, (1.0f / subQueries.length));
        }
        
        System.out.println(queryWeights);
    }
    
    /**
     * Subdivide query.
     * This method takes in a string and subdivides it to get all possible adjacent subsets,
     * (though not permutations of them). 
     * @param query The string to be subdivided
     * @return All subsets of the query, but not the query itself
     */
    private String[] subdivideQuery(String query) {
        ArrayList<String> subdivision = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String[] split = query.split(" ");

        //Code on how to split into subqueries
        for (int i = split.length - 1; i > 0; i--) {
            for (int j = 0; j <= split.length - i; j++) {
                for (int k = j; k < i + j; k++) {
                    //Put spaces in between words, but only if there's already another word.
                    if (sb.length() > 0) 
                        sb.append(" ").append(split[k]);
                    else 
                        sb.append(split[k]);
                }
                subdivision.add(sb.toString());
                sb = new StringBuilder();
            }
        }
        
        String[] a = new String[0];
        return subdivision.toArray(a);
    }
    
    /**
     * Get synonyms for all terms in the original query.
     * Pre: queryWeights must be filled by splitQuery.
     * Post: synonymWeights will be filled.
     */
    public void addSynonyms() {
        //Go through the query weights and find synonyms 
        //For each query, look them up with the thesaurus, then weight them with 
        // (queryWeights[query] / synonyms.length)
        for (String query : queryWeights.keySet()) {
            ArrayList<String> synonyms = thesaurus.getSynonyms(query);
            stop.remove(synonyms); 
            float weight = (queryWeights.get(query) / synonyms.size());
            for (String syn : synonyms) {
                synonymWeights.put(syn, weight);
            }
        }
        System.out.println("Synonyms: " + synonymWeights);
    }
    
    public void addWikiTerms() {
        //Go through the query weights and download the wiki page for each of them.
        //For each query, download the content, and get them
        for (String query : queryWeights.keySet()) {
            wikiExtractor.downloadWikiContent(query);
            wikiExtractor.getWikiTopics(wikiWeights, (queryWeights.get(query)));
        }
        
        System.out.println("Wiki Topics: " + wikiWeights);
    }
    
    //---------------------SEARCHING AND RESULTS--------------------
    
    /**
     * Send Search Query To The Server To Perform Search.
     * Does several things:
     * Consolidates and encrypts the query.
     * Loads in the abstracts to be compared against the query.
     * Ranks the abstracts against the query.
     * Sends the query and abstract choice to the server
     */
    public void search() {
        //First things first (I'm the realest) we have to fill allWeights with the encrypted data
        consolidateQuery();
        System.out.println(allWeights);
         
        boolean scanning = true;
        while(scanning) {
            //Now send allWeights over a socket.
            try {
                sock = new Socket(Config.cloudIP, Config.socketPort);

                DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

                sock.setKeepAlive(true);
                sock.setSoTimeout(10000);
            
            
                //Just write how many entries we need to write so the cloud knows.
                dos.writeInt(allWeights.size());

                //Start writing to it.  One entry at a time.
                for (String term : allWeights.keySet()) {
                    //Send the term, then the weight
                    dos.writeUTF(term);
                    dos.writeFloat(allWeights.get(term));
                }
                
                scanning = false;
            
                // Write the info on the abstracts to search
    //            dos.writeInt(searchedAbstractNames.size());
    //            for (String name : searchedAbstractNames) {
    //                dos.writeUTF(name);
    //            }

    //            dos.close();
    //            sock.close();
            } catch (IOException ex) {
                System.err.println(ClientSearcher.class.getName() + ": Error sending weights. trying again");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(ClientSearcher.class.getName()).log(Level.SEVERE, null, ex1);
                    }
            }
        }
    }

    /*
    Send search query to server.
     */

    public void searchTermInCluster() {
        //First things first (I'm the realest) we have to fill allWeights with the encrypted data
        consolidateQuery();
        System.out.println("AllWeights Length:" +allWeights.size());
        Map <String,Float> sortedTermByWeight = new HashMap<String, Float>();
        ArrayList<String> rankOfAbstractList = new ArrayList<String>();
        ArrayList<String> tempAbstractList = new ArrayList<String>();
        boolean scanning = true;
        while(scanning) {
            //Now send allWeights over a socket.
            try {



                //Just write how many entries we need to write so the cloud knows.


                sortedTermByWeight = ValueSortHashMap.sortHashMap(allWeights,false);
                ProcessTermSearchResult objToPassSortedMap = new ProcessTermSearchResult();
                objToPassSortedMap.sortedTermMap(sortedTermByWeight);
                //Start writing to it.  One entry at a time.

                boolean temp = true;
                for (String term : sortedTermByWeight.keySet()) {
                    //Use the query to prepare the hashed query set to send to server
                   //Constructor just initializes

                    //Rank our abstracts based on the query and send it over.

                        ClientSearcher searcher = new ClientSearcher(term);

                       tempAbstractList =searcher.rankAbstractsForTermSearchInCluster();

                       for(String clusterNumber: tempAbstractList){
                           rankOfAbstractList.add(clusterNumber);
                       }
                       rankOfAbstractList.add("|"); // The Separator for diffterent term Abstract Number
                }



                sock = new Socket(Config.cloudIP, Config.socketPort);
                DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

                        sock.setKeepAlive(true);
                        sock.setSoTimeout(90000);

                        dos.writeInt(rankOfAbstractList.size()); //Total Number of Cluster
                        for(String clusterNumber: rankOfAbstractList){
                            dos.writeUTF(clusterNumber);
                        }

                        System.out.print(rankOfAbstractList.size());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(ClientSearcher.class.getName()).log(Level.SEVERE, null, ex1);
                }
                        dos.writeInt(sortedTermByWeight.size()); //Total Number of Term
                        for(String term: sortedTermByWeight.keySet()){
                            dos.writeUTF(term);
                            dos.writeFloat(sortedTermByWeight.get(term));
                        }
                        dos.close();
                        sock.close();
                     //   searcher.acceptResults();
//                        searcher.processResults();


                scanning = false;
            } catch (IOException ex) {
                System.err.println(ClientSearcher.class.getName() + ": Error sending weights. trying again");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(ClientSearcher.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }



    //Put all of the weights and query stuff into one map.
    private void consolidateQuery() {
        //First go through the query weights
        for (String query : queryWeights.keySet()) {
            allWeights.put(query, queryWeights.get(query));
        }
        //Then go through the synonyms
        for (String syn : synonymWeights.keySet()) {
            allWeights.put(syn, synonymWeights.get(syn));
        }
        //Finally, the wiki terms
      /*  for (String wiki : wikiWeights.keySet()) {
            allWeights.put(wiki, wikiWeights.get(wiki));
        }*/
    }
        
    //Puts all of the weights and query stuff into one map, and encrypts the query stuff.
    private void encryptAndConsolidateQuery() {
        //First go through the query weights
        for (String query : queryWeights.keySet()) {
            String encQuery = cipher.encrypt_RSA(query);
            allWeights.put(encQuery, queryWeights.get(query));
        }
        //Then go through the synonyms
        for (String syn : synonymWeights.keySet()) {
            String encSyn = cipher.encrypt_RSA(syn);
            allWeights.put(encSyn, synonymWeights.get(syn));
        }
        //Finally, the wiki terms
        for (String wiki : wikiWeights.keySet()) {
            String encWiki = cipher.encrypt_RSA(wiki);
            allWeights.put(encWiki, wikiWeights.get(wiki));
        }
    }
    
    //Loads in all of the abstracts so they can be checked against the query
    private void loadAbstracts() {
        List<String> abstractFilePaths = Util.getAbsoluteFilePathsFromFolder(Constants.abstractLocation);
        
        for (String filePath : abstractFilePaths) {
            AbstractIndex abs = new AbstractIndex();
            abs.readAbstractFromFile(filePath);

            //This Line need to uncomment later : Sahan
          //  System.out.println(abs);
            
            abstracts.add(abs);
        }
        
        
    }
    
    // Compare every term in the query to every term in every abstract to get a score for that abstract
    public void rankAbstracts() {
        loadAbstracts(); //puts abstracts into memory (the abstracts object)
        
        // Get a queue for the ranked abstracts to be in.
        PriorityQueue<AbstractIndex> rankedAbstracts = new PriorityQueue(10, new Comparator<AbstractIndex>() {
            public int compare (AbstractIndex lhs, AbstractIndex rhs) {
                if (lhs.rankScore < rhs.rankScore) return 1;
                if (lhs.rankScore == rhs.rankScore) return 0;
                return -1;
            }
        });        
        
        String[] queryTerms = originalQuery.split(" "); //for now just split the query into regular words
        
        // Measure how long the ranking procedure took
        long begin = System.currentTimeMillis();
        
        for (AbstractIndex abs : abstracts) { 
            abs.rankScore = 0; //reset score
            
            for (String absWord : abs.terms) {
                absWord.replaceAll(" ", "_"); //our similarity thing doesn't work with spaces
                
                for (String qWord : queryTerms) {
                    abs.rankScore += computeWUP(qWord, absWord);
                }
            }
            
            rankedAbstracts.add(abs);
        }
        
        long end = System.currentTimeMillis();
        if (Config.calcMetrics)
            ClientMetrics.writeAbstractTime(end-begin, originalQuery);
        
        // Now that the abstracts have been ranked, we just get the names of however many we need to send over
        for (int i = 0; i < Config.numSearchedAbstracts && i < abstracts.size(); i++) {
            searchedAbstractNames.add(rankedAbstracts.poll().name);
        }
        System.out.println("The Term Looking in the Abstract:" + originalQuery);
        System.out.println("Will be searching over abstracts: " + searchedAbstractNames);
        
        if (Config.writeClusterChoices)
            ClientMetrics.writeClusterChoice(searchedAbstractNames, originalQuery);
    }


    // Compare every term in the query to every term in every abstract to get a score for that abstract
    public ArrayList<String> rankAbstractsForTermSearchInCluster() {
        loadAbstracts(); //puts abstracts into memory (the abstracts object)

        // Get a queue for the ranked abstracts to be in.
        PriorityQueue<AbstractIndex> rankedAbstracts = new PriorityQueue(10, new Comparator<AbstractIndex>() {
            public int compare (AbstractIndex lhs, AbstractIndex rhs) {
                if (lhs.rankScore < rhs.rankScore) return 1;
                if (lhs.rankScore == rhs.rankScore) return 0;
                return -1;
            }
        });

        String[] queryTerms = originalQuery.split(" "); //for now just split the query into regular words

        // Measure how long the ranking procedure took
        long begin = System.currentTimeMillis();

        for (AbstractIndex abs : abstracts) {
            abs.rankScore = 0; //reset score

            for (String absWord : abs.terms) {
                absWord.replaceAll(" ", "_"); //our similarity thing doesn't work with spaces

                for (String qWord : queryTerms) {
                    abs.rankScore += computeWUP(qWord, absWord);
                }
            }

            rankedAbstracts.add(abs);
        }

        long end = System.currentTimeMillis();
        if (Config.calcMetrics)
            ClientMetrics.writeAbstractTime(end-begin, originalQuery);

        // Now that the abstracts have been ranked, we just get the names of however many we need to send over
        for (int i = 0; i < Config.numSearchedAbstracts && i < abstracts.size(); i++) {
            searchedAbstractNames.add(rankedAbstracts.poll().name);
        }
        System.out.println("The Term Looking in the Abstract:" + originalQuery);
        System.out.println("Will be searching over abstracts: " + searchedAbstractNames);

        if (Config.writeClusterChoices)
            ClientMetrics.writeClusterChoice(searchedAbstractNames, originalQuery);

        return searchedAbstractNames;
    }



    // Send the abstract names we just figured out to the cloud so it can start loading them.
    public void sendAbstracts() {
        try {
            sock = new Socket(Config.cloudIP, Config.socketPort);
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            
            // Write the info on the abstracts to search
            dos.writeInt(searchedAbstractNames.size());
            for (String name : searchedAbstractNames) {
                dos.writeUTF(name);
            }
            dos.close();
            sock.close();
        } catch (IOException ex) {
            System.err.println(ClientSearcher.class.getName() + ": Error sending weights");
            ex.printStackTrace();
        }
    }
    
    //Provides the means to do a relatedness calculation on words
    private ILexicalDatabase db = new NictWordNet(); 
    private double computeWUP (String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
        return s;
    }
    
    
    
    public ArrayList<String> acceptResults() {
        System.out.println("Waiting for file list from server...");
        
        int numSearchResults = 0; 
        DataInputStream dis = null;
        // Scan for connection
        boolean scanning = true;
        while(scanning) {
            try {
                sock = new Socket(Config.cloudIP, Config.socketPort);
                dis = new DataInputStream(sock.getInputStream());
                numSearchResults = dis.readInt();
                
                scanning = false;
            } catch (IOException ex) {
                System.err.println("Connect failed, waiting and will try again.");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(ClientSearcher.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        
        try {
            
            for (int i = 0; i < numSearchResults; i++) {
                searchResults.add(dis.readUTF());
            }
            
            // If we're doing metrics, the cloud will send the data on how long it took.
            if (Config.calcMetrics)
                ClientMetrics.writeCloudTime(dis.readLong(), originalQuery);
            
            dis.close();
            sock.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return searchResults;
    }
    
    public void processResults() {
        System.out.println("\nSearch Results:");
        
        for (String result : searchResults) {
            String[] split = result.split(" ");
            System.out.println(split[0] + " has score: " + split[1]);
        }
    }


}
