/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.utilities;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility Functions.
 * Static functions that are just useful.
 * @author jason
 */
public class Util {
    /**
     * Get Files From Path.
     * Returns a list of files in the folder specified by the absolute path.
     * @param absPath Absolute path for the folder.
     * @return A list of files in the folder.
     */
    public static List<String> getAbsoluteFilePathsFromFolder(String absPath) {
        File dir = new File(absPath);
        List<String> files = new ArrayList<>();
        
        //Make sure the given path was a folder, not just a file
        if (dir.isDirectory()) {
            //get an array of relative file names in the folder
            String[] contents = dir.list();
            for (String filename : contents) {
                files.add(absPath + File.separator + filename); //Give each file its full pathname
            }
        } else {  //If it's just a file... just return the file
            files.add(absPath);
        }
        
        return files;
    }
    
    /**
     * Get the relative file name from an absolute file name.
     * Basically just strips off the path part, giving you that sweet, swett file name.
     * @param absPath Absolute path of the file.
     * @return Relative file name.
     */
    public static String getRelativeFileNameFromAbsolutePath(String absPath) {
        Path p = Paths.get(absPath);
        return p.getFileName().toString();
    }
}
