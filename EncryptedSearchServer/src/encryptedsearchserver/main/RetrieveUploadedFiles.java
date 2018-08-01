/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.main;

import encryptedsearchserver.utilities.Config;
import encryptedsearchserver.utilities.Constants;
import encryptedsearchserver.utilities.Util;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Retrive Uploaded Files.
 * 
 * Retrieves the files sent to the server from client when uploaded.
 * Supports both file based moving (used when locally uploaded) and network
 * based moving (used over network... obvsly).
 * 
 * Notes:
 *  To change the storing to an offloaded facility, change the contents of the
 *  storeFile() function.
 * @author Jason
 */
class RetrieveUploadedFiles {
    Index index;
    
    /**
     * Constructor.
     * Just sets the index.  Needed cause this needs a way to write new info
     * to the index lists.
     * @param i 
     */
    public RetrieveUploadedFiles(Index i) {
        index = i;
    }
    
    /**
     * Retrieve new files.
     * Retrieves the files in a manner based on user choice.
     * If the files were uploaded from the client locally with the file move
     * option, choose -f
     * If the files were transported over a socket, choose -n
     * @param choice -f for local upload, -n for network upload
     */
    public void retrieve(String choice) {
        switch (choice) {
            case "-f":
                retrieveWatched();
                break;
            case "-n":
                //Retrieve the file information over the socket, and place it in watched
                retrieveNetworked();
                System.out.println();
                //Then retrieve the files from watched.
                retrieveWatched();
                System.out.println();
                break;
        }
    }
    
    private void retrieveWatched() {
        //Get the names of all fies in the watched directory
        List<String> absFilePaths = Util.getAbsoluteFilePathsFromFolder(Constants.watchLocation);
        
        //Iterate through all them files
        for (String absFile : absFilePaths) {
            String fileName = Util.getRelativeFileNameFromAbsolutePath(absFile);
            //If it's a text file
            if (absFile.endsWith(".txt")) {
                storeFile(fileName);
                System.out.println(fileName + " placed in storage.");
            } 
            //If it's a key file
            else if (absFile.endsWith(".key")) {
                processKeyFile(absFile);
            }
        }
    }
    
    /**
     * Store File.
     * 
     * Moves the given file name from the watch location to the appropriate
     * storage folder.
     * @param fileName Name of file to be moved.
     */
    private void storeFile(String fileName) {
        Path sourcePath = Paths.get(Constants.watchLocation + File.separator + fileName);
        Path storagePath = Paths.get(Constants.storageLocation + File.separator + fileName);
        
        try {
            Files.move(sourcePath, storagePath, REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(this.getClass().getName() + ": Error moving file " + fileName);
        }
    }
    
    private void processKeyFile(String absFilePath) {
        BufferedReader br;
        String textFileName = Util.getRelativeFileNameFromAbsolutePath(absFilePath).replace(".key", ".txt");
        try {
            br = new BufferedReader (new FileReader(absFilePath));
            
            //Read once to get the word count, add that to doc sizes
            long wordCount = Long.parseLong(br.readLine());
            index.addToDocSizes(textFileName, wordCount);
            
            //Read through the rest of it, getting the topics and their freqs
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                String[] tokens = currentLine.split(Constants.regexIndexDelimiter);
                String topic = tokens[0];
                int freq = Integer.parseInt(tokens[1]);
                
                index.addToPostingList(topic, textFileName, freq);
            }
            
            br.close();
            //TODO: Delete key file.
        } catch (FileNotFoundException ex) {
            System.err.println(this.getClass().getName() + ": Could not find " + textFileName + "'s uploaded key file");
        } catch (IOException ex) {
            System.err.println("Error reading from " + textFileName + "'s key file.");
        }
    }

    /**
     * Retrieve packets from the client over sockets.
     * Opens up a server socket and receives a file from the client.
     * The file is then stored in the watched folder.
     */
    private void retrieveNetworked() {
        ServerSocket servsock;
        Socket sock = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        int numFiles = 0;
        
        
        try {
            servsock = new ServerSocket(Config.socketPort);
            System.out.println("Now listening on port " + Config.socketPort);
            sock = servsock.accept();
            System.out.println("Accepted connection to: " + sock);
            
            dis = new DataInputStream(sock.getInputStream());
            dos = new DataOutputStream(sock.getOutputStream());
            numFiles = dis.readInt();
            
            System.out.println("Retrieving " + numFiles + " files.");
            System.out.println();
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": Error reading number of files.  Quitting.");
            System.exit(0);
        }
        
        try {
            sock.setKeepAlive(true);
            sock.setSoTimeout(10000);
        } catch (SocketException ex) {
            Logger.getLogger(RetrieveUploadedFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Read as many times as we had a file
        for (int i = 0; i < numFiles; i++) {
            retrieveAndStoreFile(dis, dos);
        }
        
        try {
            dis.close();
            sock.close();
        } catch (SocketException ex) {
            Logger.getLogger(RetrieveUploadedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RetrieveUploadedFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void retrieveAndStoreFile(DataInputStream dis, DataOutputStream dos) {
        int fileSize;
        String fileName;
        int bytesRead;
        int current = 0;
        BufferedOutputStream bos;
        byte[] fileBytes;
        
        try {
            //Get the file info from client
            fileName = dis.readUTF();
            fileSize = dis.readInt();
            fileBytes = new byte[fileSize];
            
            System.out.print("Attempting to read " + fileName + " from client...");
            
            //Set up the new file
            File file = new File(Constants.watchLocation + File.separator + fileName);
            if (file.exists())
                file.delete();
            file.createNewFile();
            
            
            
            //Set up output stream
            bos = new BufferedOutputStream(new FileOutputStream(file));
            
            //Read the file into the bytes array
            bytesRead = dis.read(fileBytes, 0, fileSize);
            current = bytesRead;
            
            //If the whole thing wasn't read, try reading more
            if (bytesRead != fileSize) {
                do {
                    bytesRead = dis.read(fileBytes, current, (fileBytes.length - current));
                    if (bytesRead >= 0) current += bytesRead;
                } while (bytesRead > -1);
            }
            
            //Write to the new file
            bos.write(fileBytes);
            bos.flush();
            
            bos.close();
            
            
            //Notify the client that we're done so we can continue at the same pace
            dos.writeBoolean(true);
            System.out.println("done!");
        } catch (IOException ex) {
            System.err.println(this.getClass().getName() + ": " + ex.getMessage());
        }
        
    }
}
