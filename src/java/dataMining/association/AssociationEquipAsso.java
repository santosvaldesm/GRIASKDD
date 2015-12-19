/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataMining.association;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import managedBeans.GraphicControlMB;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.DinamicTable;
import util.Node;
import util.UtilFunctions;
import util.filters.TreeEquipAsso;
import weka.core.AttributeStats;
import weka.core.Instances;

/**
 *
 * @author santos
 */
public class AssociationEquipAsso extends UtilFunctions {

    private Node currentNode = null;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB = null;//acceso a la clase principal
    private int maxNumLevel = 2;
    private int maxNumRules = 10;
    private Instances data = null;
    private Instances initialData = null;
    private DinamicTable dinamicTable = new DinamicTable();
    private boolean outputItemSets = false;

    private List<SelectItem> listAttributes = new ArrayList<>();
    private List<Result> resultList = new ArrayList<>();//listado de resultados de procesos
    private String selectedResult;
    
    
    private String txtOutput = "";
    private String fileName = "download";
    private StreamedContent fileDownloadTxt;
    private int numberResults = 0;//numero de resultados generados
    private String classIndex = "-1";
    private double confidence = 0.2;
    private double support = 0.2;

    public void changeForm() {//hay un cambio en el formulario(esto para que envie los datos al nodo correspondiente)
        //    System.out.println("Cambio");
    }

    public void stopProcess() {
    }

    public void removeProcess() {
        if (selectedResult != null && selectedResult.length() != 0) {
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).getId().compareTo(selectedResult) == 0) {//SE PROCEDE A ELIMINAR EL RESULTADO
                    if (resultList.size() == 1) {//SOLO HAY UNO(LA LISTA DE RESULTADOS QUEDARA VACIA)
                        selectedResult = "";
                    } else if (i == 0) {//ES EL PRIMER RESULTADO, SE ASIGNA A selectResult EL SIGUIENTE RESULTADO
                        selectedResult = resultList.get(i + 1).getId();
                    } else {//SE ASIGNARA A selectResult EL ANTERIOR RESULTADO
                        selectedResult = resultList.get(i - 1).getId();
                    }
                    resultList.remove(i);
                    changeResultList();
                    break;
                }
            }
        }
    }

    public void changeResultList() {//CAMBIA LISTA DE RESULTADOS        
        txtOutput = "";
        for (Result r : resultList) {
            if (r.getId().compareTo(selectedResult) == 0) {
                txtOutput = r.getTxtResult();
                break;
            }
        }
    }

    public AssociationEquipAsso(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataAssociationEquipAsso.showConfigure}", "Connect node", "", "fa fa-cogs"));
        //submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewAssociationEquipAsso').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsAssociationEquipAsso", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgAssociationEquipAssoHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void showConfigure() {
        if (currentNode.getParents().isEmpty()) {//se verifica que el nodo tenga fuente de datos
            printMessage("Error", "This node do not have data source", FacesMessage.SEVERITY_ERROR);
            return;
        }
        Node nodeParent = graphicControlMB.findNodeById(Integer.parseInt(currentNode.getParents().get(0)));
        if (nodeParent.getStateNode().compareTo("_v") != 0) {//se verifica que el nodo este configurado   
            printMessage("Error", "You must configure and run the parent node", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (currentNode.getStateNode().compareTo("_r") == 0) {
            data = new Instances(nodeParent.getData());
            initialData = new Instances(nodeParent.getData());
            listAttributes = new ArrayList<>();
            classIndex = "-1";
            for (int i = 0; i < data.numAttributes(); i++) {
                listAttributes.add(new SelectItem(data.attribute(i).index(), data.attribute(i).name()));
            }
        }
        RequestContext.getCurrentInstance().update("IdFormDialogsAssociationEquipAsso");
        RequestContext.getCurrentInstance().execute("PF('wvDlgAssociationEquipAsso').show()");
    }

    public void runProcess() {
        boolean continueProcces = true;
        AttributeStats attributeStats;
        callGarbageCollector();
        for (int i = 0; i < data.numAttributes(); i++) {//ANTES DE CUALQUIER PROCESO VALIDAR QUE LOS DATOS SEAN NOMINALES(SINO PROBOCA ERROR) 
            attributeStats = data.attributeStats(i);
            if (attributeStats.numericStats != null) {
                continueProcces = false;
                txtOutput = "UnsupportedAttributeTypeException: EquipAsso: Cannot handle numeric attributes!";
                break;
            }
        }
        if (continueProcces) {
            TreeEquipAsso tree = new TreeEquipAsso(maxNumLevel, support * 100, confidence * 100, maxNumRules, Integer.parseInt(classIndex));//EN BASE A LOS ITEMSETS FRECUENTES GENERO LAS REGLAS

            tree.determineFrequentItemSets(data);
            //System.out.println(tree.printTreeInOrder());
            txtOutput = "EquipAsso\n=======\n\n";
            txtOutput = txtOutput + "Instances: " + data.numInstances() + "\n";
            txtOutput = txtOutput + "Minimum Support: " + support + ":(" + data.numInstances() * support + " instances)\n";
            txtOutput = txtOutput + "Minimum Confidence: " + confidence + "\n";
            txtOutput = txtOutput + "Maximum long in rules:" + maxNumLevel + "\n";
            txtOutput = txtOutput + "Number of rules:" + maxNumRules + "\n\n";
            if (outputItemSets) {
                txtOutput = txtOutput + tree.printLargeItemSets();
            }
            txtOutput = txtOutput + tree.generateRules();
            resultList.add(new Result(
                    String.valueOf(++numberResults),//IDENTIFICADOR
                    (new SimpleDateFormat("HH:mm:ss - ")).format(new Date()) + " EquipAsso",//NOMBRE
                    txtOutput, ""));//TEXTO Y ARBOL
            selectedResult = String.valueOf(numberResults);
            currentNode.setStateNode("_v");
            RequestContext.getCurrentInstance().update("IdFormDialogsAssociationEquipAsso:IdDlgAssociationEquipAsso:IdPanelConfigurationAssociationEquipAsso");
        }
    }

    public StreamedContent getFileDownloadTxt() {
        try {
            try (FileWriter fichero = new FileWriter("download.txt")) { //CREACION DE ARCHIVO ----------------------------
                PrintWriter pw = new PrintWriter(fichero);
                for (String line : txtOutput.split(Character.toString((char) 10))) {
                    pw.println(line);
                }
            }
            InputStream input;//DESCARGA DE ARCHIVO ----------------------------
            File file = new File("download.txt");
            input = new FileInputStream(file);
            if (fileName.trim().length() == 0) {
                fileName = "download.txt";
            } else {
                fileName = fileName.replace(".txt", "");
                fileName = fileName.replace(".", "");
            }
            fileDownloadTxt = new DefaultStreamedContent(input, "application/binary", fileName + ".txt");
            return fileDownloadTxt;
        } catch (IOException ex) {
            System.out.println("ERROR 001: " + ex.toString());
        }
        return null;
    }

    public void setFileDownloadTxt(StreamedContent fileDownloadTxt) {
        this.fileDownloadTxt = fileDownloadTxt;
    }

    public String getFileName() {
        return fileName;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //-------------------- FUNCIONES GET AND SET -------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    public DinamicTable getDinamicTable() {
        return dinamicTable;
    }

    public void setDinamicTable(DinamicTable dinamicTable) {
        this.dinamicTable = dinamicTable;
    }

    public Instances getData() {
        return data;
    }

    public void setData(Instances data) {
        this.data = data;
    }

    public List<SelectItem> getListAttributes() {
        return listAttributes;
    }

    public void setListAttributes(List<SelectItem> listAttributes) {
        this.listAttributes = listAttributes;
    }

    public String getClassIndex() {
        return classIndex;
    }

    public void setClassIndex(String classIndex) {
        this.classIndex = classIndex;
    }

    public String getTxtOutput() {
        return txtOutput;
    }

    public void setTxtOutput(String txtOutput) {
        this.txtOutput = txtOutput;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getSupport() {
        return support;
    }

    public void setSupport(double support) {
        this.support = support;
    }

    public int getMaxNumLevel() {
        return maxNumLevel;
    }

    public void setMaxNumLevel(int maxNumLevel) {
        this.maxNumLevel = maxNumLevel;
    }

    public int getMaxNumRules() {
        return maxNumRules;
    }

    public void setMaxNumRules(int maxNumRules) {
        this.maxNumRules = maxNumRules;
    }

    public boolean isOutputItemSets() {
        return outputItemSets;
    }

    public void setOutputItemSets(boolean outputItemSets) {
        this.outputItemSets = outputItemSets;
    }

    public List<Result> getResultList() {
        return resultList;
    }

    public void setResultList(List<Result> resultList) {
        this.resultList = resultList;
    }

    public String getSelectedResult() {
        return selectedResult;
    }

    public void setSelectedResult(String selectedResult) {
        this.selectedResult = selectedResult;
    }

}
