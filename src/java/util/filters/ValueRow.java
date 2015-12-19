/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.filters;

import java.awt.Color;

/**
 *
 * @author santos
 */
public class ValueRow {

    private int idValue = -1;//cuando el atributo al que pertenece es Nominal y Numerico
    private String label = "";//cuando el atributo al que pertenece es Nominal y Numerico
    private int count = 0;//cuando el atributo al que pertenece es Nominal
    private double weight = 0.0;//cuando el atributo al que pertenece es Nominal
    private String value = "";//cuando el atributo al que pertenece es Numerico
    private Color color;//color usado para el grafico

    public ValueRow(int idValue, String label, int count, double weight, String value) {
        this.idValue = idValue;
        this.label = label;
        this.count = count;
        this.weight = weight;
        this.value = value;
        this.color = getColorById(idValue);
    }

    private Color getColorById(int id) {
        switch (id % 5) {//TOMA EL MODULO
            case 0:
                return Color.BLACK;//NEGRO
            case 1:
                return Color.RED;
            case 2:
                return Color.YELLOW;
            case 3:
                return Color.BLUE;//AZUL                     
            case 4:
                return Color.GREEN;
            case 5:
                return Color.CYAN;
            case 6:
                return Color.PINK;
            case 7:
                return Color.ORANGE;
            case 8:
                return Color.MAGENTA;
            case 9:
                return Color.LIGHT_GRAY;
            case 10:
                return new Color(139, 69, 19);//CAFE
            case 11:
                return new Color(40, 200, 150);//VERDE PERLA                                      
            case 12:
                return new Color(128, 0, 128);//MORADO                
        }
        return new Color(10, 10, 10);
    }

    public int getIdValue() {
        return idValue;
    }

    public void setIdValue(int idValue) {
        this.idValue = idValue;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
