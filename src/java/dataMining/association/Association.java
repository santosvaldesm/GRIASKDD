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
import util.AssociatorEnum;
import util.Node;
import util.UtilFunctions;
import weka.associations.Apriori;
import static weka.associations.Apriori.TAGS_SELECTION;
import weka.core.Instances;
import weka.core.SelectedTag;

/**
 *
 * @author santos
 */
public class Association extends UtilFunctions {

    private Node currentNode = null;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB = null;//acceso a la clase principal
    private Instances data = null;
    private Instances initialData = null;
    private String txtOutput = "";
    private String fileName = "download";
    private StreamedContent fileDownloadTxt;
    private List<SelectItem> listAttributes = new ArrayList<>();
    private List<SelectItem> listAssociators = new ArrayList<>();
    private String selectedAssociator = "";

    //RESULT LIST---------------------------------------------------------------
    private List<Result> resultList = new ArrayList<>();//listado de resultados de procesos
    private String selectedResult;
    private int numberResults = 0;//numero de resultados generados    
    //CONFIGURE APRIORI---------------------------------------------------------
    private String classIndexA = "-1";
    private boolean carA = false;
    private double deltaA = 0.05;
    private double lowerBoundMinSupportA = 0.1;
    private String metricTypeA = "";
    private double minMetricA = 0.9;
    private int numRulesA = 10;
    private boolean outputItemSetsA = false;
    private boolean removeAllMissingColsA = false;
    private double significanceLevelA = -1.0;
    private boolean treatZeroAsMissingA = false;
    private double upperBoundMinSupportA = 1.0;
    private boolean verboseA = false;
    //CONFIGURE EQUIPASSO---------------------------------------------------------
    private String classIndexEA = "-1";
    private double confidenceEA = 0.2;
    private double supportEA = 0.2;
    private int maxNumLevelEA = 2;
    private int maxNumRulesEA = 10;
    private boolean outputItemSetsEA = false;
    //CONFIGURE FPGROWTH---------------------------------------------------------
    private int positiveIndexFPG = 2;
    private double deltaFPG = 0.05;
    private boolean findAllRulesForSupportlevelFPG = false;
    private double lowerBoundMinSupportFPG = 0.1;
    private int maxNumberOfItemsFPG = 10;
    private String metricTypeFPG = "";
    private double minMetricFPG = 0.9;
    private int numRulesToFindFPG = 10;
    private String rulesMustContainFPG = "";
    private String transactionsMustContainFPG = "";
    private double upperBoundMinSupportFPG = 1.0;
    private boolean useOrFormatMustContainListFPG = false;

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

    public Association(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        listAssociators = new ArrayList<>();
        listAssociators.add(new SelectItem("Apriori", "Apriori"));
        listAssociators.add(new SelectItem("FpGrowth", "FpGrowth"));
        listAssociators.add(new SelectItem("EquipAsso", "EquipAsso"));
        listAssociators.add(new SelectItem("MateTree", "MateTree"));
        selectedAssociator = "Apriori";

        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataAssociation.showConfigure}", "Configure", "", "fa fa-cogs"));
        //submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewAssociation').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsAssociation", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgAssociationHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void clickConfigureAssociator() {
        switch (AssociatorEnum.convert(selectedAssociator)) {
            case Apriori:
                RequestContext.getCurrentInstance().execute("PF('wvDlgApriori').show()");
                break;
            case FpGrowth:
                RequestContext.getCurrentInstance().execute("PF('wvDlgFpGrowth').show()");
                break;
            case EquipAsso:
                RequestContext.getCurrentInstance().execute("PF('wvDlgEquipAsso').show()");
                break;
            case MateTree:
                RequestContext.getCurrentInstance().execute("PF('wvDlgMateTree').show()");
                break;
            case NOVALUE:
                break;
        }
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
            classIndexA = "-1";
            for (int i = 0; i < data.numAttributes(); i++) {
                listAttributes.add(new SelectItem(data.attribute(i).index(), data.attribute(i).name()));
            }
        }
        RequestContext.getCurrentInstance().update("IdFormDialogsAssociation");
        RequestContext.getCurrentInstance().execute("PF('wvDlgAssociation').show()");
    }

    public void runProcess() {
        try {
            Apriori aprioriObj = new Apriori();
            aprioriObj.setCar(carA);
            aprioriObj.setClassIndex(Integer.parseInt(classIndexA));
            if (Integer.parseInt(classIndexA) != -1) {
                aprioriObj.setClassIndex(Integer.parseInt(classIndexA) + 1);
            }

            aprioriObj.setDelta(deltaA);
            aprioriObj.setLowerBoundMinSupport(lowerBoundMinSupportA);
            aprioriObj.setMetricType((new SelectedTag(Integer.parseInt(metricTypeA), TAGS_SELECTION)));
            aprioriObj.setMinMetric(minMetricA);
            aprioriObj.setNumRules(numRulesA);
            aprioriObj.setOutputItemSets(outputItemSetsA);
            aprioriObj.setRemoveAllMissingCols(removeAllMissingColsA);
            aprioriObj.setSignificanceLevel(significanceLevelA);
            aprioriObj.setTreatZeroAsMissing(treatZeroAsMissingA);
            aprioriObj.setUpperBoundMinSupport(upperBoundMinSupportA);
            aprioriObj.setVerbose(verboseA);
            aprioriObj.buildAssociations(initialData);
            txtOutput = aprioriObj.toString();
        } catch (Exception ex) {
            txtOutput = ex.toString();
        }

        resultList.add(new Result(
                String.valueOf(++numberResults),//IDENTIFICADOR
                (new SimpleDateFormat("HH:mm:ss - ")).format(new Date()) + " Apriori",//NOMBRE
                txtOutput, ""));//TEXTO Y ARBOL
        selectedResult = String.valueOf(numberResults);
        currentNode.setStateNode("_v");//changeDisabledOption("View", Boolean.FALSE, submenu);
        RequestContext.getCurrentInstance().update("IdFormDialogsAssociation:IdDlgAssociation:IdPanelConfigurationAssociation");
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

//    public DinamicTable getDinamicTable() {
//        return dinamicTable;
//    }
//
//    public void setDinamicTable(DinamicTable dinamicTable) {
//        this.dinamicTable = dinamicTable;
//    }
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

    public String getClassIndexA() {
        return classIndexA;
    }

    public void setClassIndexA(String classIndexA) {
        this.classIndexA = classIndexA;
    }

    public String getTxtOutput() {
        return txtOutput;
    }

    public void setTxtOutput(String txtOutput) {
        this.txtOutput = txtOutput;
    }

    public boolean isCarA() {
        return carA;
    }

    public void setCarA(boolean carA) {
        this.carA = carA;
    }

    public double getDeltaA() {
        return deltaA;
    }

    public void setDeltaA(double deltaA) {
        this.deltaA = deltaA;
    }

    public double getLowerBoundMinSupportA() {
        return lowerBoundMinSupportA;
    }

    public void setLowerBoundMinSupportA(double lowerBoundMinSupportA) {
        this.lowerBoundMinSupportA = lowerBoundMinSupportA;
    }

    public String getMetricTypeA() {
        return metricTypeA;
    }

    public void setMetricTypeA(String metricTypeA) {
        this.metricTypeA = metricTypeA;
    }

    public double getMinMetricA() {
        return minMetricA;
    }

    public void setMinMetricA(double minMetricA) {
        this.minMetricA = minMetricA;
    }

    public int getNumRulesA() {
        return numRulesA;
    }

    public void setNumRulesA(int numRulesA) {
        this.numRulesA = numRulesA;
    }

    public boolean isOutputItemSetsA() {
        return outputItemSetsA;
    }

    public void setOutputItemSetsA(boolean outputItemSetsA) {
        this.outputItemSetsA = outputItemSetsA;
    }

    public boolean isRemoveAllMissingColsA() {
        return removeAllMissingColsA;
    }

    public void setRemoveAllMissingColsA(boolean removeAllMissingColsA) {
        this.removeAllMissingColsA = removeAllMissingColsA;
    }

    public double getSignificanceLevelA() {
        return significanceLevelA;
    }

    public void setSignificanceLevelA(double significanceLevelA) {
        this.significanceLevelA = significanceLevelA;
    }

    public boolean isTreatZeroAsMissingA() {
        return treatZeroAsMissingA;
    }

    public void setTreatZeroAsMissingA(boolean treatZeroAsMissingA) {
        this.treatZeroAsMissingA = treatZeroAsMissingA;
    }

    public double getUpperBoundMinSupportA() {
        return upperBoundMinSupportA;
    }

    public void setUpperBoundMinSupportA(double upperBoundMinSupportA) {
        this.upperBoundMinSupportA = upperBoundMinSupportA;
    }

    public boolean isVerboseA() {
        return verboseA;
    }

    public void setVerboseA(boolean verboseA) {
        this.verboseA = verboseA;
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

    public String getClassIndexEA() {
        return classIndexEA;
    }

    public void setClassIndexEA(String classIndexEA) {
        this.classIndexEA = classIndexEA;
    }

    public double getConfidenceEA() {
        return confidenceEA;
    }

    public void setConfidenceEA(double confidenceEA) {
        this.confidenceEA = confidenceEA;
    }

    public double getSupportEA() {
        return supportEA;
    }

    public void setSupportEA(double supportEA) {
        this.supportEA = supportEA;
    }

    public int getMaxNumLevelEA() {
        return maxNumLevelEA;
    }

    public void setMaxNumLevelEA(int maxNumLevelEA) {
        this.maxNumLevelEA = maxNumLevelEA;
    }

    public int getMaxNumRulesEA() {
        return maxNumRulesEA;
    }

    public void setMaxNumRulesEA(int maxNumRulesEA) {
        this.maxNumRulesEA = maxNumRulesEA;
    }

    public boolean isOutputItemSetsEA() {
        return outputItemSetsEA;
    }

    public void setOutputItemSetsEA(boolean outputItemSetsEA) {
        this.outputItemSetsEA = outputItemSetsEA;
    }

    public int getPositiveIndexFPG() {
        return positiveIndexFPG;
    }

    public void setPositiveIndexFPG(int positiveIndexFPG) {
        this.positiveIndexFPG = positiveIndexFPG;
    }

    public double getDeltaFPG() {
        return deltaFPG;
    }

    public void setDeltaFPG(double deltaFPG) {
        this.deltaFPG = deltaFPG;
    }

    public boolean isFindAllRulesForSupportlevelFPG() {
        return findAllRulesForSupportlevelFPG;
    }

    public void setFindAllRulesForSupportlevelFPG(boolean findAllRulesForSupportlevelFPG) {
        this.findAllRulesForSupportlevelFPG = findAllRulesForSupportlevelFPG;
    }

    public double getLowerBoundMinSupportFPG() {
        return lowerBoundMinSupportFPG;
    }

    public void setLowerBoundMinSupportFPG(double lowerBoundMinSupportFPG) {
        this.lowerBoundMinSupportFPG = lowerBoundMinSupportFPG;
    }

    public int getMaxNumberOfItemsFPG() {
        return maxNumberOfItemsFPG;
    }

    public void setMaxNumberOfItemsFPG(int maxNumberOfItemsFPG) {
        this.maxNumberOfItemsFPG = maxNumberOfItemsFPG;
    }

    public String getMetricTypeFPG() {
        return metricTypeFPG;
    }

    public void setMetricTypeFPG(String metricTypeFPG) {
        this.metricTypeFPG = metricTypeFPG;
    }

    public double getMinMetricFPG() {
        return minMetricFPG;
    }

    public void setMinMetricFPG(double minMetricFPG) {
        this.minMetricFPG = minMetricFPG;
    }

    public int getNumRulesToFindFPG() {
        return numRulesToFindFPG;
    }

    public void setNumRulesToFindFPG(int numRulesToFindFPG) {
        this.numRulesToFindFPG = numRulesToFindFPG;
    }

    public String getRulesMustContainFPG() {
        return rulesMustContainFPG;
    }

    public void setRulesMustContainFPG(String rulesMustContainFPG) {
        this.rulesMustContainFPG = rulesMustContainFPG;
    }

    public String getTransactionsMustContainFPG() {
        return transactionsMustContainFPG;
    }

    public void setTransactionsMustContainFPG(String transactionsMustContainFPG) {
        this.transactionsMustContainFPG = transactionsMustContainFPG;
    }

    public double getUpperBoundMinSupportFPG() {
        return upperBoundMinSupportFPG;
    }

    public void setUpperBoundMinSupportFPG(double upperBoundMinSupportFPG) {
        this.upperBoundMinSupportFPG = upperBoundMinSupportFPG;
    }

    public boolean isUseOrFormatMustContainListFPG() {
        return useOrFormatMustContainListFPG;
    }

    public void setUseOrFormatMustContainListFPG(boolean useOrFormatMustContainListFPG) {
        this.useOrFormatMustContainListFPG = useOrFormatMustContainListFPG;
    }

    public List<SelectItem> getListAssociators() {
        return listAssociators;
    }

    public void setListAssociators(List<SelectItem> listAssociators) {
        this.listAssociators = listAssociators;
    }

    public String getSelectedAssociator() {
        return selectedAssociator;
    }

    public void setSelectedAssociator(String selectedAssociator) {
        this.selectedAssociator = selectedAssociator;
    }

}
