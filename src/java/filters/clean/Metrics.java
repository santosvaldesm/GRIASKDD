/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters.clean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.faces.application.FacesMessage;
import managedBeans.GraphicControlMB;
import org.primefaces.context.RequestContext;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.DinamicTable;
import util.MetricsEnum;
import util.ModeNumeric;
import util.Node;
import util.UtilFunctions;
import util.filters.ReplaceRow;
import weka.core.Instances;

/**
 *
 * @author santos
 */
public class Metrics extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;
    private DinamicTable dinamicTable = new DinamicTable();

    private List<ReplaceRow> listAttributes = new ArrayList<>();
    private List<String> selectedAttributes = new ArrayList<>();
    private String nominalStrategy = "";
    private String numericStrategy = "";
    //private double variance = 0.0;
    //private boolean disabledVariance = true;

    public Metrics(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataMetrics.showConfigure}", "Configure", "", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("Run", Boolean.TRUE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataMetrics.runProcess}", "Run process", ":IdFormDialogsMetrics", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewMetrics').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsMetrics", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgMetricsHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
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
        for (int i = 0; i < data.numAttributes(); i++) {
            listAttributes.add(new ReplaceRow(i + 1, data.attribute(i).name(), "", data.attribute(i).index()));
        }
        RequestContext.getCurrentInstance().update("IdFormDialogsMetrics");
        RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureMetrics').show()");
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
        RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureMetrics').hide()");
        currentNode.repaintGraphic();
    }

    public void runProcess() {
        //AttributeStats attributeStats;
        String replaceNominal = "";
        for (String attribute : selectedAttributes) {
            int attributeIndex = Integer.parseInt(attribute) - 1;
            //attributeStats = data.attributeStats(attributeIndex - 1);
            data.attribute(attributeIndex);
            if (data.attributeStats(attributeIndex).nominalCounts != null) {//es un atributo nominal
                switch (MetricsEnum.convert(nominalStrategy)) {
                    case Mode://determinar moda                        
                        int max = 0;
                        for (int i = 0; i < data.attributeStats(attributeIndex).nominalCounts.length; i++) {
                            if (data.attributeStats(attributeIndex).nominalCounts[i] > max) {
                                replaceNominal = data.attribute(attributeIndex).value(i);
                                max = data.attributeStats(attributeIndex).nominalCounts[i];
                            }
                        }
                        for (int i = 0; i < data.numInstances(); i++) {
                            if (data.get(i).isMissing(attributeIndex)) {
                                data.get(i).setValue(attributeIndex, replaceNominal);
                            }
                        }
                        break;
                    case Minimum://determinar minimo
                        int min = 0;
                        for (int i = 0; i < data.attributeStats(attributeIndex).nominalCounts.length; i++) {
                            if (data.attributeStats(attributeIndex).nominalCounts[i] < min) {
                                replaceNominal = data.attribute(attributeIndex).value(i);
                                min = data.attributeStats(attributeIndex).nominalCounts[i];
                            }
                        }
                        if (min != 0) {
                            for (int i = 0; i < data.numInstances(); i++) {
                                if (data.get(i).isMissing(attributeIndex)) {
                                    data.get(i).setValue(attributeIndex, replaceNominal);
                                }
                            }
                        }
                        break;
                    case NOVALUE:
                        break;

                }
            }
            if (data.attributeStats(attributeIndex).numericStats != null) {//es un atributo numerico
                switch (MetricsEnum.convert(numericStrategy)) {
                    case Average:
                        for (int i = 0; i < data.numInstances(); i++) {
                            if (data.get(i).isMissing(attributeIndex)) {
                                data.get(i).setValue(attributeIndex, data.attributeStats(attributeIndex).numericStats.mean);
                            }
                        }
                        break;
                    case Median://Valor central despues de ordenar
                        ArrayList<Double> orderList = new ArrayList<>();
                        boolean add = false;
                        for (int i = 0; i < data.numInstances(); i++) {
                            if (!data.get(i).isMissing(attributeIndex)) {
                                orderList.add(data.get(i).value(attributeIndex));
                            }
                        }
                        Collections.sort(orderList);//for (Double orderList1 : orderList) {System.out.println("Ordenado" + orderList1);}
                        double medianReplace;
                        if ((orderList.size() % 2) == 0) {//es par
                            medianReplace = orderList.get(orderList.size() / 2);
                        } else {//es impar
                            medianReplace = orderList.get((orderList.size() - 1) / 2);
                        }
                        for (int i = 0; i < data.numInstances(); i++) {//se realiza los reemplazos
                            if (data.get(i).isMissing(attributeIndex)) {
                                data.get(i).setValue(attributeIndex, medianReplace);
                            }
                        }
                        break;
                    case Mode://determinar moda en attributo numerico
                        ArrayList<ModeNumeric> numberAndCount = new ArrayList<>();
                        boolean founValue;
                        for (int i = 0; i < data.numInstances(); i++) {//se realiza los reemplazos
                            if (!data.get(i).isMissing(attributeIndex)) {
                                founValue = false;//buscarlo en el arreglo
                                for (ModeNumeric numCount : numberAndCount) {
                                    if (numCount.getValue() == data.get(i).value(attributeIndex)) {//se encuentra en el arreglo
                                        founValue = true;
                                        numCount.setCount(numCount.getCount() + 1);
                                    }
                                }
                                if (!founValue) {//no se encontro en el arreglo
                                    numberAndCount.add(new ModeNumeric(data.get(i).value(attributeIndex), 1));
                                }
                            }
                        }
                        if (!numberAndCount.isEmpty()) {
                            int max = 0;
                            double modeReplace = 0.0;
                            for (ModeNumeric numCount : numberAndCount) {//determino cual es el mayor
                                if (numCount.getCount() > max) {
                                    max = numCount.getCount();
                                    modeReplace = numCount.getValue();
                                }
                            }
                            for (int i = 0; i < data.numInstances(); i++) {//se realiza los reemplazos
                                if (data.get(i).isMissing(attributeIndex)) {
                                    data.get(i).setValue(attributeIndex, modeReplace);
                                }
                            }
                        }
                        break;
                    case PreserveDeviation:
                        int countElements;
                        double mean;
                        double deviation;
                        double sum;
                        double variance;
                        double range;
                        double valueMinusMeanPowTwoSum;//sumatoria de (x-media)cuadrado
                        double replaceValue;
                        int missingCount=data.attributeStats(attributeIndex).missingCount;
                        for (int missingReplace = 0; missingReplace < data.attributeStats(attributeIndex).missingCount; missingReplace++) {
                            countElements = 0;
                            sum = 0.0;
                            variance = 0.0;
                            valueMinusMeanPowTwoSum = 0.0;
                            for (int i = 0; i < data.numInstances(); i++) {//calculo de la media
                                if (!data.get(i).isMissing(attributeIndex)) {
                                    countElements++;
                                    sum = sum + data.get(i).value(attributeIndex);
                                }
                            }
                            mean = sum / countElements;
                            for (int i = 0; i < data.numInstances(); i++) {//se hace la suma de las diferencias respecto a a lamedia
                                if (!data.get(i).isMissing(attributeIndex)) {
                                    range = Math.pow(data.get(i).value(attributeIndex) - mean, 2f);
                                    variance = variance + range;
                                }
                            }
                            variance = variance / (countElements - 1);//suma de diferencias sobre "n"                        
                            deviation = Math.sqrt(variance);//raiz de varinza =  desviación estandar

                            for (int i = 0; i < data.numInstances(); i++) {//se hace la suma de las diferencias respecto a a lamedia
                                if (!data.get(i).isMissing(attributeIndex)) {
                                    valueMinusMeanPowTwoSum = valueMinusMeanPowTwoSum + Math.pow(data.get(i).value(attributeIndex) - mean, 2f);
                                }
                            }
                            replaceValue = Math.sqrt((Math.pow(deviation, 2f) * countElements) - valueMinusMeanPowTwoSum) + mean;
                            System.out.println(
                                    "Media: " + mean + "\t\t"
                                    + "Varianza: " + variance + "\t\t"
                                    + "Desviacion: " + deviation + "\t\t"
                                    + "Valor Predecido: " + replaceValue);
                            for (int i = 0; i < data.numInstances(); i++) {//buscar un nulo y reemplazar
                                if (data.get(i).isMissing(attributeIndex)) {
                                    data.get(i).setValue(attributeIndex, replaceValue);
                                    break;
                                }
                            }
                        }
                        break;
                    case NOVALUE:
                        break;
                }
            }
        }
        if (data != null) {//LLENAR LOS DATOS DE INSTANCES A LA TABLA DINAMICA
            dinamicTable = convertInstancesToDinamicTable(data);
        }
        currentNode.setStateNode("_v");
        changeDisabledOption("View", Boolean.FALSE, submenu);
        changeDisabledOption("Run", Boolean.TRUE, submenu);
        currentNode.repaintGraphic();
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

    public String getNominalStrategy() {
        return nominalStrategy;
    }

    public void setNominalStrategy(String nominalStrategy) {
        this.nominalStrategy = nominalStrategy;
    }

    public String getNumericStrategy() {
        return numericStrategy;
    }

    public void setNumericStrategy(String numericStrategy) {
        this.numericStrategy = numericStrategy;
    }

//    public double getVariance() {
//        return variance;
//    }
//
//    public void setVariance(double variance) {
//        this.variance = variance;
//    }
//    public boolean isDisabledVariance() {
//        return disabledVariance;
//    }
//
//    public void setDisabledVariance(boolean disabledVariance) {
//        this.disabledVariance = disabledVariance;
//    }
}
