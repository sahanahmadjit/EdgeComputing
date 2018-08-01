/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.main;

import clientencryptedsearch.utilities.*;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//import jdk.nashorn.internal.parser.Lexer;

/**
 * Uploader.
 * This class is meant to perform an upload to the server.
 * It will take in a folder that the user has specified, prepare the files for upload,
 * and put them on the server location.
 * @author jason
 */
public class BatchUploader {
    //Location being uploaded
    String path = null;
    //Extractor for getting key phrases from files.
    ExtractKeyPhrases extractor = null;
    //Cipher for encrypting and decrypting text
    CipherText cipher = null;

    /**
     * Uploader Constructor.
     * Makes a new keyphrase extractor and cipher.
     * Does not do much.
     */
    public BatchUploader() {
        //Sets up Maui to extract phrases.  Options are given at extraction call time.
        extractor = new ExtractKeyPhrases();
        //Sets up cipher.  Loads in keys for proper encryption.
    }
    
    /**
     * Default upload.
     * Calls the real upload with the default upload option to just use file moving,
     * instead of actual network uploading.
     * @param location Location of the files to be uploaded.
     * @return Success or failure
     */
    public boolean batchUpload(String location) {
        boolean success = batchUpload(location, "-f");
        return success;
    }
    
    /**
     * Parameterized Upload.
     * Attempts to upload documents in the supplied, verified location.
     * 
     * Steps:
     * * Extracts key phrases from files in the given path and puts them in .key files in that same path.
     *   - These files contain 10 key phrases on one line each
     * * Moves all resulting files to the desired location
     *   - Method of movement is based on inputted option.
     * @param location Absolute path to files to be uploaded
     * @param uploadType Flag for how the files should be uploaded.  Options: -f Files.Move based, -n Actual network upload
     * @return Success or failure
     */
    public boolean batchUpload(String location, String uploadType) {
        boolean success = true;
        path = location;
        
        //Start measurement for extraction
        long start = System.currentTimeMillis();
        
        
        // See if we actually need to do the extractions
        // Get a list of current files, and if there are equal keys and texts, skip the extraction.
        List<String> files = Util.getAbsoluteFilePathsFromFolder(path);
        int txtCount = 0; int keyCount = 0;
        for (String file : files) {
            if (file.endsWith(".txt"))
                txtCount++;
            else
                keyCount++;
        }
        
        if (txtCount != keyCount) {
            //Get options desired for Maui.  Replaces "data/tmp" with the actual path
            String[] options = Constants.getMauiExtractionOptions(path);
            //Attempt to extract keywords with those options.
            try {
                //Make the .key files.
                extractor.extract(options);
            } catch(Exception e) {
                System.err.println("Problem extracting from Maui. \nFrom: Uploader");
                return false;
            }
        }
        
        System.out.println("Already done extraction on this folder");
        
        //End timing for extraction
        long end = System.currentTimeMillis();
        System.out.println("Keyword Extraction took " + (end - start) + " ms.");
        
        //Now we have the keyfile with 10 keywords for each file.
        //Get all the files in the input folder, including new keyfiles.
       files = Util.getAbsoluteFilePathsFromFolder(path);

        //Act on each file based on its file type
        for (String file : files) {

            //For key files
            if (file.endsWith(".key")) {


                String[] fileNameWithpath = file.split(File.separator);
                String fileName = fileNameWithpath[fileNameWithpath.length-1];
                System.out.println("Uploading file: " + fileName);
                Path filePath = Paths.get(file);
                Path uploadPath = Paths.get(Constants.batchUploadLocation + File.separator + fileName);

               try {
                    Files.move(filePath, uploadPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    System.err.println(this.getClass().getName() + ": Error moving file " + file + "!");
                    ex.printStackTrace();
                }

            }


        }
        System.out.println("upload function execution end here");
        return success;
    }
    



}
