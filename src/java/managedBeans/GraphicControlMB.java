/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managedBeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import util.Node;
import util.UtilFunctions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.NodeTypeEnum;

/**
 *
 * @author santos
 */
@ManagedBean(name = "graphicControlMB")
@SessionScoped
public class GraphicControlMB extends UtilFunctions implements Serializable {

    private List<Node> nodesArray = new ArrayList<>();
    private List<String> imagesArray = new ArrayList<>();
    private MenuModel defaultMenuModel = new DefaultMenuModel();//menu sin opciones
    private final Node defaultNode = new Node(0, NodeTypeEnum.Default, "0", "0", "_v", this);//si no se ha seleccionado un nodo del grafico se usa este(para garantizar que siempre haya uno nodo seleccionado)
    private Node selectedNode = defaultNode;//nodo seleccionado actualmente
    private MenuModel contextMenuModel;
    private String fileProjectName = "project";
    private TreeNode root;
    private String realPath = "";
    private String log = "";
    public Thread m_RunThread;
    private String updateForThread = "";//lista de identificadores que se actualizaran cuando se complete el hilo
//    PrintStream outPS = null;

    @PostConstruct
    public void initialize() {
        realPath = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath("/");
//        try {            
//            outPS = new PrintStream(new FileOutputStream("output.txt"));
//            System.setOut(outPS);
//        } catch (Exception e) {
//            System.out.println("error:    " + e);
//        }
    }

    @PreDestroy
    public void preDestroyFunction() {
//        if(outPS!=null){
//            String aa=outPS.toString();
//            outPS.close();            
//            removeFile("output.txt");
//            
//        }
    }

    //CANCEL PROCESS RUNING
    @SuppressWarnings("deprecation")
    public void stopProcess() {// DETIENE EL PROCESO ACTUAL SI LO HAY.
        if (m_RunThread != null) {
            m_RunThread.interrupt();
            m_RunThread.stop();// This is deprecated (and theoretically the interrupt should do).
        }
        checkProcessRuning();
    }

    public void checkProcessRuning() {//VERIFICA SI HAY UN PROCESO EJECUTANDOSE(ABRE DIALOGO) SINO REPINTA Y PARA TIMER
        if (m_RunThread != null) {//System.out.println("PORCESO EJECUTANDOSE");
            RequestContext.getCurrentInstance().execute("PF('wvTimerThread').pause(); PF('wvTimerThread').start();");
            RequestContext.getCurrentInstance().execute("PF('wvDlgProcessRun').show()");
        } else {//System.out.println("PORCESO NO EJECUNTANDOSE");
            RequestContext.getCurrentInstance().execute("PF('wvTimerThread').pause()");
            RequestContext.getCurrentInstance().execute("PF('wvDlgProcessRun').hide()");
            if (updateForThread.length() != 0) {
                String[] updateSplit = updateForThread.split(" ");
                for (String up : updateSplit) {
                    RequestContext.getCurrentInstance().update(up);//System.out.println("Repitar: " + up);
                }
            }
            updateForThread = "";
        }
    }

    //// CONSTRUCTOR ///////////////////////////////////////////////////////////
    public GraphicControlMB() {
        loadImages();

        DefaultSubMenu submenu = new DefaultSubMenu("Options");//Menu sin opciones
        submenu.addElement(createDefaultMenuItem("No options", Boolean.TRUE, "", "", "", "", "fa fa-circle-thin"));
        defaultMenuModel.addElement(submenu);

        TreeNode nodeAux, nodeAux2;//arbol
        root = new DefaultTreeNode("Root", null);

        nodeAux = new DefaultTreeNode("Data Source", root);
        nodeAux.getChildren().add(new DefaultTreeNode("PlainText", "Plain Text", nodeAux));
        nodeAux.getChildren().add(new DefaultTreeNode("ConnectionDB", "Connection DB", nodeAux));

        nodeAux = new DefaultTreeNode("Data Saver", root);
        nodeAux.getChildren().add(new DefaultTreeNode("ArfSaver", "Arf Saver", nodeAux));
        nodeAux.getChildren().add(new DefaultTreeNode("CsvSaver", "Csv Saver", nodeAux));

        nodeAux = new DefaultTreeNode("Filters", root);

        nodeAux2 = new DefaultTreeNode("Selection", nodeAux);
        nodeAux2.getChildren().add(new DefaultTreeNode("Selection", "Selection", nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("SelectAttributes", "SelectAttributes", nodeAux2));

        nodeAux2 = new DefaultTreeNode("Clean", nodeAux);
        nodeAux2.getChildren().add(new DefaultTreeNode("RemoveMissing", "Remove Missing", nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("UpdateMissing", "Update Missing", nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("ReplaceValue", "Replace Value", nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("SamplingPercentage", "Sampling Percentage", nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("KNNImputation", "KNN Imputation", nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("Metrics", "Metrics", nodeAux2));

        nodeAux2 = new DefaultTreeNode("Transformation", nodeAux);
        nodeAux2.getChildren().add(new DefaultTreeNode("Discretize", "Discretize", nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("NumericToNominal", "Numeric To Nominal", nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("Codification", "Codification", nodeAux2));
        nodeAux2.getChildren().add(new DefaultTreeNode("NominalToBinary", "NominalToBinary", nodeAux2));

        nodeAux = new DefaultTreeNode("Data Mining", root);
        nodeAux.getChildren().add(new DefaultTreeNode("Association", "Association", nodeAux));
        nodeAux.getChildren().add(new DefaultTreeNode("Classification", "Classification", nodeAux));
        nodeAux.getChildren().add(new DefaultTreeNode("Cluster", "Cluster", nodeAux));

        nodeAux = new DefaultTreeNode("Views", root);
        nodeAux.getChildren().add(new DefaultTreeNode("DataAnalisis", "Data Analisis", nodeAux)); 
    }

    //// ABRIR PROYECTO ////////////////////////////////////////////////////////
    public void handleOpenProject(FileUploadEvent event) {//upload del archivo
        try {
            UploadedFile uploadedFile = event.getFile();
            String line;
            InputStreamReader isr;
            BufferedReader buffer;
            isr = new InputStreamReader(uploadedFile.getInputstream());
            buffer = new BufferedReader(isr);
            if ((line = buffer.readLine()) != null) {//se lee primer linea                       
                convertStringToNodesArray(line);
            }
            RequestContext.getCurrentInstance().execute("PF('wvDlgOpenProject').hide()");
            repaintGraphic();
        } catch (Exception ex) {
            printMessage("Error", "File not recognized " + ex.toString(), FacesMessage.SEVERITY_ERROR);
            System.out.println("Error 20 in " + this.getClass().getName() + ":" + ex.toString());
        }
    }

    //// GUARDAR PROYECTO (DESCARGA DEL PROYECTO EN FORMATO gkdd) //////////////    
    public StreamedContent getFileSaveProject() {
        try {
            try (FileWriter fichero = new FileWriter("project.gkdd")) { //ESCRITURA DEL FICHERO --------------------------------------------                
                PrintWriter pw = new PrintWriter(fichero);
                pw.println(convertNodesArrayToStringFile());
            }
            //DESCARGA DE ARCHIVO ----------------------------
            File file = new File("project.gkdd");
            InputStream input = new FileInputStream(file);
            if (fileProjectName.trim().length() == 0) {
                fileProjectName = "project.gkdd";
            } else {
                fileProjectName = fileProjectName.replace(".gkdd", "");
                fileProjectName = fileProjectName.replace(".", "");
            }
            StreamedContent fileDownloadCsv = new DefaultStreamedContent(input, "application/binary", fileProjectName + ".gkdd");
            return fileDownloadCsv;
        } catch (Exception e) {
            System.out.println("Error 2: " + e.toString());
        }
        return null;
    }

    //// REINICIAR EXPERIMENTO ///////////////////////////////////////////////////
    public void btnNewProjectClick() {
        nodesArray = new ArrayList<>();
        repaintGraphic();
    }

    //// ARREGLO CON IMAGENES A USAR(OBLIGATORIAMENTE PNG) /////////////////////
    private void loadImages() {
        String imageNames = ""
                + "NOVALUE,NumericToNominal,SelectAttributes,ArfSaver,CsvSaver,DataAnalisis,"
                + "DataAnalisis,Association,Classification,Cluster,Codification,NominalToBinary,ConnectionDB,Discretize,"
                + "Generator,HierarchicalTree,Mate,PlainText,"
                + "Prediction,RemoveMissing,ReplaceValue,Selection,TextTree,UpdateMissing,"
                + "WekaTree,SamplingPercentage,KNNImputation,Metrics";
        String[] imageNamesArray = imageNames.split(",");
        for (String imageName : imageNamesArray) {
            imagesArray.add(imageName + "_a");
            imagesArray.add(imageName + "_v");
            imagesArray.add(imageName + "_r");
        }
    }

    //// OBTENER PARAMETROS DE LA VISTA, APLICA SPLIT ';' //////////////////////
    private String[] getRequestParameter(String nameParameter) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            Map<String, String> params = context.getExternalContext().getRequestParameterMap();
            return params.get(nameParameter).split(";");//System.out.println("EL ID CLIQUEADO ES: "+idNodoCliqueado);        
        } catch (Exception e) {
            System.err.println("No se pudo obtener el parametro request " + nameParameter);
            return null;
        }
    }

    //// FUNCION INVOCADA DESDE LA VISTA PARA CREAR NUEVO NODO /////////////////
    public void createNode() {
        String[] newNodeData = getRequestParameter("newNodeData");//System.out.println("Crear nodo " + Arrays.toString(newNodeData));
        newNodeData[0] = newNodeData[0].replace(" ", "");
        if (NodeTypeEnum.convert(newNodeData[0]) != NodeTypeEnum.NOVALUE) {
            int max = 0;
            for (Node n : nodesArray) {
                if (max < n.getId()) {
                    max = n.getId();
                }
            }
            nodesArray.add(new Node(max + 1, NodeTypeEnum.convert(newNodeData[0]), newNodeData[1], newNodeData[2], newNodeData[3], this));
            repaintGraphic();
        }
    }

    //// ELIMINAR CONECCIONES DEL NODO SELECCIONADO ACTUALMENTE ////////////////
    public void disconnectSelectedNode() {
        if (selectedNode != null) {
            selectedNode.reserNode();//SE RESETEA ASI MISMO
            selectedNode.resetChildrenNodes();//RESEA A SUS HIJOS            
            selectedNode.setChildrens(new ArrayList<String>());//NO TIENE HIJOS                      
            selectedNode.setParents(new ArrayList<String>());//NO TIENE PADRES  
            for (Node auxNode : nodesArray) {
                for (int i = 0; i < auxNode.getChildrens().size(); i++) {//eliminamos las conexiones donde el hijo sea el nodo desconectado
                    if (auxNode.getChildrens().get(i).compareTo(String.valueOf(selectedNode.getId())) == 0) {
                        auxNode.getChildrens().remove(i);
                        i--;
                    }
                }
                for (int i = 0; i < auxNode.getParents().size(); i++) {//eliminamos las conexiones donde el padre sea el nodo desconectado
                    if (auxNode.getParents().get(i).compareTo(String.valueOf(selectedNode.getId())) == 0) {
                        auxNode.getParents().remove(i);
                        i--;
                    }
                }
            }
            repaintGraphic();
        }
    }

    //// CONEXION ENTRE DOS NODOS //////////////////////////////////////////////
    public void createConnection() {
        String[] newConnectionData = getRequestParameter("newConnectionData");//System.out.println("Conectar " + newConnectionData[0] + " con " + newConnectionData[1]);
        Node source = findNodeById(Integer.parseInt(newConnectionData[0]));
        Node target = findNodeById(Integer.parseInt(newConnectionData[1]));
        createConnection(source, target);
    }

//    //// CONECTAR NODOS PRIMERO ES ORIGEN Y EL SEGUNDO DESTINO /////////////////
//    private void connectNodes(Node source, Node target) {
//        source.getChildrens().add(String.valueOf(target.getId()));
//        target.getParents().add(String.valueOf(source.getId()));
//    }
    //// CONECTAR HIJO CON PADRE, HIJO NO PUEDE TENER PADRE /////////////////////////////
//    private void connectChildToParent(Node source, Node target) {
//        if (!source.getParents().isEmpty()) {
//            printMessage("Error", "The source node already has a parent", FacesMessage.SEVERITY_ERROR);//EL NODO YA TIENE PADRE
//        } else {//connectNodes(target, source);
//            target.getChildrens().add(String.valueOf(source.getId()));
//            source.getParents().add(String.valueOf(target.getId()));
//        }
//    }
    //// CONECTAR PADRE CON HIJO, HIJO NO PUEDE TENER PADRE /////////////////////////////
    private void connectParentToChild(Node source, Node target) {
        if (!target.getParents().isEmpty()) {
            printMessage("Error", "The node target already has a parent", FacesMessage.SEVERITY_ERROR);//EL NODO YA TIENE PADRE
        } else {//connectNodes(target, source);
            source.getChildrens().add(String.valueOf(target.getId()));
            target.getParents().add(String.valueOf(source.getId()));
        }
    }

    private boolean tipeIsIn(NodeTypeEnum typeNode, String[] typesNodeArray) {
        for (String type : typesNodeArray) {
            if (NodeTypeEnum.convert(type) == typeNode) {
                return true;
            }
        }
        return false;
    }

    //// CONEXION ENTRE DOS NODOS //////////////////////////////////////////////
    private void createConnection(Node source, Node target) {
        String[] nodesChildSuported;//NODOS SOPORTADOS COMO HIJOS
        //String[] nodesParentSuported;//NODOS SOPORTADOS COMO PADRES
        switch (source.getTypeNode()) {
            case ConnectionDB:
            case PlainText:
            case Selection:
            case SelectAttributes:
            case RemoveMissing:
            case UpdateMissing:
            case ReplaceValue:
            case SamplingPercentage:
            case KNNImputation:
            case Metrics:
            case Discretize:
            case NumericToNominal:
            case Codification:
            case NominalToBinary:
            case DataAnalisis:
                nodesChildSuported = new String[]{"ArfSaver", "CsvSaver", "Selection", "SelectAttributes",
                    "RemoveMissing", "UpdateMissing", "ReplaceValue", "SamplingPercentage", "KNNImputation",
                    "Metrics", "Discretize", "NumericToNominal", "Codification", "NominalToBinary", "Association",
                    "Classification", "Cluster", "DataAnalisis"};
                if (tipeIsIn(target.getTypeNode(), nodesChildSuported)) {//NODO FUENTE SERA PADRE DE NODO DESTINO
                    connectParentToChild(source, target);
                } else {//CONEXION NO SOPORTADA
                    printMessage("Error", "Unsupported connection node, refer to help for more information of supported connections between nodes.", FacesMessage.SEVERITY_ERROR);
                }
                break;
            case ArfSaver://NO TIENE HIJOS
            case CsvSaver://NO TIENE HIJOS
            case Association://NO TIENE HIJOS
            case Classification://NO TIENE HIJOS
            case Cluster://NO TIENE HIJOS
                printMessage("Error", "This node can not have children, refer to help for more information of supported connections between nodes.", FacesMessage.SEVERITY_ERROR);
                break;
            case Generator://CREO QUE ESTE NO SE UTILIZARA 
            case HierarchicalTree://NO SE LA DIFERENCIA CON WEKA TREE
            case WekaTree://NO SE USA
            case TextTree://ESTE NO SE UTILIZARA POR QUE EL OUTPUT DEBERIA MOSTRARLO
            case Prediction://HAY QUE AVERIGUAR QUE ES ESTO
                break;
        }
        repaintGraphic();
    }

//// MOVER UN NODO DE UNA POSICION A OTRA //////////////////////////////////
    public void moveNode() {
        String[] moveData = getRequestParameter("moveData");// System.out.println("Mover " + Arrays.toString(newConnectionData));
        for (Node n : nodesArray) {
            if (n.getId() == Integer.parseInt(moveData[0])) {
                n.setX(moveData[1]);
                n.setY(moveData[2]);
            }
        }
        repaintGraphic();
    }

    /// ELIMINACION DE UN NODO /////////////////////////////////////////////////
    public void removeNode() {//funcion llamada desde la vista para eliminar un nodo         
        String[] idRemoveNode = getRequestParameter("removeData");//System.out.println("Nodo eliminado " + idRemoveNode[0]);
        int pos = -1;
        for (int j = 0; j < nodesArray.size(); j++) {
            for (int i = 0; i < nodesArray.get(j).getChildrens().size(); i++) {
                nodesArray.get(j).resetChildrenNodes();
                if (nodesArray.get(j).getChildrens().get(i).compareTo(idRemoveNode[0]) == 0) {//eliminamos las conexiones a este nodo        
                    nodesArray.get(j).getChildrens().remove(i);
                    i--;
                }
            }
            for (int i = 0; i < nodesArray.get(j).getParents().size(); i++) {
                if (nodesArray.get(j).getParents().get(i).compareTo(idRemoveNode[0]) == 0) {//eliminamos las conexiones a este nodo        
                    nodesArray.get(j).getParents().remove(i);
                    i--;
                }
            }
            if (nodesArray.get(j).getId() == Integer.parseInt(idRemoveNode[0])) {
                pos = j;
            }
        }
        if (pos != -1) {//eliminamos nodo
            nodesArray.remove(pos);
        }
        repaintGraphic();
    }

    //// ENCONTRAR NODO SEGUN IDENTIFICADOR ////////////////////////////////////
    public Node findNodeById(int id) {
        for (Object elem : nodesArray) {
            if (((Node) elem).getId() == id) {
                return (Node) elem;
            }
        }
        return null;
    }

    //// ARMAR EL MENU CONTEXTUAL (CLICK DERECHO SOBRE AREA GRAFICA) ///////////
    public void reloadContextMenu() {
        contextMenuModel = new DefaultMenuModel();
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String idNodoCliqueado = params.get("idNodo");//System.out.println("EL ID CLIQUEADO ES: "+idNodoCliqueado);        
        selectedNode = findNodeById(Integer.parseInt(idNodoCliqueado));
        if (selectedNode != null) {
            contextMenuModel = selectedNode.getMenuModel();
        } else {
            selectedNode = defaultNode;
            contextMenuModel = defaultMenuModel;
        }
    }

    //// ENVIAR LISTA DE NODOS A LA PAGINA Y REPINTAR TODO /////////////////////
    public void repaintGraphic() {
        RequestContext.getCurrentInstance().execute("convertTxtToNodes('" + convertNodesArrayToString() + "')");
        RequestContext.getCurrentInstance().execute("repintarAreaDeTrabajo()");
        RequestContext.getCurrentInstance().update("IdPanelCanvasGrafico");//System.out.println("Repintado");
    }

    //// CONVIERTE LISTA NODOS EN STRING Y SE ENVIA A LA VISTA /////////////////
    private String convertNodesArrayToString() {
        String strReturn = "";
        if (nodesArray != null && !nodesArray.isEmpty()) {
            for (Node nodo : nodesArray) {
                strReturn = strReturn + nodo.getId() + ";" + nodo.getTypeNode() + ";" + nodo.getX() + ";" + nodo.getY() + ";" + nodo.getStateNode() + "(";
                for (String conexion : nodo.getChildrens()) {
                    strReturn = strReturn + conexion + ";";
                }
                strReturn = strReturn.substring(0, strReturn.length() - 1);
                strReturn = strReturn + "}";
            }
            strReturn = strReturn.substring(0, strReturn.length() - 1);
        }
        return strReturn;
    }

    //// CONVIERTE LISTA NODOS EN STRING (USADO PARA GUARDAR PROYECTO) /////////
    private String convertNodesArrayToStringFile() {//se diferencia del anterio por que todos los nodos quedan en rojo
        String strReturn = "";
        if (nodesArray != null && !nodesArray.isEmpty()) {
            for (Node nodo : nodesArray) {
                strReturn = strReturn + nodo.getId() + ";" + nodo.getTypeNode() + ";" + nodo.getX() + ";" + nodo.getY() + ";_r(";
                for (String conexion : nodo.getChildrens()) {
                    strReturn = strReturn + conexion + ";";
                }
                strReturn = strReturn.substring(0, strReturn.length() - 1);
                strReturn = strReturn + "}";
            }
            strReturn = strReturn.substring(0, strReturn.length() - 1);
        }
        return strReturn;
    }

    //// CONVIERTE UN STRIN EN LA LISTA NODOS /////////////////////////////////
    private void convertStringToNodesArray(String line) {
        nodesArray = new ArrayList<>();
        String[] splitNodo;
        String[] splitTexto;
        if (line.length() != 0) {//si la cadena tiene longitud es por que si vienen nodos
            String[] splitTextoNodos = line.split("}");//contiene todos los nodos        
            for (int i = 0; i < splitTextoNodos.length; i = i + 1) {
                splitNodo = splitTextoNodos[i].split("\\(");//DIVIDE EL TEXTO EN DATOS NODO(splitNodo[0]) Y CONEXIONES(splitNodo[1])                
                splitTexto = splitNodo[0].split(";");//INGRESO LOS NODOS
                nodesArray.add(new Node(Integer.parseInt(splitTexto[0]), NodeTypeEnum.convert(splitTexto[1]), splitTexto[2], splitTexto[3], splitTexto[4], this));
            }
            for (int i = 0; i < splitTextoNodos.length; i = i + 1) {
                splitNodo = splitTextoNodos[i].split("\\(");//DIVIDE EL TEXTO EN DATOS NODO(splitNodo[0]) Y CONEXIONES(splitNodo[1])                                
                if (splitNodo.length > 1) {//INGRESO LAS CONEXIONES
                    splitTexto = splitNodo[1].split(";");
                    for (int j = 0; j < splitTexto.length; j = j + 1) {
                        Node source = findNodeById(nodesArray.get(i).getId());
                        Node target = findNodeById(Integer.parseInt(splitTexto[j]));
                        createConnection(source, target);
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    /////////////////// FUNCIONES GET Y SET ////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    public List<Node> getNodesArray() {
        return nodesArray;
    }

    public void setNodesArray(List<Node> nodesArray) {
        this.nodesArray = nodesArray;
    }

    public List<String> getImagesArray() {
        return imagesArray;
    }

    public void setImagesArray(List<String> imagesArray) {
        this.imagesArray = imagesArray;
    }

    public Node getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(Node selectedNode) {
        this.selectedNode = selectedNode;
    }

    public MenuModel getContextMenuModel() {
        return contextMenuModel;
    }

    public void setContextMenuModel(MenuModel contextMenuModel) {
        this.contextMenuModel = contextMenuModel;
    }

    public MenuModel getDefaultMenuModel() {
        return defaultMenuModel;
    }

    public void setDefaultMenuModel(MenuModel defaultMenuModel) {
        this.defaultMenuModel = defaultMenuModel;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public String getFileProjectName() {
        return fileProjectName;
    }

    public void setFileProjectName(String fileProjectName) {
        this.fileProjectName = fileProjectName;
    }

    public String getRealPath() {
        return realPath;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getUpdateForThread() {
        return updateForThread;
    }

    public void setUpdateForThread(String updateForThread) {
        this.updateForThread = updateForThread;
    }

}
