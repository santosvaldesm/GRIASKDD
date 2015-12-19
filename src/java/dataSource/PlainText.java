/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataSource;

import util.Node;
import util.UtilFunctions;
import javax.faces.application.FacesMessage;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.DinamicTable;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

/**
 *
 * @author santos
 */
public class PlainText extends UtilFunctions {

    private DinamicTable dinamicTable = new DinamicTable();
    private UploadedFile uploadedFile;
    private String fileName = "No file loaded.";

    private final Node currentNode;
    private Instances data = null;//datos de instancias actuales

    private String dateAttributes = "";
    private String dateFormat = "";
    private String enclosureCharacters = "\"";
    private String fieldSeparator = "";
    private String missingValue = "?";
    private boolean noHeaderRowPresent = false;
    private String nominalAtrributes = "";
    private String stringAttributes = "";
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");

    public void changeForm() {//hay un cambi en el formulario(esto para que envie los datos al nodo correspondiente)
        //    System.out.println("Cambio");
    }

    public void changeCheckAtrribute() {//se presiona el check box que tiene cada atributo(esto para que hay la peticion ajax)
    }

    public PlainText(Node p) {//constructor,
        currentNode = p;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Open", Boolean.FALSE, "PF('wvDlgOpenPlainText').show(); PF('wvContextMenu').hide();", null, "Config file", ":IdFormDialogsPlainText", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewPlainTextData').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsPlainText", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgPlainTextHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void handleFileUpload(FileUploadEvent event) {//upload del archivo
        try {
            currentNode.resetChildrenNodes();
            uploadedFile = event.getFile();
            fileName = uploadedFile.getFileName();
            if (fileName.endsWith("arff")) {
                ArffLoader loader = new ArffLoader();
                loader.setSource(uploadedFile.getInputstream());
                data = loader.getDataSet();//System.out.println(data.toString());
            } else {
                CSVLoader loader = new CSVLoader();
                loader.setSource(uploadedFile.getInputstream());
                if (!isEmpty(dateAttributes)) {
                    loader.setDateAttributes(dateAttributes);
                }
                if (!isEmpty(dateAttributes)) {
                    loader.setDateFormat(dateFormat);
                }
                if (!isEmpty(fieldSeparator)) {
                    loader.setFieldSeparator(fieldSeparator);
                }
                if (!isEmpty(enclosureCharacters)) {
                    loader.setEnclosureCharacters(enclosureCharacters);
                }
                if (!isEmpty(missingValue)) {
                    loader.setMissingValue(missingValue);
                }

                loader.setNoHeaderRowPresent(noHeaderRowPresent);

                if (!isEmpty(nominalAtrributes)) {
                    loader.setNominalAttributes(nominalAtrributes);
                }
                if (!isEmpty(stringAttributes)) {
                    loader.setStringAttributes(stringAttributes);
                }
                data = loader.getDataSet();//System.out.println(data.toString());
            }
            if (data != null) {//LLENAR LOS DATOS DE INSTANCES A LA TABLA DINAMICA
                dinamicTable = convertInstancesToDinamicTable(data);
            }

            currentNode.setStateNode("_v");
            changeDisabledOption("View", Boolean.FALSE, submenu);
            if (uploadedFile == null || fileName.trim().length() == 0) {
                printMessage("Error", "You must select a file", FacesMessage.SEVERITY_ERROR);
            } else {
                printMessage("Right", "File upload completed successfully", FacesMessage.SEVERITY_INFO);
                RequestContext.getCurrentInstance().execute("PF('wvDlgOpenPlainText').hide()");
            }
        } catch (Exception ex) {
            System.out.println("Error 20 in " + this.getClass().getName() + ":" + ex.toString());
            printMessage("Error", "when try upload file" + ex.toString(), FacesMessage.SEVERITY_ERROR);
        }
        currentNode.repaintGraphic();
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //-------------------- FUNCIONES GET AND SET -------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------    
    public String getDateAttributes() {
        return dateAttributes;
    }

    public void setDateAttributes(String dateAttributes) {
        this.dateAttributes = dateAttributes;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getEnclosureCharacters() {
        return enclosureCharacters;
    }

    public void setEnclosureCharacters(String enclosureCharacters) {
        this.enclosureCharacters = enclosureCharacters;
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

    public String getNominalAtrributes() {
        return nominalAtrributes;
    }

    public void setNominalAtrributes(String nominalAtrributes) {
        this.nominalAtrributes = nominalAtrributes;
    }

    public String getStringAttributes() {
        return stringAttributes;
    }

    public void setStringAttributes(String stringAttributes) {
        this.stringAttributes = stringAttributes;
    }

    public Instances getData() {
        return data;
    }

    public void setData(Instances data) {
        this.data = data;
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DinamicTable getDinamicTable() {
        return dinamicTable;
    }

    public void setDinamicTable(DinamicTable dinamicTable) {
        this.dinamicTable = dinamicTable;
    }

}
