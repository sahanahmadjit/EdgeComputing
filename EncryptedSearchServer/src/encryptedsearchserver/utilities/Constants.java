/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.utilities;

import java.io.File;

/**
 *
 * @author Jason
 */
public class Constants {
    //Location of the config properties file
    public static String configLocation = "src" + File.separator + "encryptedsearchserver" + File.separator + "utilities" + File.separator;
    //public static String configLocation = "utilities" + File.separator;
    
    //Location of the directory being watched.
    public static String watchLocation = ".." + File.separator + "cloud" +
            File.separator + "cloudserver" + File.separator + "watch";
    
    //Location of the directory to move files to.
    public static String storageLocation = ".." + File.separator + "cloud" +
            File.separator + "cloudserver" + File.separator + "storage";
    
    public static String utilitiesLocation = ".." + File.separator + "cloud" +
            File.separator + "cloudserver" + File.separator + "utilities";
    
    //Location of the directory to put the clusters in.
    public static String clusterLocation = utilitiesLocation + File.separator +
            "clusters";
    
    //Name of the index file
    public static String indexFileName = "Index.txt";
    
    //DocSizes file name.
    public static String docSizesFileName = "DocSizes.txt";
    
    public static String modelName = "S3C";
    
    public static String regexIndexDelimiter = "\\|.\\|"; //For reading from the index
    public static String indexDelimiter = "|.|"; //For writing to the index
}
