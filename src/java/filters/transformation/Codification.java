/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters.transformation;

import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import managedBeans.GraphicControlMB;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.DinamicTable;
import util.Node;
import util.UtilFunctions;
import util.filters.ReplaceRow;
import weka.core.AttributeStats;
import weka.core.Instances;

/**
 *
 * @author santos
 */
public class Codification extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;
    private DinamicTable dinamicTable = new DinamicTable();
    ArrayList<ReplaceRow> dictionary = new ArrayList<>();

    public Codification(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Run", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataCodification.runProcess}", "Run process", ":IdFormDialogsCodification", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewCodification').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsCodification", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgCodificationHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void changeForm() {//Cambio en el formulario(esto para que envie los datos al nodo correspondiente)
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
        data = new Instances(nodeParent.getData());
        //genero el diccionario
        AttributeStats attributeStats;
        dictionary = new ArrayList<>();
        ReplaceRow itemDictionary;
        int countItemDictionary = 1;
        for (int idAttribute = 0; idAttribute < data.numAttributes(); idAttribute++) {
            attributeStats = data.attributeStats(idAttribute);
            if (attributeStats.nominalCounts != null) {//es un atributo nominal
                for (int idValue = 0; idValue < attributeStats.nominalCounts.length; idValue++) {
                    itemDictionary = new ReplaceRow(
                            idValue,
                            data.attribute(idAttribute).value(idValue),
                            String.valueOf(countItemDictionary++),
                            idAttribute);
                    itemDictionary.setAttributeName(data.attribute(idAttribute).name());
                    dictionary.add(itemDictionary);
                }
            }
        }
        for (ReplaceRow itemD : dictionary) {
            //System.out.println(itemD.getId() + " - " + itemD.getValue() + " - " + itemD.getReplace() + " - " + itemD.getIdAttribute());
            data.renameAttributeValue(data.attribute(itemD.getIdAttribute()), itemD.getValue(), itemD.getReplace());
        }

//        for (ReplaceRow selectedValue : listValues) {
//            if (!isEmpty(selectedValue.getReplace())) {
//                data.renameAttributeValue(data.attribute(idAtributeSelected), selectedValue.getValue(), selectedValue.getReplace());
//            }
//        }//System.out.println(data);
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

    public ArrayList<ReplaceRow> getDictionary() {
        return dictionary;
    }

    public void setDictionary(ArrayList<ReplaceRow> dictionary) {
        this.dictionary = dictionary;
    }

}
