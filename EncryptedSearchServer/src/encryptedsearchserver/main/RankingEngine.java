/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Config;
import encryptedsearchserver.utilities.ScoredDocument;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Implementation of the BM25 algorithm adjusted for encrypted data.
 * @author Jason
 */
public class RankingEngine {
    private double avgDocLength;
    private int numDocuments;
    Index index;
    
    public RankingEngine(Index in) {
        index = in;
        
        //Gather corpus info from index's data
        numDocuments = index.documentSizes.size();
        long totalSize = 0;
        for (long size : index.documentSizes.values()) 
            totalSize += size;
        avgDocLength = (float)totalSize / numDocuments;
    }
    
    
    public ArrayList<String> ScoreAllDocuments(HashMap<String, Integer> relatedDocs, HashMap<String, Float> query) {
        ArrayList<ScoredDocument> scoredDocs = new ArrayList<>();
        ArrayList<String> orderedDocs = new ArrayList<>();
        
        //Get scores for all related docs
        for (String docName : relatedDocs.keySet()) 
            scoredDocs.add(new ScoredDocument(docName, ScoreSingleDocument(docName, query)));
        
        Collections.sort(scoredDocs, ScoredDocument.DocComparator);
        
        //Turn the list of scored docs into an array of strings with doc and score
        // TODO: Just use a max heap, then only pop off 10 or so.
        for (ScoredDocument doc : scoredDocs) {
            orderedDocs.add(doc.docName + " " + doc.score);
        }
        
        return orderedDocs;
    }
    
    /**
     * Gives a score to a given document against the whole query.
     * @param docName name of document being scored.
     * @param query query with weights.
     * @return 
     */
    public double ScoreSingleDocument(String docName, HashMap<String, Float> query) {
        double score = 0;
        
        for (String term : query.keySet()) {
            score += computeBM25(docName, term, query.get(term));
        }
        
        return score;
    }
    
    /**
     * Actually do the BM25 calculation.
     * @param docName Name of document being scored
     * @param queryTerm Which term in the query we're evaluating
     * @param termWeight Weight of that term in the query
     * @return Score for the document against the query term
     */
    public double computeBM25(String docName, String queryTerm, float termWeight) {
        // Compute IDF
        HashMap<String, Integer> files = index.postingList.get(queryTerm);
        double IDF;
        if (files != null) {
            int IDFofTerm = index.postingList.get(queryTerm).keySet().size();
            double IDFEquation = (numDocuments - IDFofTerm + .5) / (IDFofTerm + .5);
            IDF = Math.log10(IDFEquation);
        } else return 0;
        
        // Get term frequency in this doc
        float TF = (index.postingList.get(queryTerm).getOrDefault(docName, 0));
        
        // Get document length normalization.  Checks it against the average for normalization.
        double DLN = Config.k1 * (1 - Config.b + (Config.b * (index.documentSizes.get(docName) / avgDocLength)));
        
        double score = IDF * ((TF * (Config.k1+ 1)) / (TF + DLN));
        
        return score;
    } 
}
