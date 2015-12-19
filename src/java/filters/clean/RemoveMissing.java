/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters.clean;

import javax.faces.application.FacesMessage;
import managedBeans.GraphicControlMB;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.DinamicTable;
import util.Node;
import util.UtilFunctions;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;

/**
 *
 * @author santos
 */
public class RemoveMissing extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;
    private RemoveWithValues filterRemove = new RemoveWithValues();
    private DinamicTable dinamicTable = new DinamicTable();

    public RemoveMissing(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Run", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataRemoveMissing.runProcess}", "Run process", ":IdFormDialogsRemoveMissing", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewRemoveMissing').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsRemoveMissing", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));        
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgRemoveMissingHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void runProcess() {
        if (currentNode.getParents().isEmpty()) {//se verifica que el nodo tenga fuente de datos
            printMessage("Error", "This node do not have data source", FacesMessage.SEVERITY_ERROR);
            return;
        }
        Node nodeParent = graphicControlMB.findNodeById(Integer.parseInt(currentNode.getParents().get(0)));
        if (nodeParent.getStateNode().compareTo("_v") != 0) {//se verifica que el nodo este configurado   
            printMessage("Error", "You must configure and run the parent node", FacesMessage.SEVERITY_ERROR);
            return;
        }
        currentNode.resetChildrenNodes();
        filterRemove.setMatchMissingValues(false);
        try {
            data = new Instances(nodeParent.getData());
            filterRemove.setInputFormat(data);
            String[] options = new String[8];
            for (int i = 0; i < nodeParent.getDataPlainText().getData().numAttributes(); i++) {
                options[0] = "-S";
                options[1] = "Infinity";
                options[2] = "-C";
                options[3] = String.valueOf(i + 1);//"2";
                options[4] = "-L";
                options[5] = "first-last";
                options[6] = "-V";      //invert selection
                options[7] = "-M";     //mising values               
                filterRemove.setOptions(options);//System.out.println(String.valueOf(i)+"////////////////" + data);
                data = Filter.useFilter(data, filterRemove);//System.out.println(String.valueOf(i)+"-------------------" + data);
            }
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

}
