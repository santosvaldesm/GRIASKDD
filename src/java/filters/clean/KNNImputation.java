/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters.clean;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import managedBeans.GraphicControlMB;
import org.primefaces.context.RequestContext;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.DinamicTable;
import util.Node;
import util.UtilFunctions;
import util.filters.ReplaceRow;
import weka.classifiers.lazy.IBk;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddValues;
import weka.filters.unsupervised.attribute.ReplaceMissingWithUserConstant;
import weka.filters.unsupervised.instance.RemoveWithValues;

/**
 *
 * @author santos
 */
public class KNNImputation extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;
    private Instances train;//datos sin nulos
    private Instances predict;//datos con nulos
    private RemoveWithValues filterRemove = new RemoveWithValues();
    private DinamicTable dinamicTable = new DinamicTable();

    private List<ReplaceRow> listAttributes = new ArrayList<>();
    private List<String> selectedAttributes = new ArrayList<>();

    public KNNImputation(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataKNNImputation.showConfigure}", "Configure", "", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("Run", Boolean.TRUE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataKNNImputation.runProcess}", "Run process", ":IdFormDialogsKNNImputation", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewKNNImputation').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsKNNImputation", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgKNNImputationHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void changeForm() {//Cambio en el formulario(esto para que envie los datos al nodo correspondiente)
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
        data = new Instances(nodeParent.getData());
        listAttributes = new ArrayList<>();
        AttributeStats attributeStats;
        for (int i = 0; i < data.numAttributes(); i++) {
            attributeStats = data.attributeStats(i);
            if (attributeStats.nominalCounts != null) {//es un atributo nominal
                listAttributes.add(new ReplaceRow(i + 1, data.attribute(i).name(), "", data.attribute(i).index()));
            }
        }
        RequestContext.getCurrentInstance().update("IdFormDialogsKNNImputation");
        RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureKNNImputation').show()");
    }

    public void saveConfiguration() {
        if (selectedAttributes == null || selectedAttributes.isEmpty()) {
            printMessage("Error", "You must select attributes", FacesMessage.SEVERITY_ERROR);
            return;
        }
        currentNode.resetChildrenNodes();
        currentNode.setStateNode("_a");
        changeDisabledOption("Run", Boolean.FALSE, submenu);
        // changeDisabledOption("Configure", Boolean.TRUE, submenu);
        RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureKNNImputation').hide()");
        currentNode.repaintGraphic();
    }

    public void runProcess() {
        for (String attributeIndex : selectedAttributes) {
            int classIndex = Integer.parseInt(attributeIndex) - 1;
            train = findNotNullInstances(attributeIndex);
            predict = findNullInstances(attributeIndex);
            if (train.attribute(classIndex).numValues() == 1) {//si solo hay un valor se reemplaza por ese valor
                for (int i = 0; i < data.numInstances(); i++) {
                    if (data.get(i).isMissing(classIndex)) {
                        data.get(i).setValue(classIndex, train.attribute(classIndex).value(0));
                    }
                }
                System.out.println(data);
            } else if (train != null && predict != null && train.numInstances() != 0 && predict.numInstances() != 0) {
                //validado que haya instancias en train y en predict
                train.setClassIndex(classIndex);
                predict.deleteAttributeAt(classIndex);
                predict.insertAttributeAt(train.attribute(classIndex), classIndex);
                predict.setClassIndex(classIndex);
                ArrayList<Imputed> KNNPrediction = new ArrayList<>();
                try {
                    IBk knn = new IBk(1);
                    knn.buildClassifier(train);
                    for (int i = 0; i < predict.numInstances(); i++) {
                        double pred = knn.classifyInstance(predict.instance(i));
                        Instance returned = knn.getNearestNeighbourSearchAlgorithm().nearestNeighbour(predict.instance(i));
                        double[] dist = knn.distributionForInstance(predict.instance(i));
                        Imputed imputed = new Imputed();
                        imputed.setTuple(returned.toString());//colocar la tupla correspondiente
                        imputed.setOrder(i + 1);
                        imputed.setActual(predict.instance(i).toString(predict.classIndex()));// print the actual value                        
                        imputed.setPredicted(predict.classAttribute().value((int) pred));// print the prediceted value                        
                        imputed.setConfidence(dist[(int) pred]);// print the confidence of the prediction
                        KNNPrediction.add(imputed);
                    }
                    int indexReplacement=0;//en KNNPrediction estan la predicciones se deben asignar a data
                    for (int i = 0; i < data.numInstances(); i++) {
                        if (data.get(i).isMissing(classIndex)) {
                            data.get(i).setValue(classIndex,KNNPrediction.get(indexReplacement++).getPredicted());
                        }
                    }
                } catch (Exception ex) {
                    printMessage("Error", ex.toString(), FacesMessage.SEVERITY_ERROR);
                }
            }
        }
        if (data != null) {//LLENAR LOS DATOS DE INSTANCES A LA TABLA DINAMICA
            dinamicTable = convertInstancesToDinamicTable(data);
        }
        currentNode.setStateNode("_v");
        changeDisabledOption("View", Boolean.FALSE, submenu);
        currentNode.repaintGraphic();
    }

    private Instances findNotNullInstances(String attribteIndex) {//RETORNA LAS INSTANCIAS NO NULAS 
        try {
            filterRemove.setInputFormat(data);
            String[] options = new String[8];
            options[0] = "-S";
            options[1] = "Infinity";
            options[2] = "-C";
            options[3] = attribteIndex;//"2";
            options[4] = "-L";
            options[5] = "first-last";
            options[6] = "-V";      //invert selection
            options[7] = "-M";     //mising values               
            filterRemove.setOptions(options);//System.out.println(String.valueOf(i)+"////////////////" + data);            
            return Filter.useFilter(data, filterRemove);
        } catch (Exception ex) {
            printMessage("Error", ex.toString(), FacesMessage.SEVERITY_ERROR);
            return null;
        }
    }

    private Instances findNullInstances(String attribteIndex) {//RETORNA LAS INSTANCIAS NO NULAS 
        try {
            filterRemove.setInputFormat(data);
            String[] options = new String[6];
            options[0] = "-S";
            options[1] = "0.0";
            options[2] = "-C";
            options[3] = attribteIndex;//"2";
            options[4] = "-L";
            options[5] = "first-last";
            filterRemove.setOptions(options);//System.out.println(String.valueOf(i)+"////////////////" + data);            
            return Filter.useFilter(data, filterRemove);
        } catch (Exception ex) {
            printMessage("Error", ex.toString(), FacesMessage.SEVERITY_ERROR);
            return null;
        }
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //-------------------- FUNCIONES GET AND SET -------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
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

    public List<ReplaceRow> getListAttributes() {
        return listAttributes;
    }

    public void setListAttributes(List<ReplaceRow> listAttributes) {
        this.listAttributes = listAttributes;
    }

    public List<String> getSelectedAttributes() {
        return selectedAttributes;
    }

    public void setSelectedAttributes(List<String> selectedAttributes) {
        this.selectedAttributes = selectedAttributes;
    }

}
