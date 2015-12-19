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
public class ComparationAttributes {

    String attributeValue; //Nombre del atributo cuando se genera un grafico nominal
    String classValue;     //Nombre del atributo clase cuando se genera un grafico nominal    
    int count = 0;         //Cantidad de instancias con este atributo y clase en un grafico nominal // ó Cantidad de instancias con esta clase y esta en el rango(min a max) en un histograma

    public ComparationAttributes(String attributeValue, String classValue, int count) {
        this.attributeValue = attributeValue;
        this.classValue = classValue;
        this.count = count;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getClassValue() {
        return classValue;
    }

    public void setClassValue(String classValue) {
        this.classValue = classValue;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
