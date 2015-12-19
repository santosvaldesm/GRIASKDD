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
import weka.core.converters.ArffSaver;

/**
 *
 * @author santos
 */
public class ArfSaver extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;
    //save arf
    private String fileName = "download";
    private StreamedContent fileDownloadArf;
    private boolean checkCapabilitiesArf = false;
    private boolean relativePathArf = false;
    private int decimalPlacesArf = 6;
    private boolean compressOutput = false;

    public ArfSaver(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataArfSaver.runProcess}", "Run process", ":IdFormDialogsArfSaver", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));        
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));        
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgArfSaverHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void changeForm() {//hay un cambi en el formulario(esto para que envie los datos al nodo correspondiente)
        //    System.out.println("Cambio");
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
        RequestContext.getCurrentInstance().execute("PF('wvDlgArfProperties').show()");
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //-------------------- FUNCIONES GET AND SET -------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    public StreamedContent getFileDownloadArf() {
        try {
            ArffSaver saver = new ArffSaver();//CREACION ARF--------------------
            saver.setInstances(data);
            saver.setFile(new File("download.arff"));
            saver.writeBatch();
            InputStream input;//DESCARGA DE ARCHIVO ----------------------------
            File file = new File("download.arff");
            input = new FileInputStream(file);
            if (fileName.trim().length() == 0) {
                fileName = "download.arff";
            } else {
                fileName = fileName.replace(".arff", "");
                fileName = fileName.replace(".", "");
            }

            fileDownloadArf = new DefaultStreamedContent(input, "application/binary", fileName + ".arff");
            return fileDownloadArf;
        } catch (IOException ex) {
            System.out.println("ERROR 001: " + ex.toString());
        }
        return null;
    }

    public void setFileDownloadArf(StreamedContent fileDownloadArf) {
        this.fileDownloadArf = fileDownloadArf;
    }

    public boolean isCompressOutput() {
        return compressOutput;
    }

    public void setCompressOutput(boolean compressOutput) {
        this.compressOutput = compressOutput;
    }

    public boolean isRelativePathArf() {
        return relativePathArf;
    }

    public void setRelativePathArf(boolean relativePathArf) {
        this.relativePathArf = relativePathArf;
    }

    public int getDecimalPlacesArf() {
        return decimalPlacesArf;
    }

    public void setDecimalPlacesArf(int decimalPlacesArf) {
        this.decimalPlacesArf = decimalPlacesArf;
    }

    public boolean isCheckCapabilitiesArf() {
        return checkCapabilitiesArf;
    }

    public void setCheckCapabilitiesArf(boolean checkCapabilitiesArf) {
        this.checkCapabilitiesArf = checkCapabilitiesArf;
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
