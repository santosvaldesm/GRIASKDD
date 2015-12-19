/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 * This class is used by the imputation class which is responsible for managing
 * everything related to data imputation.
 *
 * @author and
 */
public class ModeNumeric {

    private int count;
    private double value;

    public ModeNumeric(double value,int count) {
        this.count=count;
        this.value=value;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
