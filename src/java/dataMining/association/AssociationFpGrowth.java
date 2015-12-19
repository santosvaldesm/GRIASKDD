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
import static weka.associations.Apriori.TAGS_SELECTION;
import weka.associations.FPGrowth;
import weka.core.Instances;
import weka.core.SelectedTag;

/**
 *
 * @author santos
 */
public class AssociationFpGrowth extends UtilFunctions {

    private Node currentNode = null;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB = null;//acceso a la clase principal
    private Instances data = null;
    private Instances initialData = null;
    private DinamicTable dinamicTable = new DinamicTable();

    private List<SelectItem> listAttributes = new ArrayList<>();
    private List<Result> resultList = new ArrayList<>();//listado de resultados de procesos
    private String selectedResult;
    private int numberResults = 0;//numero de resultados generados
    
    private String txtOutput = "";
    private String fileName = "download";
    private StreamedContent fileDownloadTxt;

    private int positiveIndex = 2;
    private double delta = 0.05;
    private boolean findAllRulesForSupportlevel = false;
    private double lowerBoundMinSupport = 0.1;
    private int maxNumberOfItems = 10;
    private String metricType = "";
    private double minMetric = 0.9;
    private int numRulesToFind = 10;
    private String rulesMustContain = "";
    private String transactionsMustContain = "";
    private double upperBoundMinSupport = 1.0;
    private boolean useOrFormatMustContainList = false;

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

    public AssociationFpGrowth(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataAssociationFpGrowth.showConfigure}", "Connect node", "", "fa fa-cogs"));
        //submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewAssociationFpGrowth').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsAssociationFpGrowth", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgAssociationFpGrowthHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
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
        if (currentNode.getStateNode().compareTo("_r") == 0) {//            
            data = new Instances(nodeParent.getData());
            initialData = new Instances(nodeParent.getData());
            listAttributes = new ArrayList<>();
            positiveIndex = 2;
            for (int i = 0; i < data.numAttributes(); i++) {
                listAttributes.add(new SelectItem(data.attribute(i).index(), data.attribute(i).name()));
            }
        }
        RequestContext.getCurrentInstance().update("IdFormDialogsAssociationFpGrowth");
        RequestContext.getCurrentInstance().execute("PF('wvDlgAssociationFpGrowth').show()");
    }

    public void runProcess() {

        try {
            //SelectedTag d=new SelectedTag
            FPGrowth fpGrowthoObj = new FPGrowth();
            fpGrowthoObj.setDelta(delta);
            fpGrowthoObj.setFindAllRulesForSupportLevel(findAllRulesForSupportlevel);
            fpGrowthoObj.setLowerBoundMinSupport(lowerBoundMinSupport);
            fpGrowthoObj.setMaxNumberOfItems(maxNumberOfItems);
            fpGrowthoObj.setMetricType((new SelectedTag(Integer.parseInt(metricType), TAGS_SELECTION)));
            fpGrowthoObj.setMinMetric(minMetric);
            fpGrowthoObj.setNumRulesToFind(numRulesToFind);
            fpGrowthoObj.setPositiveIndex(positiveIndex);
            fpGrowthoObj.setRulesMustContain(rulesMustContain);
            fpGrowthoObj.setTransactionsMustContain(transactionsMustContain);
            fpGrowthoObj.setUpperBoundMinSupport(upperBoundMinSupport);
            fpGrowthoObj.setUseORForMustContainList(useOrFormatMustContainList);
            fpGrowthoObj.buildAssociations(initialData);
            txtOutput = fpGrowthoObj.toString();
        } catch (Exception ex) {
            txtOutput = ex.toString();
        }
        resultList.add(new Result(
                String.valueOf(++numberResults),//IDENTIFICADOR
                (new SimpleDateFormat("HH:mm:ss - ")).format(new Date()) + " FpGrowth",//NOMBRE
                txtOutput, ""));//TEXTO Y ARBOL
        selectedResult = String.valueOf(numberResults);
        currentNode.setStateNode("_v");
        RequestContext.getCurrentInstance().update("IdFormDialogsAssociationFpGrowth:IdDlgAssociationFpGrowth:IdPanelConfigurationAssociationFpGrowth");
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

    public String getTxtOutput() {
        return txtOutput;
    }

    public void setTxtOutput(String txtOutput) {
        this.txtOutput = txtOutput;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public double getLowerBoundMinSupport() {
        return lowerBoundMinSupport;
    }

    public void setLowerBoundMinSupport(double lowerBoundMinSupport) {
        this.lowerBoundMinSupport = lowerBoundMinSupport;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public double getMinMetric() {
        return minMetric;
    }

    public void setMinMetric(double minMetric) {
        this.minMetric = minMetric;
    }

    public double getUpperBoundMinSupport() {
        return upperBoundMinSupport;
    }

    public void setUpperBoundMinSupport(double upperBoundMinSupport) {
        this.upperBoundMinSupport = upperBoundMinSupport;
    }

    public boolean isFindAllRulesForSupportlevel() {
        return findAllRulesForSupportlevel;
    }

    public void setFindAllRulesForSupportlevel(boolean findAllRulesForSupportlevel) {
        this.findAllRulesForSupportlevel = findAllRulesForSupportlevel;
    }

    public int getMaxNumberOfItems() {
        return maxNumberOfItems;
    }

    public void setMaxNumberOfItems(int maxNumberOfItems) {
        this.maxNumberOfItems = maxNumberOfItems;
    }

    public int getNumRulesToFind() {
        return numRulesToFind;
    }

    public void setNumRulesToFind(int numRulesToFind) {
        this.numRulesToFind = numRulesToFind;
    }

    public String getRulesMustContain() {
        return rulesMustContain;
    }

    public void setRulesMustContain(String rulesMustContain) {
        this.rulesMustContain = rulesMustContain;
    }

    public String getTransactionsMustContain() {
        return transactionsMustContain;
    }

    public void setTransactionsMustContain(String transactionsMustContain) {
        this.transactionsMustContain = transactionsMustContain;
    }

    public boolean isUseOrFormatMustContainList() {
        return useOrFormatMustContainList;
    }

    public void setUseOrFormatMustContainList(boolean useOrFormatMustContainList) {
        this.useOrFormatMustContainList = useOrFormatMustContainList;
    }

    public int getPositiveIndex() {
        return positiveIndex;
    }

    public void setPositiveIndex(int positiveIndex) {
        this.positiveIndex = positiveIndex;
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
