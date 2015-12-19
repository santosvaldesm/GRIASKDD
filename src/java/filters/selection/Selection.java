/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filters.selection;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import managedBeans.GraphicControlMB;
import org.primefaces.context.RequestContext;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.Node;
import util.UtilFunctions;
import util.filters.AttributeRow;
import views.DataAnalisis;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author santos
 */
public class Selection extends UtilFunctions {

    private Node currentNode;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB;//acceso a la clase principal
    private Instances data = null;//datos de instancias actuales
    private Instances initialData = null;//datos de instancia inicial(usado para reiniciar uso de filtros)
    private Remove removeFilter = new Remove();
    private String attributesToRemove = "";
    //---------------------ATTRIBUTES -----------------------------------
    private ArrayList<AttributeRow> listAttributes = new ArrayList<>();
    //private AttributeRow selectedAtrribute = new AttributeRow(null, 0);
    //---------------------CURRENT RELATION -----------------------------
    //private String relationName = "No file loaded.";
    private String relationNumAttributes = "";
    private String relationNumInstances = "";
    private String relationSumWeights = "";

    public void changeForm() {//cambio en formulario
    }

    public Selection(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataSelection.showConfigure}", "Configure", "", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));        
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgSelectionHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
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
        initialData = new Instances(nodeParent.getData());
        if (data == null) {
            data = new Instances(nodeParent.getData());
            loadInstancesData(data);
            currentNode.setStateNode("_v");
            currentNode.repaintGraphic();
            RequestContext.getCurrentInstance().update("IdFormDialogsSelection");
        }
        RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureSelection').show()");
    }

    private void loadInstancesData(Instances in) {
        listAttributes = new ArrayList<>();
        
        relationNumAttributes = String.valueOf(in.numAttributes());
        relationNumInstances = String.valueOf(in.numInstances());
        relationSumWeights = String.valueOf(in.sumOfWeights());
        AttributeRow rowFileData;
        for (int j = 0; j < in.numAttributes(); j++) {
            rowFileData = new AttributeRow(in, j);
            listAttributes.add(rowFileData);
        }
    }

    public void selectAllAttributes() {
        for (AttributeRow attribute : listAttributes) {
            attribute.setSelected(true);
        }
    }

    public void selectNoneAttributes() {
        for (AttributeRow attribute : listAttributes) {
            attribute.setSelected(false);
        }
    }

    public void selectInvertAttributes() {
        for (AttributeRow attribute : listAttributes) {
            if (attribute.isSelected()) {
                attribute.setSelected(false);
            } else {
                attribute.setSelected(true);
            }
        }
    }

    public void restartDataset() {//dejar la instancias como cuando se cargo desde el archivo 
        data = new Instances(initialData);
        loadInstancesData(data);
        RequestContext.getCurrentInstance().update("IdFormDialogsSelection:IdPanelSelection");
    }

    public void removeAttribute() {
        //DETERMINAR SI HAY ATRIBUTOS SELECCIONADOS
        attributesToRemove = "";
        for (AttributeRow attribute : listAttributes) {
            if (attribute.isSelected()) {
                attributesToRemove = attributesToRemove + (attribute.getIdAttribute() + 1) + ",";
            }
        }
        if (attributesToRemove.length() != 0) {
            attributesToRemove = attributesToRemove.substring(0, attributesToRemove.length() - 1);
            RequestContext.getCurrentInstance().execute("PF('wvDlgConfirmRemoveAttribute').show()");
        } else {
            printMessage("Error", "You must select the attributes to be removed", FacesMessage.SEVERITY_ERROR);
        }
    }

    public void confirmRemoveAttribute() {
        try {
            //System.out.println("Atributos a eliminar: " + attributesToRemove);
            currentNode.resetChildrenNodes();
            removeFilter.setAttributeIndices(attributesToRemove);
            removeFilter.setInputFormat(data);
            data = Filter.useFilter(data, removeFilter);
            loadInstancesData(data);
            currentNode.repaintGraphic();
            RequestContext.getCurrentInstance().execute("PF('wvDlgConfirmRemoveAttribute').hide()");
            RequestContext.getCurrentInstance().update("IdFormDialogsSelection:IdPanelSelection");
            
        } catch (Exception ex) {
            Logger.getLogger(DataAnalisis.class.getName()).log(Level.SEVERE, null, ex);
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

    public Instances getData() {
        return data;
    }

    public void setData(Instances data) {
        this.data = data;
    }

    public ArrayList<AttributeRow> getListAttributes() {
        return listAttributes;
    }

    public void setListAttributes(ArrayList<AttributeRow> listAttributes) {
        this.listAttributes = listAttributes;
    }

    public String getRelationNumAttributes() {
        return relationNumAttributes;
    }

    public void setRelationNumAttributes(String relationNumAttributes) {
        this.relationNumAttributes = relationNumAttributes;
    }

    public String getRelationNumInstances() {
        return relationNumInstances;
    }

    public void setRelationNumInstances(String relationNumInstances) {
        this.relationNumInstances = relationNumInstances;
    }

    public String getRelationSumWeights() {
        return relationSumWeights;
    }

    public void setRelationSumWeights(String relationSumWeights) {
        this.relationSumWeights = relationSumWeights;
    }

}
