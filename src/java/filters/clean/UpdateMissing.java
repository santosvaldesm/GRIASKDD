/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters.clean;

import java.util.ArrayList;
import java.util.List;
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
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingWithUserConstant;

/**
 *
 * @author santos
 */
public class UpdateMissing extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;
    private ReplaceMissingWithUserConstant filterReplace = new ReplaceMissingWithUserConstant();
    private DinamicTable dinamicTable = new DinamicTable();

    private List<ReplaceRow> listAttributes = new ArrayList<>();
    private List<String> selectedAttributes = new ArrayList<>();
    private String dateFormat = "";
    private String dateReplacementValue = "";
    private boolean debug = false;
    private boolean checkCapabilities = false;
    private boolean ignoreClass = false;
    private String nominalReplace = "";
    private String numericReplace = "";

    public UpdateMissing(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataUpdateMissing.showConfigure}", "Configure", "", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("Run", Boolean.TRUE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataUpdateMissing.runProcess}", "Run process", ":IdFormDialogsUpdateMissing", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewUpdateMissing').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsUpdateMissing", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgUpdateMissingHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
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
        RequestContext.getCurrentInstance().update("IdFormDialogsUpdateMissing");
        RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureUpdateMissing').show()");
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
        RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureUpdateMissing').hide()");
        currentNode.repaintGraphic();
    }

    public void runProcess() {
        try {
//-------------------------------------------------------------------------------------------            
//------ESTE FILTRO NO FUNCIONA SI SE ADICIONA UN VALOR YA EXISTENTE EN LA CATEGORIA---------
//------ usar el siguiente codigo para agrega manualmente valores ---------------------------            
//-------------------------------------------------------------------------------------------            
//                try {
//                    AddValues addValue = new AddValues(); // new instance of filter                     
//                    addValue.setAttributeIndex(attributeIndex);
//                    addValue.setLabels(train.attribute(classIndex).value(0) + "d");
//                    addValue.setInputFormat(data); // to inform filter about dataset, tableData is initial Dataset from DB 
//                    data = Filter.useFilter(data, addValue); // apply filter                     
//                } catch (Exception e) {
//                    System.out.println(""+e.toString());
//                }                
            String idAttributes = "";
            for (String selected : selectedAttributes) {
                idAttributes = idAttributes + selected + ",";
            }
            idAttributes = idAttributes.substring(0, idAttributes.length() - 1);//eliminar ultima coma
            //System.out.println("Los atributos son: "+idAttributes);
            filterReplace.setIgnoreClass(ignoreClass);
            filterReplace.setAttributes(idAttributes);
            if (!isEmpty(nominalReplace)) {
                filterReplace.setNominalStringReplacementValue(nominalReplace);
            }
            if (!isEmpty(numericReplace)) {
                filterReplace.setNumericReplacementValue(numericReplace);
            }

            if (!isEmpty(dateFormat)) {
                filterReplace.setDateFormat(dateFormat);
            }
            if (!isEmpty(dateReplacementValue)) {
                filterReplace.setDateReplacementValue(dateReplacementValue);
            }
            filterReplace.setInputFormat(data);
            data = Filter.useFilter(data, filterReplace);//System.out.println(data);
            if (data != null) {//LLENAR LOS DATOS DE INSTANCES A LA TABLA DINAMICA
                dinamicTable = convertInstancesToDinamicTable(data);
            }
            currentNode.setStateNode("_v");
            changeDisabledOption("View", Boolean.FALSE, submenu);
        } catch (Exception ex) {
            printMessage("Error", ex.toString(), FacesMessage.SEVERITY_ERROR);
        }
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

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDateReplacementValue() {
        return dateReplacementValue;
    }

    public void setDateReplacementValue(String dateReplacementValue) {
        this.dateReplacementValue = dateReplacementValue;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isCheckCapabilities() {
        return checkCapabilities;
    }

    public void setCheckCapabilities(boolean checkCapabilities) {
        this.checkCapabilities = checkCapabilities;
    }

    public boolean isIgnoreClass() {
        return ignoreClass;
    }

    public void setIgnoreClass(boolean ignoreClass) {
        this.ignoreClass = ignoreClass;
    }

    public String getNominalReplace() {
        return nominalReplace;
    }

    public void setNominalReplace(String nominalReplace) {
        this.nominalReplace = nominalReplace;
    }

    public String getNumericReplace() {
        return numericReplace;
    }

    public void setNumericReplace(String numericReplace) {
        this.numericReplace = numericReplace;
    }

}
