/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import dataMining.Cluster.Cluster;
import dataMining.association.Association;
import dataMining.association.AssociationApriori;
import dataMining.association.AssociationEquipAsso;
import dataMining.association.AssociationFpGrowth;
import dataMining.classification.Classification;
import dataSaver.ArfSaver;
import dataSaver.CsvSaver;
import dataSource.PlainText;
import dataSource.ConnectionDB;
import filters.clean.KNNImputation;
import filters.clean.Metrics;
import filters.transformation.Codification;
import filters.transformation.DiscretizeAttribute;
import java.util.ArrayList;
import filters.clean.RemoveMissing;
import filters.clean.SamplingPercentage;
import filters.clean.ReplaceValue;
import filters.transformation.NumericToNominalFilter;
import filters.selection.Selection;
import filters.clean.UpdateMissing;
import filters.selection.SelectAttributes;
import filters.transformation.NominalToBinaryFilter;
import managedBeans.GraphicControlMB;
import org.primefaces.model.menu.MenuModel;
import static util.NodeTypeEnum.Metrics;
import views.DataAnalisis;
import weka.core.Instances;

/**
 *
 * @author santos
 */
public class Node extends UtilFunctions {

    private int id = 0;//identificador
    private NodeTypeEnum typeNode;//me baso en el tipo para sacar url, nombre, imagen
    private String x = "";
    private String stateNode = "";//estado puede tomar tres valores _r _v _a  (ROJO=no_configurado AMARILLO=configurado VERDE=funcionando )
    private String y = "";
    private ArrayList<String> parents = new ArrayList<>();
    private ArrayList<String> childrens = new ArrayList<>();

    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private PlainText dataPlainText;
    private ConnectionDB dataConnectionDB;
    private RemoveMissing dataRemoveMissing;
    private ReplaceValue dataReplaceValue;
    private UpdateMissing dataUpdateMissing;
    private Selection dataSelection;
    private DataAnalisis dataAnalisis;
    private ArfSaver dataArfSaver;
    private CsvSaver dataCsvSaver;
    private SelectAttributes dataSelectAttributes;
    private DiscretizeAttribute dataDiscretize;
    private NumericToNominalFilter dataNumericToNominal;
    private SamplingPercentage dataSamplingPercentage;
    private Codification dataCodification;
    private KNNImputation dataKNNImputation;
    private Metrics dataMetrics;
    //private AssociationApriori dataAssociationApriori;
    //private AssociationFpGrowth dataAssociationFpGrowth;
    //private AssociationEquipAsso dataAssociationEquipAsso;
    private Association dataAssociation;
    private Classification dataClassification;
    private Cluster dataCluster;
    private NominalToBinaryFilter dataNominalToBinary;

    public Node(int id, NodeTypeEnum typeNode, String x, String y, String stateNode, GraphicControlMB imb) {

        this.id = id;
        this.x = x;
        this.y = y;
        this.typeNode = typeNode;
        this.stateNode = stateNode;
        this.graphicControlMB = imb;
        dataPlainText = new PlainText(this);
        dataConnectionDB = new ConnectionDB(this);
        dataRemoveMissing = new RemoveMissing(this, imb);
        dataReplaceValue = new ReplaceValue(this, imb);
        dataUpdateMissing = new UpdateMissing(this, imb);
        dataSelection = new Selection(this, imb);
        dataAnalisis = new DataAnalisis(this, imb);
        dataArfSaver = new ArfSaver(this, imb);
        dataCsvSaver = new CsvSaver(this, imb);
        dataDiscretize = new DiscretizeAttribute(this, imb);
        dataNumericToNominal = new NumericToNominalFilter(this, imb);
        dataSamplingPercentage = new SamplingPercentage(this, imb);
        dataSelectAttributes = new SelectAttributes(this, imb);
        dataCodification = new Codification(this, imb);
        dataKNNImputation = new KNNImputation(this, imb);
        dataMetrics = new Metrics(this, imb);
        //dataAssociationApriori = new AssociationApriori(this, imb);
        //dataAssociationFpGrowth = new AssociationFpGrowth(this, imb);
        //dataAssociationEquipAsso = new AssociationEquipAsso(this, imb);
        dataAssociation = new Association(this, imb);
        dataClassification = new Classification(this, imb);
        dataCluster = new Cluster(this, imb);
        dataNominalToBinary = new NominalToBinaryFilter(this, imb);
    }

    public void repaintGraphic() {
        graphicControlMB.repaintGraphic();
    }

    public void reserNode() {//dejar en estado inicial un nodo
        switch (typeNode) {
            case PlainText://"Connections"-------------------------------
                dataPlainText.reset();
                break;
            case ConnectionDB:
                dataConnectionDB.reset();
                break;
            case ArfSaver:
                dataArfSaver.reset();
                break;
            case CsvSaver:
                dataCsvSaver.reset();
                break;
            case Selection://Filters > Selection-------------------------------
                dataSelection.reset();
                break;
            case SelectAttributes:
                dataSelectAttributes.reset();
                break;
            case Discretize:
                dataDiscretize.reset();
                break;
            case RemoveMissing://"Filters"-------------------------------
                dataRemoveMissing.reset();
                break;
            case UpdateMissing:
                dataUpdateMissing.reset();
                break;
            case ReplaceValue:
                dataReplaceValue.reset();
                break;
            case SamplingPercentage:
                dataSamplingPercentage.reset();
                break;
            case KNNImputation:
                dataKNNImputation.reset();
                break;
            case Metrics:
                dataMetrics.reset();
                break;
            case NumericToNominal:
                dataNumericToNominal.reset();
                break;
            case NominalToBinary:
                dataNominalToBinary.reset();
                break;
            case Codification:
                dataCodification.reset();
                break;
//            case Apriori://"Association"-------------------------------
//                dataAssociationApriori.reset();
//                break;
//            case FPGrowth:
//                dataAssociationFpGrowth.reset();
//                break;
            case Association:
                dataAssociation.reset();
                break;
            case Classification://"Clasification"-------------------------------
                dataClassification.reset();
                break;
            case Cluster:
                dataCluster.reset();
                break;
//            case Generator://"Views"-------------------------------                
//            case HierarchicalTree:
//            case WekaTree:
//            case TextTree:
//            case Prediction:
//                break;
            case DataAnalisis:
                dataAnalisis.reset();
                break;
        }
        stateNode = "_r";
    }

    public void resetChildrenNodes() {//dejar en estado desconfigurado los nodos hijos
        for (String children : childrens) {
            Node search = graphicControlMB.findNodeById(Integer.parseInt(children));
            search.reserNode();
            search.resetChildrenNodes();
        }
    }

    public MenuModel getMenuModel() {//Obtiene el menu dependiendo del tipo de nodo
        switch (typeNode) {
            case PlainText://"Connections"-------------------------------
                return dataPlainText.getMenuModel();
            case ConnectionDB:
                return dataConnectionDB.getMenuModel();
            case ArfSaver:
                return dataArfSaver.getMenuModel();
            case CsvSaver:
                return dataCsvSaver.getMenuModel();
            case Selection://Filters > Selection-------------------------------
                return dataSelection.getMenuModel();
            case SelectAttributes:
                return dataSelectAttributes.getMenuModel();
            case Discretize:
                return dataDiscretize.getMenuModel();
            case RemoveMissing://"Filters"-------------------------------
                return dataRemoveMissing.getMenuModel();
            case UpdateMissing:
                return dataUpdateMissing.getMenuModel();
            case ReplaceValue:
                return dataReplaceValue.getMenuModel();
            case SamplingPercentage:
                return dataSamplingPercentage.getMenuModel();
            case KNNImputation:
                return dataKNNImputation.getMenuModel();
            case Metrics:
                return dataMetrics.getMenuModel();
            case NumericToNominal:
                return dataNumericToNominal.getMenuModel();
            case NominalToBinary:
                return dataNominalToBinary.getMenuModel();
            case Codification:
                return dataCodification.getMenuModel();
            case DataAnalisis:
                return dataAnalisis.getMenuModel();
//            case Apriori:
//                return dataAssociationApriori.getMenuModel();
//            case FPGrowth:
//                return dataAssociationFpGrowth.getMenuModel();
            case Association:
                return dataAssociation.getMenuModel();
            case Classification:
                return dataClassification.getMenuModel();
            case Cluster:
                return dataCluster.getMenuModel();
//            case Generator:
//            case HierarchicalTree:
//            case WekaTree:
//            case TextTree:
//            case Prediction:
//                return graphicControlMB.getDefaultMenuModel();
        }
        return graphicControlMB.getDefaultMenuModel();
    }

    public Instances getData() {//obtiene Instances apartir del tipo de nodo
        switch (typeNode) {
            case PlainText://"Connections"-------------------------------
                return dataPlainText.getData();
            case ConnectionDB:
                return dataConnectionDB.getData();
            case RemoveMissing://"Filters"-------------------------------
                return dataRemoveMissing.getData();
            case UpdateMissing:
                return dataUpdateMissing.getData();
            case KNNImputation:
                return dataKNNImputation.getData();
            case Metrics:
                return dataMetrics.getData();
            case Selection:
                return dataSelection.getData();
            case SelectAttributes:
                return dataSelectAttributes.getData();
            case Codification:
                return dataCodification.getData();
            case ReplaceValue:
                return dataReplaceValue.getData();
            case Discretize:
                return dataDiscretize.getData();
            case NumericToNominal:
                return dataNumericToNominal.getData();
            case NominalToBinary:
                return dataNominalToBinary.getData();
            case SamplingPercentage:
                return dataSamplingPercentage.getData();
//            case Apriori://"Association"-------------------------------
//                return dataAssociationApriori.getData();
//            case FPGrowth:
//                return dataAssociationFpGrowth.getData();
            case Association:
                return dataAssociation.getData();
            case Classification://"Clasification"-------------------------------
                return dataClassification.getData();
            case Cluster:
                return dataCluster.getData();
//            case Generator://"Views"-------------------------------
//            case HierarchicalTree:
//            case WekaTree:
//            case TextTree:
//            case Prediction:
//                return null;
        }
        return null;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //-------------------- FUNCIONES GET AND SET -------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PlainText getDataPlainText() {
        return dataPlainText;
    }

    public void setDataPlainText(PlainText dataPlainText) {
        this.dataPlainText = dataPlainText;
    }

    public ConnectionDB getDataConnectionDB() {
        return dataConnectionDB;
    }

    public void setDataConnectionDB(ConnectionDB dataConnectionDB) {
        this.dataConnectionDB = dataConnectionDB;
    }

    public RemoveMissing getDataRemoveMissing() {
        return dataRemoveMissing;
    }

    public void setDataRemoveMissing(RemoveMissing dataRemoveMissing) {
        this.dataRemoveMissing = dataRemoveMissing;
    }

    public NodeTypeEnum getTypeNode() {
        return typeNode;
    }

    public void setTypeNode(NodeTypeEnum typeNode) {
        this.typeNode = typeNode;
    }

    public String getStateNode() {
        return stateNode;
    }

    public void setStateNode(String stateNode) {
        this.stateNode = stateNode;
    }

    public ArrayList<String> getParents() {
        return parents;
    }

    public void setParents(ArrayList<String> parents) {
        this.parents = parents;
    }

    public ArrayList<String> getChildrens() {
        return childrens;
    }

    public void setChildrens(ArrayList<String> childrens) {
        this.childrens = childrens;
    }

    public GraphicControlMB getGraphicControlMB() {
        return graphicControlMB;
    }

    public void setGraphicControlMB(GraphicControlMB graphicControlMB) {
        this.graphicControlMB = graphicControlMB;
    }

    public ReplaceValue getDataReplaceValue() {
        return dataReplaceValue;
    }

    public void setDataReplaceValue(ReplaceValue dataReplaceValue) {
        this.dataReplaceValue = dataReplaceValue;
    }

    public UpdateMissing getDataUpdateMissing() {
        return dataUpdateMissing;
    }

    public void setDataUpdateMissing(UpdateMissing dataUpdateMissing) {
        this.dataUpdateMissing = dataUpdateMissing;
    }

    public Selection getDataSelection() {
        return dataSelection;
    }

    public void setDataSelection(Selection dataSelection) {
        this.dataSelection = dataSelection;
    }

    public DataAnalisis getDataAnalisis() {
        return dataAnalisis;
    }

    public void setDataAnalisis(DataAnalisis dataAnalisis) {
        this.dataAnalisis = dataAnalisis;
    }

    public ArfSaver getDataArfSaver() {
        return dataArfSaver;
    }

    public void setDataArfSaver(ArfSaver dataArfSaver) {
        this.dataArfSaver = dataArfSaver;
    }

    public CsvSaver getDataCsvSaver() {
        return dataCsvSaver;
    }

    public void setDataCsvSaver(CsvSaver dataCsvSaver) {
        this.dataCsvSaver = dataCsvSaver;
    }

    public SelectAttributes getDataSelectAttributes() {
        return dataSelectAttributes;
    }

    public void setDataSelectAttributes(SelectAttributes dataSelectAttributes) {
        this.dataSelectAttributes = dataSelectAttributes;
    }

    public DiscretizeAttribute getDataDiscretize() {
        return dataDiscretize;
    }

    public void setDataDiscretize(DiscretizeAttribute dataDiscretize) {
        this.dataDiscretize = dataDiscretize;
    }

    public NominalToBinaryFilter getDataNominalToBinary() {
        return dataNominalToBinary;
    }

    public void setDataNominalToBinary(NominalToBinaryFilter dataNominalToBinary) {
        this.dataNominalToBinary = dataNominalToBinary;
    }

    public NumericToNominalFilter getDataNumericToNominal() {
        return dataNumericToNominal;
    }

    public void setDataNumericToNominal(NumericToNominalFilter dataNumericToNominal) {
        this.dataNumericToNominal = dataNumericToNominal;
    }

    public SamplingPercentage getDataSamplingPercentage() {
        return dataSamplingPercentage;
    }

    public void setDataSamplingPercentage(SamplingPercentage dataSamplingPercentage) {
        this.dataSamplingPercentage = dataSamplingPercentage;
    }

    public Codification getDataCodification() {
        return dataCodification;
    }

    public void setDataCodification(Codification dataCodification) {
        this.dataCodification = dataCodification;
    }

    public KNNImputation getDataKNNImputation() {
        return dataKNNImputation;
    }

    public void setDataKNNImputation(KNNImputation dataKNNImputation) {
        this.dataKNNImputation = dataKNNImputation;
    }

    public Metrics getDataMetrics() {
        return dataMetrics;
    }

    public void setDataMetrics(Metrics dataMetrics) {
        this.dataMetrics = dataMetrics;
    }

//    public AssociationApriori getDataAssociationApriori() {
//        return dataAssociationApriori;
//    }
//
//    public void setDataAssociationApriori(AssociationApriori dataAssociationApriori) {
//        this.dataAssociationApriori = dataAssociationApriori;
//    }
//
//    public AssociationFpGrowth getDataAssociationFpGrowth() {
//        return dataAssociationFpGrowth;
//    }
//
//    public void setDataAssociationFpGrowth(AssociationFpGrowth dataAssociationFpGrowth) {
//        this.dataAssociationFpGrowth = dataAssociationFpGrowth;
//    }

    public Association getDataAssociation() {
        return dataAssociation;
    }

    public void setDataAssociation(Association dataAssociation) {
        this.dataAssociation = dataAssociation;
    }

    public Classification getDataClassification() {
        return dataClassification;
    }

    public void setDataClassification(Classification dataClassification) {
        this.dataClassification = dataClassification;
    }

    public Cluster getDataCluster() {
        return dataCluster;
    }

    public void setDataCluster(Cluster dataCluster) {
        this.dataCluster = dataCluster;
    }

}
