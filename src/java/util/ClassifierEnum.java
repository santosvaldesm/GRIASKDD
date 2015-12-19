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
public enum ClassifierEnum {

    J48,
    ID3,
    LMT,
    M5P,
    DecisionStump,
    HoeffdingTree,    
    RandomForest,
    RandomTree,
    REPTree,
    NOVALUE;

    public static ClassifierEnum convert(String str) {
        try {
            return valueOf(str);
        } catch (IllegalArgumentException ex) {
            return NOVALUE;
        }
    }
}
