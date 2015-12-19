/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters.transformation;

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
import weka.filters.unsupervised.attribute.NominalToBinary;


/**
 *
 * @author santos
 */
public class NominalToBinaryFilter extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;
    private NominalToBinary nominalToBinary = new NominalToBinary();
    private DinamicTable dinamicTable = new DinamicTable();

    private List<ReplaceRow> listAttributes = new ArrayList<>();
    private List<String> selectedAttributes = new ArrayList<>();
//    private String dateFormat = "";
//    private String dateReplacementValue = "";

    private boolean binaryAttributesNominal = false;

    private boolean invertSelection = false;
    private boolean transformAllValues = false;

    public NominalToBinaryFilter(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }
    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu= new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataNominalToBinary.showConfigure}", "Configure", "", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("Run", Boolean.TRUE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataNominalToBinary.runProcess}", "Run process", ":IdFormDialogsUpdateMissing", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewNominalToBinary').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsNominalToBinary", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));        
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgNominalToBinaryHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
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
            listAttributes.add(new ReplaceRow(i + 1, data.attribute(i).name(), "",data.attribute(i).index()));
        }
        RequestContext.getCurrentInstance().update("IdFormDialogsNominalToBinary");
        RequestContext.getCurrentInstance().execute("PF('wvDlgNominalToBinary').show()");
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
        RequestContext.getCurrentInstance().execute("PF('wvDlgNominalToBinary').hide()");
        currentNode.repaintGraphic();
    }

    public void runProcess() {
        try {
            String idAttributes = "";
            for (String selected : selectedAttributes) {
                idAttributes = idAttributes + selected + ",";
            }
            idAttributes = idAttributes.substring(0, idAttributes.length() - 1);//eliminar ultima coma
            nominalToBinary.setAttributeIndices(idAttributes);
            nominalToBinary.setBinaryAttributesNominal(binaryAttributesNominal);
            nominalToBinary.setTransformAllValues(transformAllValues);
            nominalToBinary.setInvertSelection(invertSelection);

            nominalToBinary.setInputFormat(data);
            data = Filter.useFilter(data, nominalToBinary);//System.out.println(data);
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

    public boolean isBinaryAttributesNominal() {
        return binaryAttributesNominal;
    }

    public void setBinaryAttributesNominal(boolean binaryAttributesNominal) {
        this.binaryAttributesNominal = binaryAttributesNominal;
    }

    public boolean isTransformAllValues() {
        return transformAllValues;
    }

    public void setTransformAllValues(boolean transformAllValues) {
        this.transformAllValues = transformAllValues;
    }

    

    public boolean isInvertSelection() {
        return invertSelection;
    }

    public void setInvertSelection(boolean invertSelection) {
        this.invertSelection = invertSelection;
    }

}
