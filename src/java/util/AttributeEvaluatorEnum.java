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
public enum AttributeEvaluatorEnum {

    CfsSubsetEval,    
    CorrelationAtributeEval,
    GainRatioAttributeEval,
    InfoGainAttributeEval,    
    OneRAttributeEval,
    PrincipalComponents,
    ReliefFAttributeEval,
    SymetricalUncertAttributeEval,
    WrapperSubsetEval,
    NOVALUE;

    public static AttributeEvaluatorEnum convert(String str) {
        try {
            return valueOf(str);
        } catch (IllegalArgumentException ex) {
            return NOVALUE;
        }
    }
}
