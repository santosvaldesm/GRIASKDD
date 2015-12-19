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
public enum NodeTypeEnum {

    Default,
    PlainText,//"DATA SOURCE"-------------------------------
    ConnectionDB,
    ArfSaver,//"DATA SAVER"-------------------------------
    CsvSaver,
    Selection,//FILTER > SELECTION"-------------------------------
    SelectAttributes,
    RemoveMissing,//FILTER > CLEAN"-------------------------------
    UpdateMissing,
    ReplaceValue,
    SamplingPercentage,
    KNNImputation,
    Metrics,    
    //    NumericRange,
    Discretize,//FILTER > TRANSFORMATION"-------------------------------
    NumericToNominal,
    Codification,
    NominalToBinary,
    //Apriori,//"DATA MINING > ASSOCIATION"-------------------------------
    //FPGrowth,
    //EquipAsso,
    Association,
    Classification,//"DATA MINING > CLASIFICATION"-------------------------------    
    Cluster,//"CLUSTER"-------------------------------    
    DataAnalisis,//"VIEWS"-------------------------------
    Generator,
    HierarchicalTree,
    WekaTree,
    TextTree,
    Prediction,
    NOVALUE;

    public static NodeTypeEnum convert(String str) {
        try {
            return valueOf(str);
        } catch (IllegalArgumentException ex) {
            return NOVALUE;
        }
    }
}
