/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.filters;

import java.text.DecimalFormat;

/**
 *
 * @author santos
 */
public class NumericRange {

    private DecimalFormat df = new DecimalFormat("#.##");
    private double min;
    private double max;
    private String rangeStr = "";  //rango compuesto en formato String (min - max)

    public NumericRange(double min, double max) {
        this.min = min;
        this.max = max;
        rangeStr = df.format(min) + "-" + df.format(max);
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public String getRangeStr() {
        return rangeStr;
    }

    public void setRangeStr(String rangeStr) {
        this.rangeStr = rangeStr;
    }

}
