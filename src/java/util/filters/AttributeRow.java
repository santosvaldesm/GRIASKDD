/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.filters;

import java.util.ArrayList;
import java.util.List;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.Utils;

/**
 *
 * @author santos
 */
public class AttributeRow {

    private int idAttribute = -1;
    private String attributeName = "";
    private String type = "";
    private String missing = "";
    private int missingInt = 0;
    private String distinct = "";
    private String unique = "";
    private boolean selected = true;
    private AttributeStats attributeStats;
    private List<ValueRow> listValuesData = new ArrayList<>();

    protected boolean m_allEqualWeights = true;
    boolean isNumericData = false;//mostar tabla numerica
    boolean isNominalData = false;//mostar tabla nominal
    boolean isOtherData = false;//mostar tabla vacia

    public AttributeRow(Instances in, int idAttribute) {
        this.idAttribute = idAttribute;
        if (in != null) {
            attributeStats = in.attributeStats(idAttribute);
            this.idAttribute = in.attribute(idAttribute).index();
            attributeName = in.attribute(idAttribute).name();
            type = Attribute.typeToString(in.attribute(idAttribute));
            selected = true;
            long percent = Math.round(100.0 * attributeStats.missingCount / attributeStats.totalCount);
            missing = "" + attributeStats.missingCount + " (" + percent + "%)";
            missingInt = attributeStats.missingCount;
            percent = Math.round(100.0 * attributeStats.uniqueCount / attributeStats.totalCount);
            unique = "" + attributeStats.uniqueCount + " (" + percent + "%)";
            distinct = "" + attributeStats.distinctCount;
            //////////////////////////////////////////////
            m_allEqualWeights = true;
            double w = in.instance(0).weight();
            for (int i = 1; i < in.numInstances(); i++) {
                if (in.instance(i).weight() != w) {
                    m_allEqualWeights = false;
                    break;
                }
            }
            //////////////////////////////////////////////
            if (attributeStats.nominalCounts != null) {
                for (int i = 0; i < attributeStats.nominalCounts.length; i++) {
                    listValuesData.add(new ValueRow(i + 1, in.attribute(idAttribute).value(i), attributeStats.nominalCounts[i], new Double(Utils.doubleToString(attributeStats.nominalWeights[i], 3)), ""));
                    if (i > 197) {//no agregar mas de mil valores
                        listValuesData.add(new ValueRow(200, "Too values...", 0, 0, ""));
                        break;
                    }
                }
                isNominalData = true;
            } else if (attributeStats.numericStats != null) {
                listValuesData.add(new ValueRow(0, "Minimum", 0, 0.0, Utils.doubleToString(attributeStats.numericStats.min, 3)));
                listValuesData.add(new ValueRow(2, "Maximum", 0, 0.0, Utils.doubleToString(attributeStats.numericStats.max, 3)));
                listValuesData.add(new ValueRow(0, "Mean" + ((!m_allEqualWeights) ? " (weighted)" : ""), 0, 0.0, Utils.doubleToString(attributeStats.numericStats.mean, 3)));
                listValuesData.add(new ValueRow(2, "StdDev" + ((!m_allEqualWeights) ? " (weighted)" : ""), 0, 0.0, Utils.doubleToString(attributeStats.numericStats.stdDev, 3)));
                isNumericData = true;
            } else {
                isOtherData = true;
                //solo se muestra estadisticad de numericos y nominales aqui los datos quedan vacios
            }
        }
    }

    public AttributeStats getAttributeStats() {
        return attributeStats;
    }

    public void setAttributeStats(AttributeStats attributeStats) {
        this.attributeStats = attributeStats;
    }

    public int getIdAttribute() {
        return idAttribute;
    }

    public void setIdAttribute(int idAttribute) {
        this.idAttribute = idAttribute;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getMissing() {
        return missing;
    }

    public void setMissing(String missing) {
        this.missing = missing;
    }

    public String getDistinct() {
        return distinct;
    }

    public void setDistinct(String distinct) {
        this.distinct = distinct;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public List<ValueRow> getListValuesData() {
        return listValuesData;
    }

    public void setListValuesData(List<ValueRow> listValuesData) {
        this.listValuesData = listValuesData;
    }

    public boolean isIsNumericData() {
        return isNumericData;
    }

    public void setIsNumericData(boolean isNumericData) {
        this.isNumericData = isNumericData;
    }

    public boolean isIsNominalData() {
        return isNominalData;
    }

    public void setIsNominalData(boolean isNominalData) {
        this.isNominalData = isNominalData;
    }

    public boolean isIsOtherData() {
        return isOtherData;
    }

    public void setIsOtherData(boolean isOtherData) {
        this.isOtherData = isOtherData;
    }

    public int getMissingInt() {
        return missingInt;
    }

    public void setMissingInt(int missingInt) {
        this.missingInt = missingInt;
    }

}
