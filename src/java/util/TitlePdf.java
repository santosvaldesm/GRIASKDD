/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.itextpdf.text.Chunk;

/**
 *
 * @author santos
 */
public class TitlePdf {

    //ESTA CLASE ALMACENA CADA UNO DE LOS TITULOS QUE TIENE EL PDF
    //TAMBIEN ALMACENA LOS NULOS SI SE TRATA DE UN SUBTITULO DE UN ATRIBUTO
    private Chunk localDestination;
    private int pageNumber;
    private String missing; //nulos con cantidad y porcentaje tipo texto
    private int missingInt;//entro con la cantidad de nulos
    private boolean calculateMissing;//si calcul nulos es por que es un subtitulo de un atributo

    public TitlePdf(Chunk localDestination, int pageNumber, String missing, int missingInt, boolean calculateMissing) {
        this.localDestination = localDestination;
        this.pageNumber = pageNumber;
        this.missing = missing;
        this.missingInt = missingInt;
        this.calculateMissing = calculateMissing;
    }

    public Chunk getLocalDestination() {
        return localDestination;
    }

    public void setLocalDestination(Chunk localDestination) {
        this.localDestination = localDestination;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getMissing() {
        return missing;
    }

    public void setMissing(String missing) {
        this.missing = missing;
    }

    public int getMissingInt() {
        return missingInt;
    }

    public void setMissingInt(int missingInt) {
        this.missingInt = missingInt;
    }

    public boolean isCalculateMissing() {
        return calculateMissing;
    }

    public void setCalculateMissing(boolean calculateMissing) {
        this.calculateMissing = calculateMissing;
    }

}
