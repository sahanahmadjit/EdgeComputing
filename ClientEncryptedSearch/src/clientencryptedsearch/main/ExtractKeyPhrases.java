/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;
import maui.main.MauiModelBuilder;
import maui.main.MauiTopicExtractor;

/**
 * Extract Key Phrases.
 * 
 * A class for extracting keywords from a file.
 * Uses the Maui model to extract the keywords from a document and put them
 * into a .key file.
 * @author Jason
 */
class ExtractKeyPhrases {
    //Maui options
    private String opts[];
    
    private MauiModelBuilder modelBuilder;
    private MauiTopicExtractor topicExtractor;
    
    /**
     * Set Options.
     * Set internal maui options array to passed options.
     * @param opts String array of Maui options
     */
    public void setOptions(String [] opts) {
        this.opts = opts;
    }
    
    /**
     * Get Options.
     * Returns the internal Maui options.
     * @return String array of Maui Options
     */
    public String[] getOptions() {
        return opts;
    }
    
    /**
     * Extract Key Phrases Constructor.
     * 
     * Sets new maui model builder and topic extractor
     * Called by the Uploader Constructor
     */
    public ExtractKeyPhrases() {
        modelBuilder = new MauiModelBuilder();
        topicExtractor = new MauiTopicExtractor();
        //No docs on these things 
    }

    
    /**
     * Extract.
     * Uses Maui to extract key phrases from the files in the given path.
     * Creates .key files.
     * @param options 
     */
    void extract(String[] options) {
        this.opts = options;
        if (opts != null) {
            topicExtractor = new MauiTopicExtractor();
            //buildModel();
            //System.out.println(getOptions());
            topicExtractor.topicExtractor(options);
        } else {
            System.err.println("Please provide options for Maui");
        }
    }
}
