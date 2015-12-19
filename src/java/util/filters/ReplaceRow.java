/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.filters;

/**
 *
 * @author santos
 */
public class ReplaceRow {

    int id = -1;//id del valor
    String value = "";//valor inicial
    String replace = "";//reemplazo
    int idAttribute = -1;//id del atributo
    String attributeName = "";//reemplazo

    public ReplaceRow(int idValue, String valueOfAttribute, String replaceValue, int idAttribute) {
        id = idValue;
        value = valueOfAttribute;
        replace = replaceValue;
        this.idAttribute = idAttribute;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdAttribute() {
        return idAttribute;
    }

    public void setIdAttribute(int idAttribute) {
        this.idAttribute = idAttribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getReplace() {
        return replace;
    }

    public void setReplace(String replace) {
        this.replace = replace;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

}
