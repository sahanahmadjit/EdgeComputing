/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptedsearchserver.main;

import java.util.HashMap;

/**
 * A very simple bean meant to hold info for a centroid.
 * For the partitioning.
 * @author Jason
 */
public class Centroid {
    public String term;
    public HashMap<String, Integer> files;
    
    @Override
    public String toString() {
        return term + ": " + files;
    }
}
