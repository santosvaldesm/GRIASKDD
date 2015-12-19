/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters.selection;

import dataMining.association.Result;
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
import util.AttributeEvaluatorEnum;
import util.DinamicTable;
import util.Node;
import util.UtilFunctions;
import util.filters.ValueRow;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.OneRAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.SymmetricalUncertAttributeEval;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.attributeSelection.AttributeSelection;

/**
 *
 * @author santos
 */
public class SelectAttributes extends UtilFunctions {

    private Node currentNode = null;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB = null;//acceso a la clase principal
    private Instances data = null;
    private Instances initialData = null;
    private DinamicTable dinamicTable = new DinamicTable();
    private List<ValueRow> listEvaluatedAttributes = new ArrayList<>();
    private List<Attribute> listAttributes = new ArrayList<>();
    private String selectedAttribute = "";
    private String txtOutput = "";
    private String fileName = "download";
    private StreamedContent fileDownloadTxt;
    private List<Result> resultList = new ArrayList<>();//listado de resultados de procesos
    private String selectedResult;
    private int numberResults = 0;//numero de resultados generados
    //ATTRIBUTE SELECTION MODE--------------------------------------------------
    private String attributeSelectionMode = "Use full trainin set";//(Use full trainin set, Cross validation)  
    private boolean disabledCrossValidationRadio = true;//cuando se deshabilita el radio buton
    private boolean disabledCrossValidationFoldsSeed = true;//cuando se deshabilitan los controles de folds y seed
    private int crossValidationFolds = 10;
    private int crossValidationSeed = 1;

    //ATTRIBUTE EVALUATOR-------------------------------------------------------
    private List<SelectItem> listAttributeEvaluators = new ArrayList<>();
    private String selectedAttributeEvaluator = "";
    //InfoGainAttributeEval-------
    private boolean InfoGainBinarizeNumericAttributes = false;
    private boolean InfoGainMissingMerge = true;
    //GainRatioAttributeEval-------
    private boolean gainRatioMissingMerge = true;
    //CorrelationAttributeEval-------
    private boolean correlationOutputDetailedInfo = false;
    //oneRAttributeEval-------
    private boolean oneREvalUsingTrainingData = false;
    private int oneRFolds = 10;
    private int oneRMinimunBucketSize = 6;
    private int oneRSeed = 1;
    //symetricalUncertAttributeEval-------
    private boolean symetricalUncertMissingMerge = true;

    //SEARCH METHOD-------------------------------------------------------------
    //private List<SelectItem> listSearchMethod = new ArrayList<>();
    //private String selectedSearchMethod = "Ranker";
    //ranker----------------------
    private boolean rankingGenerateRanking = true;
    private int rankingNumToSelect = -1;
    private String rankingStartSet = "";
    private double rankingThreshold = -1.7976931348623157E308;

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

    public void changeAttributeSelectionMode() {
        disabledCrossValidationFoldsSeed = attributeSelectionMode.compareTo("Cross validation") != 0;
    }

    public SelectAttributes(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public void changeAttribute() {
        listAttributeEvaluators = new ArrayList<>();
        selectedAttributeEvaluator = "";
        AttributeStats attributeStats;
        attributeStats = initialData.attributeStats(Integer.parseInt(selectedAttribute));
        if (attributeStats.nominalCounts != null) {//es un atributo nominal
            listAttributeEvaluators.add(new SelectItem("CorrelationAtributeEval", "CorrelationAtributeEval"));
            listAttributeEvaluators.add(new SelectItem("GainRatioAttributeEval", "GainRatioAttributeEval"));
            listAttributeEvaluators.add(new SelectItem("InfoGainAttributeEval", "InfoGainAttributeEval"));
            listAttributeEvaluators.add(new SelectItem("OneRAttributeEval", "OneRAttributeEval"));
            listAttributeEvaluators.add(new SelectItem("SymetricalUncertAttributeEval", "SymetricalUncertAttributeEval"));
            selectedAttributeEvaluator = "CorrelationAtributeEval";
        }
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataSelectAttributes.showConfigure}", "Configure node", "", "fa fa-cogs"));
        //submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewSelectAttributes').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsSelectAttributes", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgSelectAttributesHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
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
            selectedAttribute = "";
            for (int i = 0; i < data.numAttributes(); i++) {
                listAttributes.add(data.attribute(i));
                selectedAttribute = String.valueOf(data.attribute(0).index());
            }
            changeAttribute();
        }
        RequestContext.getCurrentInstance().update("IdFormDialogsSelectAttributes");
        RequestContext.getCurrentInstance().execute("PF('wvDlgSelectAttributes').show()");
    }

    public void clickConfigureAttributeEvaluator() {
        switch (AttributeEvaluatorEnum.convert(selectedAttributeEvaluator)) {
            case CorrelationAtributeEval:
                RequestContext.getCurrentInstance().execute("PF('wvDlgCorrelationAttributeEval').show()");
                break;
            case GainRatioAttributeEval:
                RequestContext.getCurrentInstance().execute("PF('wvDlgGainRatioAttributeEval').show()");
                break;
            case InfoGainAttributeEval:
                RequestContext.getCurrentInstance().execute("PF('wvDlgInfoGainAttributeEval').show()");
                break;
            case OneRAttributeEval:
                RequestContext.getCurrentInstance().execute("PF('wvDlgOneRAttributeEval').show()");
                break;
            case SymetricalUncertAttributeEval:
                RequestContext.getCurrentInstance().execute("PF('wvDlgSymetricalUncertAttributeEval').show()");
                break;
            case NOVALUE:
                break;
        }
    }

    public void clickConfigureSearchMethod() {
        RequestContext.getCurrentInstance().execute("PF('wvDlgRanker').show()");
    }

    public void runProcess() {
        AttributeSelection filter = new AttributeSelection();
        initialData.setClassIndex(Integer.parseInt(selectedAttribute));
        Ranker search = new Ranker();
        search.setGenerateRanking(true);
        filter.setFolds(crossValidationFolds);
        filter.setSeed(crossValidationSeed);
        filter.setSearch(search);
        txtOutput = "";
        filter.setXval(attributeSelectionMode.compareTo("Cross validation") == 0);//realizar croos validation or full training set
        try {
            switch (AttributeEvaluatorEnum.convert(selectedAttributeEvaluator)) {
                case CorrelationAtributeEval:
                    CorrelationAttributeEval cae = new CorrelationAttributeEval();
                    cae.setOutputDetailedInfo(correlationOutputDetailedInfo);
                    filter.setEvaluator(cae);
                    break;
                case GainRatioAttributeEval:
                    GainRatioAttributeEval grae = new GainRatioAttributeEval();
                    grae.setMissingMerge(gainRatioMissingMerge);
                    filter.setEvaluator(grae);
                    break;
                case InfoGainAttributeEval:
                    InfoGainAttributeEval igae = new InfoGainAttributeEval();
                    igae.setBinarizeNumericAttributes(InfoGainBinarizeNumericAttributes);
                    igae.setMissingMerge(InfoGainMissingMerge);
                    filter.setEvaluator(igae);
                    break;
                case OneRAttributeEval:
                    OneRAttributeEval orae = new OneRAttributeEval();
                    orae.setEvalUsingTrainingData(oneREvalUsingTrainingData);
                    orae.setFolds(oneRFolds);
                    orae.setMinimumBucketSize(oneRMinimunBucketSize);
                    orae.setSeed(oneRSeed);
                    filter.setEvaluator(orae);
                    break;
                case SymetricalUncertAttributeEval:
                    SymmetricalUncertAttributeEval suae = new SymmetricalUncertAttributeEval();
                    suae.setMissingMerge(symetricalUncertMissingMerge);
                    filter.setEvaluator(suae);
                    break;
                case NOVALUE:
                    break;
            }
            filter.SelectAttributes(initialData);//ejecutar proceso de seleccion de attributos
            txtOutput = filter.toResultsString();//             

            List<ValueRow> listEvaluatedAttributesAux = new ArrayList<>();
            listEvaluatedAttributes = new ArrayList<>();
            for (int i = 0; i < initialData.numAttributes(); i++) {
                listEvaluatedAttributesAux.add(new ValueRow(initialData.attribute(i).index(), initialData.attribute(i).name(), 0, 0.0, ""));
            }
            int i = 0;
            for (double[] rankedAttribute : search.rankedAttributes()) {
                for (ValueRow vr : listEvaluatedAttributesAux) {
                    if (vr.getIdValue() == Integer.parseInt(String.valueOf(rankedAttribute[0]).replace(".0", ""))) {
                        listEvaluatedAttributes.add(new ValueRow(i++, vr.getLabel(), 0, rankedAttribute[1], ""));
                        break;
                    }
                }
            }
        } catch (Exception ex) {//System.out.println(ex.toString());
            txtOutput = txtOutput + " \n" + ex.toString();
        }
        if (data != null) {//LLENAR LOS DATOS DE INSTANCES A LA TABLA DINAMICA
            dinamicTable = convertInstancesToDinamicTable(data);
        }
        resultList.add(new Result(
                String.valueOf(++numberResults),//IDENTIFICADOR
                (new SimpleDateFormat("HH:mm:ss - ")).format(new Date()) + " " + selectedAttributeEvaluator,//NOMBRE
                txtOutput, ""));//TEXTO Y ARBOL
        selectedResult = String.valueOf(numberResults);
        currentNode.setStateNode("_v");
        RequestContext.getCurrentInstance().update("IdFormDialogsSelectAttributes:IdDlgSelectAttributes:IdPanelConfigurationSelectAttributes");
        RequestContext.getCurrentInstance().update("IdFormDialogsSelectAttributes:IdDlgSelectAttributes:IdAttributesSelectionTable");
    }

    public StreamedContent getFileDownloadTxt() {
        try {
            try (FileWriter fichero = new FileWriter("download.txt")) {//CREACION DE ARCHIVO ----------------------------
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

    public List<Attribute> getListAttributes() {
        return listAttributes;
    }

    public void setListAttributes(List<Attribute> listAttributes) {
        this.listAttributes = listAttributes;
    }

    public String getSelectedAttribute() {
        return selectedAttribute;
    }

    public void setSelectedAttribute(String selectedAttribute) {
        this.selectedAttribute = selectedAttribute;
    }

    public String getAttributeSelectionMode() {
        return attributeSelectionMode;
    }

    public void setAttributeSelectionMode(String attributeSelectionMode) {
        this.attributeSelectionMode = attributeSelectionMode;
    }

    public boolean isDisabledCrossValidationRadio() {
        return disabledCrossValidationRadio;
    }

    public void setDisabledCrossValidationRadio(boolean disabledCrossValidationRadio) {
        this.disabledCrossValidationRadio = disabledCrossValidationRadio;
    }

    public boolean isDisabledCrossValidationFoldsSeed() {
        return disabledCrossValidationFoldsSeed;
    }

    public void setDisabledCrossValidationFoldsSeed(boolean disabledCrossValidationFoldsSeed) {
        this.disabledCrossValidationFoldsSeed = disabledCrossValidationFoldsSeed;
    }

    public int getCrossValidationFolds() {
        return crossValidationFolds;
    }

    public void setCrossValidationFolds(int crossValidationFolds) {
        this.crossValidationFolds = crossValidationFolds;
    }

    public int getCrossValidationSeed() {
        return crossValidationSeed;
    }

    public void setCrossValidationSeed(int crossValidationSeed) {
        this.crossValidationSeed = crossValidationSeed;
    }

    public List<SelectItem> getListAttributeEvaluators() {
        return listAttributeEvaluators;
    }

    public void setListAttributeEvaluators(List<SelectItem> listAttributeEvaluators) {
        this.listAttributeEvaluators = listAttributeEvaluators;
    }

    public String getSelectedAttributeEvaluator() {
        return selectedAttributeEvaluator;
    }

    public void setSelectedAttributeEvaluator(String selectedAttributeEvaluator) {
        this.selectedAttributeEvaluator = selectedAttributeEvaluator;
    }

//    public String getOptionsEvaluator() {
//        return optionsEvaluator;
//    }
//
//    public void setOptionsEvaluator(String optionsEvaluator) {
//        this.optionsEvaluator = optionsEvaluator;
//    }
    public boolean isInfoGainBinarizeNumericAttributes() {
        return InfoGainBinarizeNumericAttributes;
    }

    public void setInfoGainBinarizeNumericAttributes(boolean InfoGainBinarizeNumericAttributes) {
        this.InfoGainBinarizeNumericAttributes = InfoGainBinarizeNumericAttributes;
    }

    public boolean isInfoGainMissingMerge() {
        return InfoGainMissingMerge;
    }

    public void setInfoGainMissingMerge(boolean InfoGainMissingMerge) {
        this.InfoGainMissingMerge = InfoGainMissingMerge;
    }

//    public List<SelectItem> getListSearchMethod() {
//        return listSearchMethod;
//    }
//
//    public void setListSearchMethod(List<SelectItem> listSearchMethod) {
//        this.listSearchMethod = listSearchMethod;
//    }
//
//    public String getSelectedSearchMethod() {
//        return selectedSearchMethod;
//    }
//
//    public void setSelectedSearchMethod(String selectedSearchMethod) {
//        this.selectedSearchMethod = selectedSearchMethod;
//    }
    public boolean isRankingGenerateRanking() {
        return rankingGenerateRanking;
    }

    public void setRankingGenerateRanking(boolean rankingGenerateRanking) {
        this.rankingGenerateRanking = rankingGenerateRanking;
    }

    public int getRankingNumToSelect() {
        return rankingNumToSelect;
    }

    public void setRankingNumToSelect(int rankingNumToSelect) {
        this.rankingNumToSelect = rankingNumToSelect;
    }

    public String getRankingStartSet() {
        return rankingStartSet;
    }

    public void setRankingStartSet(String rankingStartSet) {
        this.rankingStartSet = rankingStartSet;
    }

    public double getRankingThreshold() {
        return rankingThreshold;
    }

    public void setRankingThreshold(double rankingThreshold) {
        this.rankingThreshold = rankingThreshold;
    }

    public List<ValueRow> getListEvaluatedAttributes() {
        return listEvaluatedAttributes;
    }

    public void setListEvaluatedAttributes(List<ValueRow> listEvaluatedAttributes) {
        this.listEvaluatedAttributes = listEvaluatedAttributes;
    }

    public String getTxtOutput() {
        return txtOutput;
    }

    public void setTxtOutput(String txtOutput) {
        this.txtOutput = txtOutput;
    }

    public boolean isGainRatioMissingMerge() {
        return gainRatioMissingMerge;
    }

    public void setGainRatioMissingMerge(boolean gainRatioMissingMerge) {
        this.gainRatioMissingMerge = gainRatioMissingMerge;
    }

    public boolean isCorrelationOutputDetailedInfo() {
        return correlationOutputDetailedInfo;
    }

    public void setCorrelationOutputDetailedInfo(boolean correlationOutputDetailedInfo) {
        this.correlationOutputDetailedInfo = correlationOutputDetailedInfo;
    }

    public boolean isOneREvalUsingTrainingData() {
        return oneREvalUsingTrainingData;
    }

    public void setOneREvalUsingTrainingData(boolean oneREvalUsingTrainingData) {
        this.oneREvalUsingTrainingData = oneREvalUsingTrainingData;
    }

    public int getOneRFolds() {
        return oneRFolds;
    }

    public void setOneRFolds(int oneRFolds) {
        this.oneRFolds = oneRFolds;
    }

    public int getOneRMinimunBucketSize() {
        return oneRMinimunBucketSize;
    }

    public void setOneRMinimunBucketSize(int oneRMinimunBucketSize) {
        this.oneRMinimunBucketSize = oneRMinimunBucketSize;
    }

    public int getOneRSeed() {
        return oneRSeed;
    }

    public void setOneRSeed(int oneRSeed) {
        this.oneRSeed = oneRSeed;
    }

    public boolean isSymetricalUncertMissingMerge() {
        return symetricalUncertMissingMerge;
    }

    public void setSymetricalUncertMissingMerge(boolean symetricalUncertMissingMerge) {
        this.symetricalUncertMissingMerge = symetricalUncertMissingMerge;
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
