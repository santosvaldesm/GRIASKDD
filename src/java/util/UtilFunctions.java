/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import static org.apache.jasper.Constants.DEFAULT_BUFFER_SIZE;
import org.jfree.chart.JFreeChart;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;
import weka.core.AttributeStats;
import weka.core.Instances;

/**
 *
 * @author santos
 */
public class UtilFunctions {

    public void callGarbageCollector() {
        Runtime garbage = Runtime.getRuntime();
        //System.out.println("Memoria libre antes de limpieza: " + garbage.freeMemory());
        garbage.gc();
        //System.out.println("Memoria libre tras la limpieza: " + garbage.freeMemory());
    }

    public void writePdfInResponse(String path, String nameFile, HttpServletResponse response) {
        //ESCRITURA DE UN ARCHIVO PDF EN response(RESPUESTA DEL SERVIDOR)
        try {
            File file = new File(path, nameFile);       // Open file.            
            try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE)) {
                response.reset();// Init servlet response.
                response.setHeader("Content-Type", "application/pdf");
                response.setHeader("Content-Length", String.valueOf(file.length()));
                response.setHeader("Content-Disposition", "inline; filename=\""+nameFile+"\"");
                BufferedOutputStream output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];// Write file contents to response.
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                output.flush();// Finalize task.
            } // Init servlet response.
        } catch (IOException e) {
            printError(e, this);
        }
    }

    public void printMessage(String titulo, String message, FacesMessage.Severity tipo) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(tipo, titulo, message));
        //RequestContext.getCurrentInstance().execute("writeMessage('" + titulo + ": " + message + "')");
        System.out.println(message);
    }

    public DefaultMenuItem createDefaultMenuItem(String value, boolean disabled, String onComplete, String actionListener, String title, String update, String icon) {
        DefaultMenuItem item = new DefaultMenuItem();
        item.setValue(value);
        item.setDisabled(disabled);
        item.setIcon(icon);
        item.setTitle(title);//tooltip
        item.setUpdate(update);
        if (onComplete != null) {
            item.setOncomplete(onComplete);
        }
        if (actionListener != null) {
            item.setCommand(actionListener);
        }
        return item;
    }

    public DinamicTable convertInstancesToDinamicTable(Instances in) {
        DinamicTable dinamicTable;
        int numInstances = in.numInstances();
        int numAttributes = in.numAttributes();
        DecimalFormat df = new DecimalFormat("###.##");
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<ArrayList<String>> listOfRecords = new ArrayList<>();
        ArrayList<String> rowFileData;
        AttributeStats attributeStats;// = in.attributeStats(idAttribute);

        for (int i = 0; i < numInstances; i++) {
            rowFileData = new ArrayList<>();
            for (int j = 0; j < numAttributes; j++) {
                attributeStats = in.attributeStats(j);
                if (!in.get(i).isMissing(j)) {//NO ES NULL
                    if (attributeStats.numericStats != null) {//ES NUMERICO                       
                        rowFileData.add(df.format(in.get(i).value(j)));
                    } else {//ES NOMINAL FECHA O STRING
                        rowFileData.add(in.get(i).stringValue(j));
                    }
                } else {
                    rowFileData.add("");
                }
            }
            listOfRecords.add(rowFileData);
            if (i > 50) {
                break;
            }
        }
        for (int j = 0; j < numAttributes; j++) {//TITULOS
            titles.add(in.attribute(j).name());
        }
        dinamicTable = new DinamicTable(listOfRecords, titles);
        return dinamicTable;
    }

    public boolean isEmpty(String valor) {
        //validas si una cadena es vacia //true= vacio //false=no vacio
        if (valor == null) {
            return true;
        }
        return valor.trim().length() == 0;
    }

    public MenuModel changeDisabledOption(String value, boolean disabled, DefaultSubMenu subMenu) {//cambia estado de uno de los items de un menu
        MenuModel menuModel;
        for (MenuElement item : subMenu.getElements()) {
            DefaultMenuItem menuItem = (DefaultMenuItem) item;
            if (menuItem.getValue().toString().compareTo(value) == 0) {
                menuItem.setDisabled(disabled);
                break;
            }
        }
        menuModel = new DefaultMenuModel();
        menuModel.addElement(subMenu);
        return menuModel;

    }

    public void createFile(String urlFile, String txtFile) {
        FileWriter fw;
        try {
            fw = new FileWriter(urlFile);
            BufferedWriter bw = new BufferedWriter(fw);

            try (PrintWriter salArch = new PrintWriter(bw)) {
                salArch.println(txtFile);//escribir cabecera                        
                salArch.close();
            } catch (Exception ex) {
                System.out.println("Error " + this.getClass().getName() + " - " + ex.toString());
            }
        } catch (IOException ex) {
            System.out.println("Error " + this.getClass().getName() + " - " + ex.toString());
        }
    }

    public void printError(Exception ex, Object clase) {
        System.out.println("Error " + clase.getClass().getName() + " - " + ex.toString());
    }

    public void removeFile(String urlFile) {//ELIMINACION DE UN ARCHIVO DEL SERVDOR EN BASE A SU RUTA
        File ficherofile = new File(urlFile);
        if (ficherofile.exists()) {//Lo Borramos
            ficherofile.delete();//Lo Borramos
        }
    }

    public void createImageAsPdf(String url, JFreeChart chart, int width, int height) {//CREAR UN PDF QUE CONTIENE UNA IMAGEN JFREECHART
        try {
            Rectangle pagesize = new Rectangle(width, height);
            Document document = new Document(pagesize, 50, 50, 50, 50);
            String path = url;
            //System.out.println("Phat is: " + path);
            FileOutputStream os = new FileOutputStream(path);
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
            Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
            chart.draw(g2, r2D);
            g2.dispose();
            cb.addTemplate(tp, 0, 0);
            document.close();
        } catch (FileNotFoundException | DocumentException e) {
            System.out.println(e.toString());
        }
    }

}
