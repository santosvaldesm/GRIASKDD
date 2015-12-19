/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataSaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.faces.application.FacesMessage;
import managedBeans.GraphicControlMB;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.Node;
import util.UtilFunctions;
import weka.core.Instances;
import weka.core.converters.CSVSaver;

/**
 *
 * @author santos
 */
public class CsvSaver extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;
    //save csv    
    private StreamedContent fileDownloadCsv;
    private boolean checkCapabilitiesCsv = false;
    private boolean relativePathCsv = false;
    private int decimalPlacesCsv = 6;
    private String fieldSeparator = ",";
    private String missingValue = "?";
    private boolean noHeaderRowPresent = false;
    private String fileName = "download";

    public CsvSaver(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataCsvSaver.runProcess}", "Run process", ":IdFormDialogsCsvSaver", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgArfSaverHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void changeForm() {//Cambio en el formulario(esto para que envie los datos al nodo correspondiente)
    }

    public void runProcess() {
        if (currentNode.getParents().isEmpty()) {//se verifica que el nodo tenga fuente de datos
            printMessage("Error", "This node do not have data source", FacesMessage.SEVERITY_ERROR);
            return;
        }
        Node nodeParent = graphicControlMB.findNodeById(Integer.parseInt(currentNode.getParents().get(0)));
        if (nodeParent.getStateNode().compareTo("_v") != 0) {//se verifica que el nodo este configurado   
            printMessage("Error", "You must configure and run the parent node", FacesMessage.SEVERITY_ERROR);
            return;
        }
        data = nodeParent.getData();
        currentNode.setStateNode("_v");
        currentNode.repaintGraphic();
        RequestContext.getCurrentInstance().execute("PF('wvDlgCsvProperties').show()");
    }
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //-------------------- FUNCIONES GET AND SET -------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------    

    public StreamedContent getFileDownloadCsv() {
        try {
            CSVSaver saver = new CSVSaver();//CREACION ARF--------------------
            saver.setFieldSeparator(fieldSeparator);
            saver.setInstances(data);
            saver.setFile(new File("download.csv"));
            saver.writeBatch();
            InputStream input;//DESCARGA DE ARCHIVO ----------------------------
            File file = new File("download.csv");
            input = new FileInputStream(file);
            if (fileName.trim().length() == 0) {
                fileName = "download.arf";
            } else {
                fileName = fileName.replace(".csv", "");
                fileName = fileName.replace(".", "");
            }
            fileDownloadCsv = new DefaultStreamedContent(input, "application/binary", fileName + ".csv");
            return fileDownloadCsv;
        } catch (IOException ex) {
            System.out.println("ERROR 001: " + ex.toString());
        }
        return null;
    }

    public void setFileDownloadCsv(StreamedContent fileDownloadCsv) {
        this.fileDownloadCsv = fileDownloadCsv;
    }

    public boolean isCheckCapabilitiesCsv() {
        return checkCapabilitiesCsv;
    }

    public void setCheckCapabilitiesCsv(boolean checkCapabilitiesCsv) {
        this.checkCapabilitiesCsv = checkCapabilitiesCsv;
    }

    public int getDecimalPlacesCsv() {
        return decimalPlacesCsv;
    }

    public void setDecimalPlacesCsv(int decimalPlacesCsv) {
        this.decimalPlacesCsv = decimalPlacesCsv;
    }

    public boolean isRelativePathCsv() {
        return relativePathCsv;
    }

    public void setRelativePathCsv(boolean relativePathCsv) {
        this.relativePathCsv = relativePathCsv;
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public String getMissingValue() {
        return missingValue;
    }

    public void setMissingValue(String missingValue) {
        this.missingValue = missingValue;
    }

    public boolean isNoHeaderRowPresent() {
        return noHeaderRowPresent;
    }

    public void setNoHeaderRowPresent(boolean noHeaderRowPresent) {
        this.noHeaderRowPresent = noHeaderRowPresent;
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    public Instances getData() {
        return data;
    }

    public void setData(Instances data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
