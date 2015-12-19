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
public enum AssociatorEnum {

    Apriori,
    FpGrowth,
    EquipAsso,
    MateTree,
    NOVALUE;

    public static AssociatorEnum convert(String str) {
        try {
            return valueOf(str);
        } catch (IllegalArgumentException ex) {
            return NOVALUE;
        }
    }
}
