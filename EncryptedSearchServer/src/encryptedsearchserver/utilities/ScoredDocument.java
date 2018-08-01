/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.utilities;

import java.util.Comparator;

/**
 *
 * @author Jason
 */
public class ScoredDocument implements Comparable<ScoredDocument>{
        public String docName;
        public double score;

        public ScoredDocument(String n, double s) {
            docName = n;
            score = s;
        }
        
        
        @Override
        public int compareTo(ScoredDocument o) {
            return o.score > this.score ? 1 : o.score < this.score ? -1 : 0;
        }
        public static final Comparator<ScoredDocument> DocComparator = new Comparator<ScoredDocument>() {

            public int compare(ScoredDocument doc1, ScoredDocument doc2) {
                return doc1.compareTo(doc2);
            }
        };
        
};
    

