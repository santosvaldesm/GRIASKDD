/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managedBeans;

import util.UtilFunctions;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author santos
 */
@ManagedBean(name = "helpMB")
@SessionScoped
public class HelpMB extends UtilFunctions implements Serializable {

    private TreeNode rootTreeHelp;
    private TreeNode selectedNode;

    @PostConstruct
    public void initialize() {
    }

    //// CONSTRUCTOR ///////////////////////////////////////////////////////////
    public HelpMB() {

        //arbol
        TreeNode nodeAux, nodeAux2;
        rootTreeHelp = new DefaultTreeNode(new NodeTreeHelp("Files", "-", "Folder"), null);

        nodeAux = new DefaultTreeNode(new NodeTreeHelp("General information", "-", "Folder"), rootTreeHelp);
        nodeAux.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("About GRIASKDD", "30 KB", "Word Document"), nodeAux));
        nodeAux.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("Main interface", "30 KB", "Word Document"), nodeAux));        
        nodeAux.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("Nodes Connection", "30 KB", "Word Document"), nodeAux));        
        
        nodeAux = new DefaultTreeNode(new NodeTreeHelp("Data source", "-", "Folder"), rootTreeHelp);
        nodeAux.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("Plain Text", "30 KB", "Word Document"), nodeAux));
        nodeAux.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("Conecton DB", "30 KB", "Word Document"), nodeAux));
        
        nodeAux = new DefaultTreeNode(new NodeTreeHelp("Data savers", "-", "Folder"), rootTreeHelp);
        nodeAux.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("Arf Saver", "30 KB", "Word Document"), nodeAux));
        nodeAux.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("Csv Saver", "30 KB", "Word Document"), nodeAux));
        
        nodeAux = new DefaultTreeNode(new NodeTreeHelp("Filters", "-", "Folder"), rootTreeHelp);
        
        nodeAux2 = new DefaultTreeNode(new NodeTreeHelp("Selection", "-", "Folder"), nodeAux);
        nodeAux2.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("Selection", "30 KB", "Word Document"), nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("SelectAttributes", "30 KB", "Word Document"), nodeAux2));
        
        nodeAux2 = new DefaultTreeNode(new NodeTreeHelp("Clean", "-", "Folder"), nodeAux);
        nodeAux2.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("RemoveMissing", "30 KB", "Word Document"), nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("UpdateMissing", "30 KB", "Word Document"), nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("ReplaceValue", "30 KB", "Word Document"), nodeAux2));
        
        nodeAux2 = new DefaultTreeNode(new NodeTreeHelp("Transformation", "-", "Folder"), nodeAux);
        nodeAux2.getChildren().add(new DefaultTreeNode("document", new NodeTreeHelp("Discretize", "30 KB", "Word Document"), nodeAux2));
        
    }

    public void onNodeSelect(NodeSelectEvent event) {
        NodeTreeHelp a = (NodeTreeHelp) event.getTreeNode().getData();
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected", event.getTreeNode().toString() + " - " + a.getSize());
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    ////////////////////////////////////////////////////////////////////////
    /////////////////// FUNCIONES GET Y SET ////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public TreeNode getRootTreeHelp() {
        return rootTreeHelp;
    }

    public void setRootTreeHelp(TreeNode rootTreeHelp) {
        this.rootTreeHelp = rootTreeHelp;
    }

}
