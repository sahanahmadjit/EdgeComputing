/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientencryptedsearch.main;

import clientencryptedsearch.utilities.CipherText;
import clientencryptedsearch.utilities.Config;
import clientencryptedsearch.utilities.Constants;
import clientencryptedsearch.utilities.StopwordsRemover;
import clientencryptedsearch.utilities.Util;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
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
public class Uploader {
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
    public Uploader() {
        //Sets up Maui to extract phrases.  Options are given at extraction call time.
        extractor = new ExtractKeyPhrases();
        //Sets up cipher.  Loads in keys for proper encryption.
        cipher = new CipherText();
    }
    
    /**
     * Default upload.
     * Calls the real upload with the default upload option to just use file moving,
     * instead of actual network uploading.
     * @param location Location of the files to be uploaded.
     * @return Success or failure
     */
    public boolean upload(String location) {
        boolean success = upload(location, "-f");
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
    public boolean upload(String location, String uploadType) {
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
                //NOTE: This could probably all be consolidated into one function with a bunch of options...
                //Would speed up upload process a bit since we wouldn't read the same file over and over.
                
                //If we want to split the key file's key phrases into individual words, do so.
                if (Config.splitKeywords)
                    splitKeywords(file);
                
                //If we want to get the upload side semantics, start that now.
                if (Config.uploadSideSemantics)  //sahan value change to false
                    addKeySemantics(file);
                
                //If we want to count actual frequencies, do so.  If not, just give them all 1.
                if (Config.countFrequencies)
                    getWordCountAndKeyFrequencies(file);
                else 
                    getWordCountAndConstantFrequency(file);
                
                //Finally, encrypt the info in it.
                if (Config.encryptKeyPhrases)
                    encryptKeyFile(Constants.tempLocation + File.separator + Util.getRelativeFileNameFromAbsolutePath(file));
            }
            //For regular text files
            else if (file.endsWith(".txt")) {
                //Just encryptFile_DES the file, uploading will be dealt with later.
                encryptFile(file);
            }


        }
        
        //Now perform batch upload on all files based on the chosen upload option
        switch (uploadType) {
            case "-f":
                uploadAllLocalType();
                break;
            case "-n":
                uploadAllNetworkType();
                break;
            default:
                uploadAllLocalType();
                break;
        }
        System.out.println("upload function execution end here");
        return success;
    }
    
    private void splitKeywords(String absFilePath) {
        StopwordsRemover stop = new StopwordsRemover("stopwords_en.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(absFilePath));
            LinkedHashSet<String> lines = new LinkedHashSet<>();
            
            //Go through the file, appending and splitting as it goes
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                //Being a hash set should take care of the possible repetition
                lines.add(currentLine);
                
                String[] split = currentLine.split(" ");
                for (String word : split) {
                    lines.add(word);
                }
            }
            br.close();
            
            //Remove stopwords from the split keywords
            //We When we split, we might end up with some undesirebale middle phrases like "the" or "with"
            stop.remove(lines);
            
            //Write everything just added to the hash set to the file
            BufferedWriter bw = new BufferedWriter(new FileWriter(absFilePath));
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            
            bw.close();
        } catch (IOException e) {
            System.err.println("Error splitting keywords in file " + absFilePath);
        }
    }
    
    /**
     * Find word count and count key phrase frequencies.
     * Goes through the file and counts how many times each of the key phrases appear,
     * as well as getting an overall word count.  This is info is then written back 
     * to the key file in the form of:
     * 
     * word count
     * term|.|freq
     * term|.|freq
     * ...
     * 
     * The |.| separator is just so that we can split the count from the terms easily later on.
     * This is written to the temp directory so as to leave the original key file untouched.
     * @param absKeyFilePath absolute file path for the key file
     */
    private void getWordCountAndKeyFrequencies(String absKeyFilePath) {
        try {
            String fileName = Util.getRelativeFileNameFromAbsolutePath(absKeyFilePath);
            BufferedReader br = new BufferedReader(new FileReader(absKeyFilePath));
            HashSet<String> phrases = new HashSet<>();
            
            //Go through the file, adding each phrase to the set
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                phrases.add(currentLine.toLowerCase());
            }
            br.close();
            
            //Now that we' have the hash set with the phrases,
            //we need to read the text file to count their frequency
            String textFilePath = absKeyFilePath.replace(".key", ".txt");
            br = new BufferedReader(new FileReader(textFilePath));
            
            StringBuilder strbuild = new StringBuilder();
            while ((currentLine = br.readLine()) != null) {
                strbuild.append(currentLine);
                strbuild.append(" ");
            }
            br.close();
            
            //This string represents all of the text in the file.
            //TODO: Find a better way to represent the file throughout reading...
            String fileText = strbuild.toString();
            strbuild = null;
            String tokens[] = (fileText.replaceAll("[^a-zA-Z ]", "")).toLowerCase().split("\\s+");
            fileText = null;
            
            //Map for the counting
            HashMap<String, Integer> freqMap = new HashMap<>();
            
            //Find the largest phrase in the keyset, set out maximum # of words to that.
            int maxWords = 1;
            for (String phrase : phrases) {
                String words[] = phrase.split(" ");
                if (maxWords < words.length)
                    maxWords = words.length;
            }
            
            //Now go through the entire text of the file, finding the counts of phrases.
            //i represents the spot in the line we're working with
            for (int i = 0; i < tokens.length - maxWords + 1; ++i) {
                String phrase = "";
                //j represents how long the phrase should grow to.
                for (int j = 1; j <= maxWords; j++) {
                    StringBuilder strb = new StringBuilder();
                    //k represents the actual size of the phrase we're extracting from the tokens
                    for (int k = 0; k < j; k++) {
                        strb.append(tokens[i+k]);
                        //Don't put a space at the end of the phrase.
                        if (k != j - 1) 
                            strb.append(" ");
                    }
                    phrase = strb.toString();
                    
                    if (phrases.contains(phrase)) {
                        if (freqMap.containsKey(phrase)) {
                            int freq = freqMap.get(phrase);
                            freq++;
                            freqMap.put(phrase, freq);
                        } else {
                            freqMap.put(phrase, 1);
                        }
                    }
                }
            }
            //freqMap should have frequency count for each key phrase.
            
            //Key file must be written to in the temp location so it works with the uploader
            System.out.println("Counting file " + fileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.tempLocation + File.separator
                    + fileName));
            
            //First write the word count.
            bw.write(Integer.toString(tokens.length));
            bw.newLine();
            
            //Write the phrases to the file now, separated from freq info by the |.| separator
            for (String phrase : freqMap.keySet()) {
                bw.write(phrase);
                bw.write(Constants.indexDelimiter + freqMap.get(phrase));
                bw.newLine();
            }
            
            bw.close();
        } catch (IOException e) {
            System.err.println("Error reading frequencies for " + absKeyFilePath + "\nFrom: Uploader");
        }
        
        
    }

    /**
     * Get Word Count and Constant Frequency.
     * Does the same as its counterpart, but just puts 1 in the frequency
     * info as opposed to the actual frequency.
     * @param absKeyFilePath 
     */
    private void getWordCountAndConstantFrequency(String absKeyFilePath) {
        try {
            String fileName = Util.getRelativeFileNameFromAbsolutePath(absKeyFilePath);
            BufferedReader br = new BufferedReader(new FileReader(absKeyFilePath));
            HashSet<String> phrases = new HashSet<>();
            
            //Go through the file, adding each phrase to the set
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                phrases.add(currentLine.toLowerCase());
            }
            br.close();
            
            //Now that we' have the hash set with the phrases,
            //we need to read the text file to count their frequency
            String textFilePath = absKeyFilePath.replace(".key", ".txt");
            br = new BufferedReader(new FileReader(textFilePath));
            
            StringBuilder strbuild = new StringBuilder();
            while ((currentLine = br.readLine()) != null) {
                strbuild.append(currentLine);
                strbuild.append(" ");
            }
            br.close();
            
            //This string represents all of the text in the file.
            //Let's hope it's small enough for memory...
            String fileText = strbuild.toString();
            strbuild = null;
            String tokens[] = (fileText.replaceAll("[^a-zA-Z ]", "")).toLowerCase().split("\\s+");
            fileText = null;
            
            //Key file must be written to in the temp location so it works with the uploader
            System.out.println("Counting file " + fileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.tempLocation + File.separator
                    + fileName));
            
            //First write the word count.
            bw.write(Integer.toString(tokens.length));
            bw.newLine();
            
            //Write the phrases to the file now, separated from freq info by the |.| separator
            for (String phrase : phrases) {
                bw.write(phrase);
                bw.write(Constants.indexDelimiter + 1);
                bw.newLine();
            }
            
            bw.close();
        } catch (IOException e) {
            System.err.println("Error reading frequencies for " + absKeyFilePath + "\nFrom: " + this.getClass().getName());
        }
    }
    
    private void encryptFile(String absFilePath) {
        String fileName = Util.getRelativeFileNameFromAbsolutePath(absFilePath);
        System.out.println("Encrypting text file: " + fileName);
        try {
            //Encrypt the input file using our cipher
            cipher.encryptFile_DES(Constants.cipherKey, absFilePath, Constants.tempLocation + File.separator + fileName);
        } catch (Throwable ex) {
            System.err.println("Error encrypting file: " + fileName +
                    "\nFrom: " + this.getClass().getName());
        }
    }
    
    /**
     * Encrypt a key file.
     * Goes through the given key file and encrypt the strings, while leaving 
     * the frequency information untouched.
     * 
     * For this to work, the key file should be structured like:
     *  wordCount
     *  Plaintext|.|frequency
     *  Plaintext|.|frequency
     * @param absKeyPath 
     */
    private void encryptKeyFile(String absKeyPath) {
        String fileName = Util.getRelativeFileNameFromAbsolutePath(absKeyPath);
        System.out.println("Encrypting key file: " + fileName);
        
        //Holds the info to be written back to the file later.
        HashMap<String, Integer> encFreqMap = new HashMap<>();
        long wordCount = 0;
        
        //Try reading
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(absKeyPath));
            String currentLine;
            String[] tokens;
            
            //Read one line to get the word count out of the way.
            wordCount = Long.parseLong(br.readLine());
            
            while ((currentLine = br.readLine()) != null) {
                //Extract and encrypt info
                tokens = currentLine.split(Constants.regexIndexDelimiter);
                String encryptedText = cipher.encrypt_RSA(tokens[0]); 
                int freq = Integer.parseInt(tokens[1]);
                
                encFreqMap.put(encryptedText, freq);
            }
            br.close();
        } catch (FileNotFoundException ex) {
            System.err.println(this.getClass().getName() + ": Key File " + absKeyPath + " not found!");
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error reading from key file " + absKeyPath + "!");
        }
        
        
        //Now freqMap should contain all encrypted data mapped to frequency.
        //So try writing back to that same file
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(absKeyPath));
            
            //Write the word count first
            bw.write(Long.toString(wordCount));
            bw.newLine();
            
            //Write all of freqMap to this file
            for (String encTerm: encFreqMap.keySet()) {
                bw.write(encTerm + " " + encFreqMap.get(encTerm));
                bw.newLine();
            }
            
            bw.close();
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error writing back to " + absKeyPath + "!");
        }
        
    }
    
    private void decryptFile(String absKeyPath) {
        String fileName = Util.getRelativeFileNameFromAbsolutePath(absKeyPath);
        System.out.println("Deterministically encrypting key file: " + fileName);
        
        
        //Try reading
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(absKeyPath));
            String currentLine;
            String[] tokens;
            
            //Read one line to get the word count out of the way.
            br.readLine();
            
            while ((currentLine = br.readLine()) != null) {
                //Extract and encrypt info
                tokens = currentLine.split(" ");
                String encryptedText = cipher.decrypt_RSA(tokens[0]); 
                float freq = Float.parseFloat(tokens[1]);
                
                System.out.println("CipherText from " + fileName + ": " + tokens[0]);
                System.out.println("Plaintext from " + fileName + ": " + encryptedText);
            }
            br.close();
        } catch (FileNotFoundException ex) {
            System.err.println(this.getClass().getName() + ": Key File " + absKeyPath + " not found!");
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error reading from key file " + absKeyPath + "!");
        }
    }

    /**
     * Move all files to the specified cloud folder.
     * An upload for local movement of uploaded files.
     * Goes through all files in the temp directory and moves them to the local cloud folder.
     */
    private void uploadAllLocalType() {
        List<String> files = Util.getAbsoluteFilePathsFromFolder(Constants.tempLocation);
        
        for (String file : files) {
            uploadFileLocalType(file);
        }
    }
    
    /**
     * Move a single file to the local cloud folder.
     * @param absFilePath The file being moved.
     */
    private void uploadFileLocalType(String absFilePath) {
        String fileName = Util.getRelativeFileNameFromAbsolutePath(absFilePath);
        System.out.println("Uploading file: " + fileName);
        
        Path filePath = Paths.get(absFilePath);
        Path uploadPath = Paths.get(Constants.uploadLocation + File.separator + fileName);
        
        try {
            Files.move(filePath, uploadPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error moving file " + fileName + "!");
            ex.printStackTrace();
        }
    }

    /**
     * Upload all files through the network.
     * Opens a socket connection to the server, then goes through all files to upload them.
     */
    private void uploadAllNetworkType() {
        //We want to try until we successfully connect to the server
        boolean successfulConnect = false;
        Socket sock = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        Scanner scan = new Scanner(System.in);
        
        while (!successfulConnect) {
            try {
                //Try connecting to the server
                System.out.println("Attempting to connect to " + Config.cloudIP + "...");
                sock = new Socket(Config.cloudIP, Config.socketPort);
                System.out.println("Connecting to " + Config.cloudIP);
                
                successfulConnect = true;
            } catch (Exception ex) {
                System.err.println(this.getClass().getName() + ": Error " + ex.getMessage() + ".  Going to try again?");
                try {
                    Thread.sleep(500);
                } catch(Exception e) {
                    System.err.println("This shouldn't happen.");
                }
            } 
        }
        
        //By the time we get here, we must have a successful connection
        System.out.println("Server accepted connection!");
       
        //Now we want to go through all of those files and upload them
        List<String> files = Util.getAbsoluteFilePathsFromFolder(Constants.tempLocation);
        
        try {
            //First send over how many files we're sending
            //This is determined by if we wanted to send txt files or not
            dos = new DataOutputStream (sock.getOutputStream());
            dis = new DataInputStream(sock.getInputStream());
            if (Config.uploadTxts)
                dos.writeInt(files.size());
            else
                dos.writeInt(files.size() / 2);
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error sending num of files.  Quitting to prevent further harm");
            System.exit(0);
        }
        
        try {
            sock.setKeepAlive(true);
            sock.setSoTimeout(10000);
        } catch (SocketException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (String file : files) {
            // If we don't want txt files, just delete the file and move one
            if (!Config.uploadTxts && file.endsWith("txt")) {
                new File(file).delete();
                continue;
            }
            
            System.out.print("Attempting to upload " + Util.getRelativeFileNameFromAbsolutePath(file) + "... ");
            uploadFileNetworkType(dis, dos, file);
            System.out.println("done!");
        }
        
        try {
            dos.close();
            sock.close();
        } catch (SocketException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean uploadFileNetworkType(DataInputStream dis, DataOutputStream dos, String absFilePath) {
        FileInputStream fis;
        BufferedInputStream bis;
        String fileName = Util.getRelativeFileNameFromAbsolutePath(absFilePath);
        
        try {
            //Send over the file name
            
//            //Read the file in as bytes
            File file = new File(absFilePath);
            byte[] fileBytes = new byte[(int) file.length()];
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bis.read(fileBytes, 0, fileBytes.length);
            
            //Send the file name and the length of the file to server.
            dos.writeUTF(fileName);
            dos.writeInt(fileBytes.length);
            dos.flush();
//            
//            //Now try to write the file out
            dos.write(fileBytes, 0, fileBytes.length);
            dos.flush();
            
            fis.close();
            bis.close();
            
            // Wait for the client to confirm we're done
            dis.readBoolean();
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ":  Error uploading " + fileName + "!  " + ex.getMessage());
            return false;
        } 
        
        return true;
    }

    /**
     * Add Key Phrase Semantics to the Key File.
     * Adds semantics to each phrase in the key file.
     * Does a lookup for synonyms and related terms from wikipedia.
     * @param file 
     */
    private void addKeySemantics(String file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
