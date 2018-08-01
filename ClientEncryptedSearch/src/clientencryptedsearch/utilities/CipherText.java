package clientencryptedsearch.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class CipherText {
    
    //KeyPair for RSA encryption
    private KeyPair rsaKeys;
    
    /**
     * Default Constructor.
     * Initializes the keys and everything needed for encryption.
     * If this is the first time running (loading fails) a new key will generate.
     */
    public CipherText() {
        //Attempt to load in the keys
        boolean success;
        try {
            rsaKeys = loadKeyPair(Constants.encryptionKeysLocation, "RSA");
            success = true;
        } catch (IOException ex) {
            success = false;
        } catch (InvalidKeySpecException ex) {
            success = false;
        } catch (NoSuchAlgorithmException ex) {
            success = false;
        }
        
        //If loading the keys didn't work, then we probably just don't have keys.
        //Create those keys, if the user wants to.
        //Ugh, this if tree is a mess.  Too much.  Just know that it creates the keys and then saves them.
        if (!success) {
            if (warnUser("RSA")) {
                //They want to create new keys, so do it!
                rsaKeys = initRSAKeys();
                if (rsaKeys == null) {
                    System.err.println("Still can't make keys!  Abort!");
                    System.exit(0);
                } else {
                    System.out.println("New Keys Created!");
                    try {
                        saveKeyPair(Constants.encryptionKeysLocation);
                        System.out.println("New Keys Saved!");
                    } catch (IOException ex) {
                        System.err.println("Error saving encryption keys!");
                    }
                }
            }
            else {
                System.out.println("Goodbye");
                System.exit(0); //Panic
            }
        }
        
        //However you slice it, if you got here, you got keys.
    }
    
    public String encrypt_RSA(String inputStr) {
        String encryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            
            cipher.init(Cipher.ENCRYPT_MODE, rsaKeys.getPublic());
            byte[] encryptedBytes = cipher.doFinal(inputStr.getBytes());
            encryptedText = new String(Base64.getEncoder().encode(encryptedBytes));
            
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(CipherText.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return encryptedText;
    }
    
    public String decrypt_RSA(String encryptedStr) {
        String plaintext = "";
        
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            
            cipher.init(Cipher.DECRYPT_MODE, rsaKeys.getPrivate());
            byte[] ciphertextBytes = Base64.getDecoder().decode(encryptedStr.getBytes());
            byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
            
            plaintext = new String(decryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(CipherText.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return plaintext;
    }

    public void encryptFile_DES(String key, String input, String output) throws Throwable {
        encryptOrDecrypt(key, Cipher.ENCRYPT_MODE, input, output);
    }

    public void decryptFile_DES(String key, String input, String output) throws Throwable {
        encryptOrDecrypt(key, Cipher.DECRYPT_MODE, input, output);
    }

    private void encryptOrDecrypt(String key, int mode, String input, String output) throws Throwable {

        //Read the file
        File file = new File(input);
        FileInputStream is = new FileInputStream(file);
        file = new File(output);
        if(file.exists()) {
                file.delete();
        }
        file.createNewFile();

        FileOutputStream os = new FileOutputStream(file);

        DESKeySpec dks = new DESKeySpec(key.getBytes());
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        SecretKey desKey = skf.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES"); // DES/ECB/PKCS5Padding for SunJCE

        if (mode == Cipher.ENCRYPT_MODE) {
                cipher.init(Cipher.ENCRYPT_MODE, desKey);
                CipherInputStream cis = new CipherInputStream(is, cipher);
                doCopy(cis, os);
        } else if (mode == Cipher.DECRYPT_MODE) {
                cipher.init(Cipher.DECRYPT_MODE, desKey);
                CipherOutputStream cos = new CipherOutputStream(os, cipher);
                doCopy(is, cos);
        }
    }

    public static void doCopy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[64];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
                os.write(bytes, 0, numBytes);
        }
        os.flush();
        os.close();
        is.close();
    }

    /**
     * Hash File Contents.
     * Goes through a file line by line, hashing each line as a string.
     * Then writes each string to another keyfile of the same name in the
     * temporary directory where the encrypted files are being held.
     * @param filePath Absolute path of the file
     * @param fileName Name of the file
     * @throws IOException 
     */
    public void HashFileContents(String filePath, String fileName) throws IOException{
        //Prep to read the file
        File file = new File(filePath);
        FileInputStream is = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        //Each line in the file is a key phrase we're looking to hash
        List<String> hashedPhrases = new ArrayList<String>();
        String keyPhrase;
        while ((keyPhrase = br.readLine()) != null) {
            Integer hash = keyPhrase.toLowerCase().hashCode();
            hashedPhrases.add(hash.toString());
        }

        //Now hashedPhrases has a collection of strings representing the hashed keyphrases
        String outputFile = Constants.tempLocation + File.separator
                + fileName; //Where we're putting the new file
        Path outputPath = Paths.get(outputFile);
        //Actuall write to the file
        Files.write(outputPath, hashedPhrases, Charset.forName("UTF-8"));

    }
        
    private KeyPair loadKeyPair(String path, String algorithm) throws FileNotFoundException, IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        FileInputStream fis;
        //Read pub key
        File pubKeyFile = new File(path + File.separator + "public.key");
        fis = new FileInputStream(path + File.separator + "public.key");
        byte[] encodedPubKey = new byte[(int) pubKeyFile.length()];
        fis.read(encodedPubKey);
        fis.close();

        //Read priv key
        File privKeyFile = new File(path + File.separator + "private.key");
        fis = new FileInputStream(path + File.separator + "private.key");
        byte[] encodedPrivKey = new byte[(int) privKeyFile.length()];
        fis.read(encodedPrivKey);
        fis.close();

        //Generate Keys
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encodedPubKey);
        PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        return new KeyPair(publicKey, privateKey);
    }
    
    private void saveKeyPair(String path) throws IOException {
        FileOutputStream fos;
        PublicKey pub = rsaKeys.getPublic();
            PrivateKey priv = rsaKeys.getPrivate();
            //Store public key
            X509EncodedKeySpec x509 = new X509EncodedKeySpec(pub.getEncoded());
            fos = new FileOutputStream(path + File.separator + "public.key");
            fos.write(x509.getEncoded());
            fos.close();
            
            //Store Private key
            PKCS8EncodedKeySpec pkcs = new PKCS8EncodedKeySpec(priv.getEncoded());
            fos = new FileOutputStream(path + File.separator + "private.key");
            fos.write(pkcs.getEncoded());
            fos.close();
    }
    
    private boolean warnUser(String algorithm) {
        System.out.println("ERROR: No Keys Found for " + algorithm + " Encryption. \n"
                + "Do you want to create new ones?\n"
                + "y - sure why not\n"
                + "n - no also quit");
        
        Scanner scan = new Scanner(System.in);
        String choice = scan.nextLine();
        return ("y".equals(choice.trim()) || "yes".equals(choice.trim()));
    }
    
    private KeyPair initRSAKeys() {
        KeyPair keyPair = null;
        try {
            keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algorithm not supported! " + e.getMessage() + "!");
        }
 
        return keyPair;
    }
}