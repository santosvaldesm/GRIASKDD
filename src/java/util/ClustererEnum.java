/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 * This class handles enumerators related with closures
 *
 * @author SANTOS
 */
public enum ClustererEnum {

    Canopy,
    Cobweb,
    EM,
    FarthesFirst,
    //FilteredClusterer,
    HierarchicalClusterer,
    //MakeDensityBasedClusterer,
    SimpleKMeans,
    NOVALUE;

    public static ClustererEnum convert(String str) {
        try {
            return valueOf(str);
        } catch (IllegalArgumentException ex) {
            return NOVALUE;
        }
    }
}
