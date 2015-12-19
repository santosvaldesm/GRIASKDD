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
import weka.core.Attribute;
import weka.core.Instances;

/**
 *
 * @author santos
 */
public class ReplaceValue extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;
    private DinamicTable dinamicTable = new DinamicTable();
    private int idAtributeSelected = -1;
    private List<Attribute> listAttributes = new ArrayList<>();
    private List<ReplaceRow> listValues = new ArrayList<>();
    //private List<ReplaceRow> selectedValues = new ArrayList<>();
    //private String replaceValue = "";//valor a reemplazar

    public ReplaceValue(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataReplaceValue.showConfigure}", "Configure", "", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("Run", Boolean.TRUE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataReplaceValue.runProcess}", "Run process", ":IdFormDialogsReplaceValue", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewReplaceValue').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsReplaceValue", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgReplaceValueHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
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
        idAtributeSelected = -1;
        for (int i = 0; i < data.numAttributes(); i++) {
            listAttributes.add(data.attribute(i));
            if (i == 0) {
                idAtributeSelected = 0;
                listValues = new ArrayList<>();
                for (int j = 0; j < data.attribute(i).numValues(); j++) {
                    listValues.add(new ReplaceRow(i, data.attribute(i).value(j), "", data.attribute(i).index()));
                }
            }
        }
        RequestContext.getCurrentInstance().update("IdFormDialogsReplaceValue");
        RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureReplaceValue').show()");
    }

    public void changeAtribute() {
        listValues = new ArrayList<>();
        for (int j = 0; j < data.attribute(idAtributeSelected).numValues(); j++) {
            listValues.add(new ReplaceRow(j, data.attribute(idAtributeSelected).value(j), "", data.attribute(idAtributeSelected).index()));
        }
    }

    public void saveConfiguration() {
        currentNode.resetChildrenNodes();
        currentNode.setStateNode("_a");
        changeDisabledOption("Run", Boolean.FALSE, submenu);
        RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureReplaceValue').hide()");
        currentNode.repaintGraphic();
    }

    public void runProcess() {
        for (ReplaceRow selectedValue : listValues) {
            if (!isEmpty(selectedValue.getReplace())) {
                data.renameAttributeValue(data.attribute(idAtributeSelected), selectedValue.getValue(), selectedValue.getReplace());
            }
        }//System.out.println(data);
        if (data != null) {//LLENAR LOS DATOS DE INSTANCES A LA TABLA DINAMICA
            dinamicTable = convertInstancesToDinamicTable(data);
        }
        currentNode.setStateNode("_v");
        changeDisabledOption("View", Boolean.FALSE, submenu);
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

    public int getIdAtributeSelected() {
        return idAtributeSelected;
    }

    public void setIdAtributeSelected(int idAtributeSelected) {
        this.idAtributeSelected = idAtributeSelected;
    }

    public List<Attribute> getListAttributes() {
        return listAttributes;
    }

    public void setListAttributes(List<Attribute> listAttributes) {
        this.listAttributes = listAttributes;
    }

    public List<ReplaceRow> getListValues() {
        return listValues;
    }

    public void setListValues(List<ReplaceRow> listValues) {
        this.listValues = listValues;
    }

}
