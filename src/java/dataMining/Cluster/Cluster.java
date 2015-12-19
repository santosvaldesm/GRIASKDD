/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataMining.Cluster;

import dataMining.association.Result;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import managedBeans.GraphicControlMB;
import static org.apache.jasper.Constants.DEFAULT_BUFFER_SIZE;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.ClustererEnum;
import util.DinamicTable;
import util.Node;
import util.UtilFunctions;
import weka.classifiers.Evaluation;
import weka.clusterers.Canopy;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.Cobweb;
import weka.clusterers.EM;
import weka.clusterers.FarthestFirst;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Drawable;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.CostMatrixEditor;
import weka.gui.Logger;
import weka.gui.ResultHistoryPanel;
import weka.gui.SysErrLog;
import weka.gui.TaskLogger;
import weka.gui.explorer.ClustererAssignmentsPlotInstances;
import weka.gui.explorer.ExplorerDefaults;

/**
 *
 * @author santos
 */
public class Cluster extends UtilFunctions {

    private Node currentNode = null;
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private GraphicControlMB graphicControlMB = null;//acceso a la clase principal
    private Instances data = null;
    private Instances initialData = null;
    private DinamicTable dinamicTable = new DinamicTable();

    private List<SelectItem> listAttributes = new ArrayList<>();
    private int classIndex = 0;
    private String txtOutput = "";
    private String fileName = "download";
    private StreamedContent fileDownloadTxt;
    private boolean disabledVisualizeTree = false;

    private List<SelectItem> listClassifiers = new ArrayList<>();
    private String selectedClassifier = "";

    //TEST OPTIONS
    private String testOption = "Use training set";//(Use full trainin set, Cross validation)      
    private boolean disabledClassToClusterEvalualtion = true;//cuando se deshabilita el radio buton
    private boolean disabledPercentageSplit = true;//cuando se deshabilitan los controles de folds y seed
    private double percentageSplit = 66;
    //RESULT LIST
    private List<Result> resultList = new ArrayList<>();//listado de resultados de procesos
    private String selectedResult;
    private int numberResults = 0;//numero de resultados generados
    private List<String> ignoreAttribulesList = new ArrayList<>();

    //CONFIGURATION Canopy---------------------------------------------------------
    private Canopy canopyObj = null;
    private boolean debugCa = false;
    private boolean doNotCheckCapabilitiesCa = false;
    private boolean dontReplaceMissingValuesCa = false;
    private int maxNumCandidateCanopiesToHoldInMemoryCa = 100;
    private double minimumCanopyDensityCa = 2.0;
    private int numClustersCa = -1;
    private int periodicPruningRateCa = 10000;
    private int seddCa = -1;
    private double t1Ca = -1.25;
    private double t2Ca = -1.0;
    //CONFIGURATION Cobweb ------------------------------------------------
    private Cobweb cobwebObj = null;
    private boolean debugCo = false;
    private boolean doNotCheckCapabilitiesCo = false;
    private boolean saveInstancesDataCo = false;
    private double acuityCo = 1.0;
    private double cutoffCo = 0.0028209479177387815;
    private int seedCo = 42;
    //CONFIGURATION EM -----------------------------------------------------
    private EM EMObj = null;
    private boolean debugEM = false;
    private boolean displayModelInOldFormatEM = false;
    private boolean doNotCheckCapabilitiesEM = false;
    private int maxIterationsEM = 100;
    private int maximumNumberOfClustersEM = -1;
    private double minLogLikelihoodImprovementCVEM = 1.0E-6;
    private double minLogLikelihoodImprovementIteratingEM = 1.0E-6;
    private double minStdDevEM = 1.0E-6;
    private int numClustersEM = -1;
    private int numExecutionSlotsEM = 1;
    private int numFoldsEM = 10;
    private int seedEM = 100;
    //CONFIGURATION FarthestFirst ----------------------------------------------
    private FarthestFirst farthestFirstObj = null;
    private boolean debugFF = false;
    private boolean doNotCheckCapabilitiesFF = false;
    private int numClustersFF = -1;
    private int seedFF = 100;
    //CONFIGURATION HierarchicalClusterer --------------------------------------
    private HierarchicalClusterer hierarchicalClustererObj = null;
    private boolean debugHC = false;
    private boolean doNotCheckCapabilitiesHC = false;
    private boolean distanceIsBranchLengthHC = false;
    private String distanceFunctionHC = "";
    private String linkTypeHC = "";
    private int numClustersHC = 2;
    private boolean printNewickHC = true;
    //CONFIGURATION SimpleKMeans ----------------------------------
    private SimpleKMeans simpleKMeansObj = null;
    private int canopyMaxCanopiesToHoldInMemorySKM = 100;
    private double canopyMinimumCanopyDensitySKM = 2.0;
    private int canopyPeriodicPruningRateSKM = 10000;
    private double canopyT1SKM = -1.25;
    private double canopyT2SKM = -1.0;
    private boolean debugSKM = false;
    private boolean displayStdDevsSKM = false;
    private String distanceFunctionSKM = "";
    private boolean doNotCheckCapabilitiesSKM = false;
    private boolean dontReplaceMissingValuesSKM = false;
    private boolean fastDistanceCalcSKM = false;
    private String initializationMethodSKM = "";
    private int maxIterationsSKM = 500;
    private int numClustersSKM = 2;
    private int numExecutionSlotsSKM = 1;
    private boolean preserveInstancesOrderSKM = false;
    private boolean reduceNumberOfDistanceCalcsViaCanopiesSKM = true;
    private int seedSKM = 10;
    private String attributeIndicesEuclideanDistance = "first-last";
    private boolean dontNormalizeEuclideanDistance = false;
    private boolean invertSelectionEuclideanDistance = false;
    private String attributeIndicesManhattanDistance = "first-last";
    private boolean dontNormalizeManhattanDistance = false;
    private boolean invertSelectionManhattanDistance = false;
    private String attributeIndicesChebyshevDistance = "first-last";
    private boolean dontNormalizeChebyshevDistance = false;
    private boolean invertSelectionChebyshevDistance = false;
    private String attributeIndicesMinkowskiDistance = "first-last";
    private boolean dontNormalizeMinkowskiDistance = false;
    private boolean invertSelectionMinkowskiDistance = false;
    private double orderMinkowskiDistance = 2.0;

    public void changeForm() {//hay un cambio en el formulario(esto para que envie los datos al nodo correspondiente)
    }

    public void removeProcess() {
        if (selectedResult != null && selectedResult.length() != 0) {
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).getId().compareTo(selectedResult) == 0) {//SE PROCEDE A ELIMINAR EL RESULTADO
                    if (resultList.size() == 1) {//SOLO HAY UNO(LA LISTA DE RESULTADOS QUEDARA VACIA)
                        selectedResult = "";
                    } else if (i == 0) {//ES EL PRIMER RESULTADO, SE ASIGNA A selectResult EL SIGUIENTE RESULTADO
                        selectedResult = resultList.get(i + 1).getId();
                    } else {//SE ASIGNARA A selectResult EL ANTERIOR RESULTADO
                        selectedResult = resultList.get(i - 1).getId();
                    }
                    resultList.remove(i);
                    changeResultList();
                    break;
                }
            }
        }
    }

    public void changeResultList() {//CAMBIA LISTA DE RESULTADOS        
        txtOutput = "";
        grph = "";
        disabledVisualizeTree = true;
        for (Result r : resultList) {
            if (r.getId().compareTo(selectedResult) == 0) {
                txtOutput = r.getTxtResult();
                grph = r.getTreeGraph();
                disabledVisualizeTree = (grph == null || grph.length() == 0); //NO SE PUEDE GENERAR EL GRAFICO
                break;
            }
        }
    }

    public Cluster(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public void changeTestMode() {
        if (testOption.compareTo("Percentage split") == 0) {
            disabledPercentageSplit = false;
            disabledClassToClusterEvalualtion = true;
        } else if (testOption.compareTo("Use training set") == 0) {
            disabledPercentageSplit = true;
            disabledClassToClusterEvalualtion = true;
        } else if (testOption.compareTo("Classes to clusters evaluation") == 0) {
            disabledPercentageSplit = true;
            disabledClassToClusterEvalualtion = false;
        } else if (testOption.compareTo("m_TestSplitBut") == 0) {
            disabledPercentageSplit = true;
            disabledClassToClusterEvalualtion = true;
        } else {
            printError(new Exception("Unknown test mode"), this);
        }
    }

    public final void reset() {
        listClassifiers = new ArrayList<>();
        listClassifiers.add(new SelectItem("SimpleKMeans", "SimpleKMeans"));
        listClassifiers.add(new SelectItem("Canopy", "Canopy"));
        listClassifiers.add(new SelectItem("Cobweb", "Cobweb"));
        listClassifiers.add(new SelectItem("EM", "EM"));
        listClassifiers.add(new SelectItem("FarthesFirst", "FarthesFirst"));
        listClassifiers.add(new SelectItem("HierarchicalClusterer", "HierarchicalClusterer"));
        selectedClassifier = "SimpleKMeans";

        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataCluster.showConfigure}", "Configure", "", "fa fa-cogs"));
        //submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewCluster').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsCluster", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgClusterHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void clickConfigureClusterer() {
        switch (ClustererEnum.convert(selectedClassifier)) {
            case Canopy:
                RequestContext.getCurrentInstance().execute("PF('wvCanopy').show()");
                break;
            case Cobweb:
                RequestContext.getCurrentInstance().execute("PF('wvCobweb').show()");
                break;
            case EM:
                RequestContext.getCurrentInstance().execute("PF('wvEM').show()");
                break;
            case FarthesFirst:
                RequestContext.getCurrentInstance().execute("PF('wvFarthesFirst').show()");
                break;
            case HierarchicalClusterer:
                RequestContext.getCurrentInstance().execute("PF('wvHierarchicalClusterer').show()");
                break;
            case SimpleKMeans:
                RequestContext.getCurrentInstance().execute("PF('wvSimpleKMeans').show()");
                break;
            case NOVALUE:
                break;
        }
    }

    public void clickBtnDistanceFunctionHC() {
        switch (distanceFunctionHC) {
            case "0"://ChebyshevDistance
                RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureChebyshevDistance').show()");
                break;
            case "1"://EuclideanDistance
                RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureEuclideanDistance').show()");
                break;
            case "2"://ManhattanDistance
                RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureManhattanDistance').show()");
                break;
            case "3"://MinkowskiDistance
                RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureMinkowskiDistance').show()");
                break;
        }
    }

    public void clickBtnDistanceFunctionSKM() {
        switch (distanceFunctionSKM) {
            case "1"://EuclideanDistance
                RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureEuclideanDistance').show()");
                break;
            case "2"://ManhattanDistance
                RequestContext.getCurrentInstance().execute("PF('wvDlgConfigureManhattanDistance').show()");
                break;
        }
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
        if (data == null) {
            data = new Instances(nodeParent.getData());
        }
        if (currentNode.getStateNode().compareTo("_r") == 0) {
            data = new Instances(nodeParent.getData());
            initialData = new Instances(nodeParent.getData());
            listAttributes = new ArrayList<>();
            classIndex = -1;
            for (int i = 0; i < data.numAttributes(); i++) {
                listAttributes.add(new SelectItem(data.attribute(i).index(), data.attribute(i).name()));
            }
        }
        RequestContext.getCurrentInstance().update("IdFormDialogsCluster");
        RequestContext.getCurrentInstance().execute("PF('wvDlgCluster').show()");
    }

    private final Logger m_Log = new SysErrLog();

    protected JTextArea m_OutText = new JTextArea(20, 40);//The output area for classification results.
    protected ResultHistoryPanel m_History = new ResultHistoryPanel(m_OutText);//A panel controlling results viewing.
    protected CostMatrixEditor m_CostMatrixEditor = new CostMatrixEditor();//The cost matrix editor for evaluation costs.
    protected List<String> m_selectedEvalMetrics = Evaluation.getAllEvaluationMetricNames();
    protected String m_RandomSeedText = "1";
    protected JCheckBox m_PreserveOrderBut = new JCheckBox("Preserve order for % Split");// Whether randomization is turned off to preserve order.
    String grph = null;

    public Clusterer createAndConfigureClusterer() {
        switch (ClustererEnum.convert(selectedClassifier)) {
            case Canopy:
                canopyObj = new Canopy();
                return canopyObj;
            case Cobweb:
                cobwebObj = new Cobweb();
                return cobwebObj;
            case EM:
                EMObj = new EM();
                return EMObj;
            case FarthesFirst:
                farthestFirstObj = new FarthestFirst();
                return farthestFirstObj;
            case HierarchicalClusterer:
                hierarchicalClustererObj = new HierarchicalClusterer();
                return hierarchicalClustererObj;
            case SimpleKMeans:
                simpleKMeansObj = new SimpleKMeans();
                return simpleKMeansObj;
            case NOVALUE:
                return null;
        }
        return null;
    }

    //protected Thread m_RunThread;
    
    //final RequestContext requestContext = RequestContext.getCurrentInstance();

    public void runProcess() {//CLICK EN BOTON "RUN PROCESS"
        if (graphicControlMB.m_RunThread == null) {//SI NO HAY UN PROCESO EJECUTANDOSE SE DA INICIO AL PROCESO
            graphicControlMB.m_RunThread = new Thread() {
                @Override
                public void run() {
                    txtOutput = "";
                    try {
                        long trainTimeStart, trainTimeElapsed;// for timing                        
                        m_Log.statusMessage("Setting up...");// Copy the current state of things
                        Instances inst = new Instances(initialData);
                        inst.setClassIndex(-1);
                        Instances userTest = null;
                        ClustererAssignmentsPlotInstances plotInstances = ExplorerDefaults.getClustererAssignmentsPlotInstances();
                        plotInstances.setClusterer(createAndConfigureClusterer());
                        grph = null;
                        int[] ignoredAtts = null;
                        int testMode = 0;
                        Clusterer clusterer = createAndConfigureClusterer();
                        Clusterer fullClusterer = null;
                        StringBuffer outBuff = new StringBuffer();
                        String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
                        String cname = clusterer.getClass().getName();
                        if (cname.startsWith("weka.clusterers.")) {
                            name += cname.substring("weka.clusterers.".length());
                        } else {
                            name += cname;
                        }
                        String cmd = clusterer.getClass().getName();// m_ClustererEditor.getValue().getClass().getName();
                        if (clusterer instanceof OptionHandler) {//m_ClustererEditor.getValue() instanceof OptionHandler) {
                            cmd += " " + Utils.joinOptions(((OptionHandler) clusterer).getOptions());//m_ClustererEditor.getValue()).getOptions());
                        }
                        try {
                            m_Log.logMessage("Started " + cname);
                            m_Log.logMessage("Command: " + cmd);
                            if (m_Log instanceof TaskLogger) {
                                ((TaskLogger) m_Log).taskStarted();
                            }
                            if (testOption.compareTo("Percentage split") == 0) {//m_PercentBut.isSelected()) {
                                testMode = 2;//percentageSplit = Integer.parseInt(m_PercentText.getText());
                                if ((percentageSplit <= 0) || (percentageSplit >= 100)) {
                                    throw new Exception("Percentage must be between 0 and 100");
                                }
                            } else if (testOption.compareTo("Use training set") == 0) {//m_TrainBut.isSelected()) {
                                testMode = 3;
                            } else if (testOption.compareTo("m_TestSplitBut") == 0) {//m_TestSplitBut.isSelected()) {
                                testMode = 4;//SUPLIED TEST SET NO TRABAJARE POR EL MOMENTO(POR ESO SE ELIMINA CODIGO weka.gui.explorer.ClassifierPanel.)                                                
                                if (userTest == null) {// Check the test instance compatibility
                                    throw new Exception("No user test set has been opened");
                                }
                                if (!inst.equalHeaders(userTest)) {
                                    throw new Exception("Train and test set are not compatible\n" + inst.equalHeadersMsg(userTest));
                                }
                            } else if (testOption.compareTo("Classes to clusters evaluation") == 0) {//m_ClassesToClustersBut.isSelected()) {
                                testMode = 5;
                            } else {
                                throw new Exception("Unknown test mode");
                            }

                            Instances trainInst = new Instances(inst);
                            if (testOption.compareTo("Classes to clusters evaluation") == 0) {//m_ClassesToClustersBut.isSelected()) {
                                trainInst.setClassIndex(classIndex);//m_ClassCombo.getSelectedIndex());
                                inst.setClassIndex(classIndex);//m_ClassCombo.getSelectedIndex());
                                if (inst.classAttribute().isNumeric()) {
                                    throw new Exception("Class must be nominal for class based " + "evaluation!");
                                }
                            }
                            if (!ignoreAttribulesList.isEmpty()) {//m_ignoreKeyList.isSelectionEmpty()) {
                                trainInst = removeIgnoreCols(trainInst);
                            }
                            outBuff.append("=== Run information ===\n\n");// Output some header information
                            outBuff.append("Scheme:       ").append(cname);
                            if (clusterer instanceof OptionHandler) {
                                String[] o = ((OptionHandler) clusterer).getOptions();
                                outBuff.append(" ").append(Utils.joinOptions(o));
                            }
                            outBuff.append("\n");
                            outBuff.append("Relation:     ").append(inst.relationName()).append('\n');
                            outBuff.append("Instances:    ").append(inst.numInstances()).append('\n');
                            outBuff.append("Attributes:   ").append(inst.numAttributes()).append('\n');
                            if (inst.numAttributes() < 100) {
                                boolean[] selected = new boolean[inst.numAttributes()];
                                for (int i = 0; i < inst.numAttributes(); i++) {
                                    selected[i] = true;
                                }
                                if (!ignoreAttribulesList.isEmpty()) { //m_ignoreKeyList.isSelectionEmpty()) {
                                    for (String attriId : ignoreAttribulesList) {//for (int i = 0; i < indices.length; i++) {
                                        selected[Integer.valueOf(attriId)] = false;                //selected[indices[i]]=false;
                                    }                                                           //
                                }
                                if (testOption.compareTo("Classes to clusters evaluation") == 0) {//m_ClassesToClustersBut.isSelected()) {
                                    selected[classIndex] = false;//m_ClassCombo.getSelectedIndex()] = false;
                                }
                                for (int i = 0; i < inst.numAttributes(); i++) {
                                    if (selected[i]) {
                                        outBuff.append("              ").append(inst.attribute(i).name()).append('\n');
                                    }
                                }
                                if (!ignoreAttribulesList.isEmpty() || testOption.compareTo("Classes to clusters evaluation") == 0) {
                                    outBuff.append("Ignored:\n");
                                    for (int i = 0; i < inst.numAttributes(); i++) {
                                        if (!selected[i]) {
                                            outBuff.append("              ").append(inst.attribute(i).name()).append('\n');
                                        }
                                    }
                                }
                            } else {
                                outBuff.append("              [list of attributes omitted]\n");
                            }
                            if (!ignoreAttribulesList.isEmpty()) {//m_ignoreKeyList.isSelectionEmpty()) {
                                ignoredAtts = new int[ignoreAttribulesList.size()];//m_ignoreKeyList.getSelectedIndices();
                                for (int i = 0; i < ignoreAttribulesList.size(); i++) {
                                    ignoredAtts[i] = Integer.parseInt(ignoreAttribulesList.get(i));
                                }
                            }
                            if (testOption.compareTo("Classes to clusters evaluation") == 0) {//m_ClassesToClustersBut.isSelected()) {                                
                                if (ignoredAtts == null) {// add class to ignored list
                                    ignoredAtts = new int[1];
                                    ignoredAtts[0] = classIndex;//m_ClassCombo.getSelectedIndex();
                                } else {
                                    int[] newIgnoredAtts = new int[ignoredAtts.length + 1];
                                    System.arraycopy(ignoredAtts, 0, newIgnoredAtts, 0, ignoredAtts.length);
                                    newIgnoredAtts[ignoredAtts.length] = classIndex;//m_ClassCombo.getSelectedIndex();
                                    ignoredAtts = newIgnoredAtts;
                                }
                            }
                            outBuff.append("Test mode:    ");
                            switch (testMode) {
                                case 3: // Test on training
                                    outBuff.append("evaluate on training data\n");
                                    break;
                                case 2: // Percent split
                                    outBuff.append("split ").append(percentageSplit).append("% train, remainder test\n");
                                    break;
                                case 4: // Test on user split
                                    outBuff.append("user supplied test set: ").append(userTest.numInstances()).append(" instances\n");
                                    break;
                                case 5: // Classes to clusters evaluation on training
                                    outBuff.append("Classes to clusters evaluation on training data");
                                    break;
                            }
                            outBuff.append("\n");
                            m_History.addResult(name, outBuff);
                            m_History.setSingle(name);
                            // Build the model and output it.
                            m_Log.statusMessage("Building model on training data...");
                            // remove the class attribute (if set) and build the clusterer
                            trainTimeStart = System.currentTimeMillis();
                            clusterer.buildClusterer(removeClass(trainInst));
                            trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
                            // if (testMode == 2) {
                            outBuff.append("\n=== Clustering model (full training set) ===\n\n");
                            outBuff.append(clusterer.toString()).append('\n');
                            outBuff.append("\nTime taken to build model (full training data) : ").append(Utils.doubleToString(trainTimeElapsed / 1000.0, 2)).append(" seconds\n\n");
                            // }
                            m_History.updateResult(name);
                            if (clusterer instanceof Drawable && cmd.contains("Cobweb")) {
                                try {
                                    grph = ((Drawable) clusterer).graph();
                                } catch (Exception ex) {
                                }
                            }
                            // copy full model for output
                            SerializedObject so = new SerializedObject(clusterer);
                            fullClusterer = (Clusterer) so.getObject();
                            ClusterEvaluation eval = new ClusterEvaluation();
                            eval.setClusterer(clusterer);
                            switch (testMode) {
                                case 3:
                                case 5: // Test on training
                                    m_Log.statusMessage("Clustering training data...");
                                    eval.evaluateClusterer(trainInst, "", false);
                                    plotInstances.setInstances(inst);
                                    plotInstances.setClusterEvaluation(eval);
                                    outBuff.append("=== Model and evaluation on training set ===\n\n");
                                    break;
                                case 2: // Percent split
                                    m_Log.statusMessage("Randomizing instances...");
                                    inst.randomize(new Random(1));
                                    trainInst.randomize(new Random(1));
                                    int trainSize = (int) (trainInst.numInstances() * percentageSplit / 100);
                                    int testSize = trainInst.numInstances() - trainSize;
                                    Instances train = new Instances(trainInst, 0, trainSize);
                                    Instances test = new Instances(trainInst, trainSize, testSize);
                                    Instances testVis = new Instances(inst, trainSize, testSize);
                                    m_Log.statusMessage("Building model on training split...");
                                    trainTimeStart = System.currentTimeMillis();
                                    clusterer.buildClusterer(train);
                                    trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
                                    m_Log.statusMessage("Evaluating on test split...");
                                    eval.evaluateClusterer(test, "", false);
                                    plotInstances.setInstances(testVis);
                                    plotInstances.setClusterEvaluation(eval);
                                    outBuff.append("=== Model and evaluation on test split ===\n");
                                    outBuff.append(clusterer.toString()).append("\n");
                                    outBuff.append("\nTime taken to build model (percentage split) : ").append(Utils.doubleToString(trainTimeElapsed / 1000.0, 2)).append(" seconds\n\n");
                                    break;
                                case 4: // Test on user split
                                    m_Log.statusMessage("Evaluating on test data...");
                                    Instances userTestT = new Instances(userTest);
                                    if (!ignoreAttribulesList.isEmpty()) {//m_ignoreKeyList.isSelectionEmpty()) {
                                        userTestT = removeIgnoreCols(userTestT);
                                    }
                                    eval.evaluateClusterer(userTestT, "", false);
                                    plotInstances.setInstances(userTest);
                                    plotInstances.setClusterEvaluation(eval);
                                    outBuff.append("=== Evaluation on test set ===\n");
                                    break;
                                default:
                                    throw new Exception("Test mode not implemented");
                            }
                            outBuff.append(eval.clusterResultsToString());
                            outBuff.append("\n");
                            m_History.updateResult(name);
                            m_Log.logMessage("Finished " + cname);
                            m_Log.statusMessage("OK");
                        } catch (Exception ex) {//ex.printStackTrace();
                            m_Log.logMessage(ex.getMessage());
                            printMessage("Error Evaluate clusterer", "Problem evaluating clusterer:\n" + ex.getMessage(), FacesMessage.SEVERITY_ERROR);
                            m_Log.statusMessage("Problem evaluating clusterer");
                        } finally {
                            if (isInterrupted()) {
                                m_Log.logMessage("Interrupted " + cname);
                                m_Log.statusMessage("See error log");
                                txtOutput = "Process Interrupted " + cname;
                            } else {//SI NO FUE INTERRUMPIDO ES POR QUE FINALIZO CORRECTAMENTE
                                txtOutput = outBuff.toString();
                            }
                            graphicControlMB.m_RunThread = null;
                            
                            //m_StartBut.setEnabled(true);//AQUI ACTIVAR CONTROLES
                        }
                    } catch (Exception ex) {
                        txtOutput = ex.toString();
                        graphicControlMB.m_RunThread = null;
                    }
                    resultList.add(new Result(String.valueOf(++numberResults),//IDENTIFICADOR
                            (new SimpleDateFormat("HH:mm:ss - ")).format(new Date()) + " " + selectedClassifier,//NOMBRE
                            txtOutput, grph));//TEXTO Y ARBOL
                    selectedResult = String.valueOf(numberResults);
                    changeResultList();
                    currentNode.setStateNode("_v");                                        
                }
            };            
            graphicControlMB.setUpdateForThread(""
                    + "IdFormDialogsCluster:IdClusterOutput "
                    + "IdFormDialogsCluster:IdResultListClasification "
                    + "IdFormDialogsCluster:IdBtnViewTree");
            graphicControlMB.checkProcessRuning();
            graphicControlMB.m_RunThread.setPriority(Thread.MIN_PRIORITY);
            graphicControlMB.m_RunThread.start();
        } else {//SE ESTA EJECUTANDO UN ROCESO
            printMessage("Error", "Currently running process", FacesMessage.SEVERITY_ERROR);
        }
    }

    private Instances removeClass(Instances inst) {
        Remove af = new Remove();
        Instances retI = null;

        try {
            if (inst.classIndex() < 0) {
                retI = inst;
            } else {
                af.setAttributeIndices("" + (inst.classIndex() + 1));
                af.setInvertSelection(false);
                af.setInputFormat(inst);
                retI = Filter.useFilter(inst, af);
            }
        } catch (Exception e) {
            printError(e, this);//e.printStackTrace();
        }
        return retI;
    }

    private Instances removeIgnoreCols(Instances inst) {

        // If the user is doing classes to clusters evaluation and
        // they have opted to ignore the class, then unselect the class in
        // the ignore list
        if (testOption.compareTo("Classes to clusters evaluation") == 0) {//m_ClassesToClustersBut.isSelected()) {
            //int classIndex = m_ClassCombo.getSelectedIndex();//clas index ya esta definido            
            for (int i = 0; i < ignoreAttribulesList.size(); i++) {
                if (Integer.parseInt(ignoreAttribulesList.get(i)) == classIndex) {//if (m_ignoreKeyList.isSelectedIndex(classIndex)) {
                    ignoreAttribulesList.remove(i);         //m_ignoreKeyList.removeSelectionInterval(classIndex, classIndex);
                    break;                                  //}
                }
            }
        }
        int[] selected = new int[ignoreAttribulesList.size()];//int[] selected = m_ignoreKeyList.getSelectedIndices();
        for (int i = 0; i < ignoreAttribulesList.size(); i++) {
            selected[i] = Integer.parseInt(ignoreAttribulesList.get(i));
        }
        Remove af = new Remove();
        Instances retI = null;
        try {
            af.setAttributeIndicesArray(selected);
            af.setInvertSelection(false);
            af.setInputFormat(inst);
            retI = Filter.useFilter(inst, af);
        } catch (Exception e) {
            printError(e, this);//e.printStackTrace();
        }
        return retI;
    }

    public StreamedContent getFileDownloadTxt() {
        try {
            try (FileWriter fichero = new FileWriter("download.txt")) { //CREACION DE ARCHIVO ----------------------------
                PrintWriter pw = new PrintWriter(fichero);
                for (String line : txtOutput.split(Character.toString((char) 10))) {
                    pw.println(line);
                }
            }
            InputStream input;//DESCARGA DE ARCHIVO ----------------------------
            File file = new File("download.txt");
            input = new FileInputStream(file);
            if (fileName.trim().length() == 0) {
                fileName = "download.txt";
            } else {
                fileName = fileName.replace(".txt", "");
                fileName = fileName.replace(".", "");
            }
            fileDownloadTxt = new DefaultStreamedContent(input, "application/binary", fileName + ".txt");
            return fileDownloadTxt;
        } catch (IOException ex) {
            printError(ex, this);
        }
        return null;
    }

    public void setFileDownloadTxt(StreamedContent fileDownloadTxt) {
        this.fileDownloadTxt = fileDownloadTxt;
    }

    public void generarPdf() throws IOException {//genera un pdf de una historia seleccionada en el historial         
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        try {
            removeFile(graphicControlMB.getRealPath() + "tree.pdf");                //ELIMINAR pdf
            removeFile(graphicControlMB.getRealPath() + "dotFile.dot");             //ELIMINAR dot
            createFile(graphicControlMB.getRealPath() + "dotFile.dot", grph);       //CREAR ARCHIVO dot            
            new ProcessBuilder(
                    "D:\\bin\\dot.exe", "-Tpdf", graphicControlMB.getRealPath() + "dotFile.dot",
                    "-o", graphicControlMB.getRealPath() + "tree.pdf").start().waitFor();           //CREAR ARCHIVO pdf

            File file = new File(graphicControlMB.getRealPath(), "tree.pdf");       // Open file.            
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
            // Init servlet response.
            response.reset();
            response.setHeader("Content-Type", "application/pdf");
            response.setHeader("Content-Length", String.valueOf(file.length()));
            response.setHeader("Content-Disposition", "inline; filename=\"tree.pdf\"");
            BufferedOutputStream output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
            // Write file contents to response.
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            // Finalize task.
            output.flush();
            input.close();
            input.close();
        } catch (IOException | InterruptedException e) {
            printError(e, this);
        }
        // Inform JSF that it doesn't need to handle response.
        // This is very important, otherwise you will get the following exception in the logs:
        // java.lang.IllegalStateException: Cannot forward after response has been committed.
        facesContext.responseComplete();
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //-------------------- FUNCIONES GET AND SET -------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

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

    public String getTxtOutput() {
        return txtOutput;
    }

    public void setTxtOutput(String txtOutput) {
        this.txtOutput = txtOutput;
    }

    public List<SelectItem> getListAttributes() {
        return listAttributes;
    }

    public void setListAttributes(List<SelectItem> listAttributes) {
        this.listAttributes = listAttributes;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }

    public List<SelectItem> getListClassifiers() {
        return listClassifiers;
    }

    public void setListClassifiers(List<SelectItem> listClassifiers) {
        this.listClassifiers = listClassifiers;
    }

    public String getSelectedClassifier() {
        return selectedClassifier;
    }

    public void setSelectedClassifier(String selectedClassifier) {
        this.selectedClassifier = selectedClassifier;
    }

    public String getTestOption() {
        return testOption;
    }

    public void setTestOption(String testOption) {
        this.testOption = testOption;
    }

    public boolean isDisabledPercentageSplit() {
        return disabledPercentageSplit;
    }

    public void setDisabledPercentageSplit(boolean disabledPercentageSplit) {
        this.disabledPercentageSplit = disabledPercentageSplit;
    }

    public double getPercentageSplit() {
        return percentageSplit;
    }

    public void setPercentageSplit(double percentageSplit) {
        this.percentageSplit = percentageSplit;
    }

    public List<Result> getResultList() {
        return resultList;
    }

    public void setResultList(List<Result> resultList) {
        this.resultList = resultList;
    }

    public String getSelectedResult() {
        return selectedResult;
    }

    public void setSelectedResult(String selectedResult) {
        this.selectedResult = selectedResult;
    }

    public boolean isDisabledVisualizeTree() {
        return disabledVisualizeTree;
    }

    public void setDisabledVisualizeTree(boolean disabledVisualizeTree) {
        this.disabledVisualizeTree = disabledVisualizeTree;
    }

    public List<String> getIgnoreAttribulesList() {
        return ignoreAttribulesList;
    }

    public void setIgnoreAttribulesList(List<String> ignoreAttribulesList) {
        this.ignoreAttribulesList = ignoreAttribulesList;
    }

    public boolean isDisabledClassToClusterEvalualtion() {
        return disabledClassToClusterEvalualtion;
    }

    public void setDisabledClassToClusterEvalualtion(boolean disabledClassToClusterEvalualtion) {
        this.disabledClassToClusterEvalualtion = disabledClassToClusterEvalualtion;
    }

    public boolean isDebugCa() {
        return debugCa;
    }

    public void setDebugCa(boolean debugCa) {
        this.debugCa = debugCa;
    }

    public boolean isDoNotCheckCapabilitiesCa() {
        return doNotCheckCapabilitiesCa;
    }

    public void setDoNotCheckCapabilitiesCa(boolean doNotCheckCapabilitiesCa) {
        this.doNotCheckCapabilitiesCa = doNotCheckCapabilitiesCa;
    }

    public boolean isDontReplaceMissingValuesCa() {
        return dontReplaceMissingValuesCa;
    }

    public void setDontReplaceMissingValuesCa(boolean dontReplaceMissingValuesCa) {
        this.dontReplaceMissingValuesCa = dontReplaceMissingValuesCa;
    }

    public int getMaxNumCandidateCanopiesToHoldInMemoryCa() {
        return maxNumCandidateCanopiesToHoldInMemoryCa;
    }

    public void setMaxNumCandidateCanopiesToHoldInMemoryCa(int maxNumCandidateCanopiesToHoldInMemoryCa) {
        this.maxNumCandidateCanopiesToHoldInMemoryCa = maxNumCandidateCanopiesToHoldInMemoryCa;
    }

    public double getMinimumCanopyDensityCa() {
        return minimumCanopyDensityCa;
    }

    public void setMinimumCanopyDensityCa(double minimumCanopyDensityCa) {
        this.minimumCanopyDensityCa = minimumCanopyDensityCa;
    }

    public int getNumClustersCa() {
        return numClustersCa;
    }

    public void setNumClustersCa(int numClustersCa) {
        this.numClustersCa = numClustersCa;
    }

    public int getPeriodicPruningRateCa() {
        return periodicPruningRateCa;
    }

    public void setPeriodicPruningRateCa(int periodicPruningRateCa) {
        this.periodicPruningRateCa = periodicPruningRateCa;
    }

    public int getSeddCa() {
        return seddCa;
    }

    public void setSeddCa(int seddCa) {
        this.seddCa = seddCa;
    }

    public double getT1Ca() {
        return t1Ca;
    }

    public void setT1Ca(double t1Ca) {
        this.t1Ca = t1Ca;
    }

    public double getT2Ca() {
        return t2Ca;
    }

    public void setT2Ca(double t2Ca) {
        this.t2Ca = t2Ca;
    }

    public boolean isDebugCo() {
        return debugCo;
    }

    public void setDebugCo(boolean debugCo) {
        this.debugCo = debugCo;
    }

    public boolean isDoNotCheckCapabilitiesCo() {
        return doNotCheckCapabilitiesCo;
    }

    public void setDoNotCheckCapabilitiesCo(boolean doNotCheckCapabilitiesCo) {
        this.doNotCheckCapabilitiesCo = doNotCheckCapabilitiesCo;
    }

    public boolean isSaveInstancesDataCo() {
        return saveInstancesDataCo;
    }

    public void setSaveInstancesDataCo(boolean saveInstancesDataCo) {
        this.saveInstancesDataCo = saveInstancesDataCo;
    }

    public double getAcuityCo() {
        return acuityCo;
    }

    public void setAcuityCo(double acuityCo) {
        this.acuityCo = acuityCo;
    }

    public double getCutoffCo() {
        return cutoffCo;
    }

    public void setCutoffCo(double cutoffCo) {
        this.cutoffCo = cutoffCo;
    }

    public int getSeedCo() {
        return seedCo;
    }

    public void setSeedCo(int seedCo) {
        this.seedCo = seedCo;
    }

    public boolean isDebugEM() {
        return debugEM;
    }

    public void setDebugEM(boolean debugEM) {
        this.debugEM = debugEM;
    }

    public boolean isDisplayModelInOldFormatEM() {
        return displayModelInOldFormatEM;
    }

    public void setDisplayModelInOldFormatEM(boolean displayModelInOldFormatEM) {
        this.displayModelInOldFormatEM = displayModelInOldFormatEM;
    }

    public boolean isDoNotCheckCapabilitiesEM() {
        return doNotCheckCapabilitiesEM;
    }

    public void setDoNotCheckCapabilitiesEM(boolean doNotCheckCapabilitiesEM) {
        this.doNotCheckCapabilitiesEM = doNotCheckCapabilitiesEM;
    }

    public int getMaxIterationsEM() {
        return maxIterationsEM;
    }

    public void setMaxIterationsEM(int maxIterationsEM) {
        this.maxIterationsEM = maxIterationsEM;
    }

    public int getMaximumNumberOfClustersEM() {
        return maximumNumberOfClustersEM;
    }

    public void setMaximumNumberOfClustersEM(int maximumNumberOfClustersEM) {
        this.maximumNumberOfClustersEM = maximumNumberOfClustersEM;
    }

    public double getMinLogLikelihoodImprovementCVEM() {
        return minLogLikelihoodImprovementCVEM;
    }

    public void setMinLogLikelihoodImprovementCVEM(double minLogLikelihoodImprovementCVEM) {
        this.minLogLikelihoodImprovementCVEM = minLogLikelihoodImprovementCVEM;
    }

    public double getMinLogLikelihoodImprovementIteratingEM() {
        return minLogLikelihoodImprovementIteratingEM;
    }

    public void setMinLogLikelihoodImprovementIteratingEM(double minLogLikelihoodImprovementIteratingEM) {
        this.minLogLikelihoodImprovementIteratingEM = minLogLikelihoodImprovementIteratingEM;
    }

    public double getMinStdDevEM() {
        return minStdDevEM;
    }

    public void setMinStdDevEM(double minStdDevEM) {
        this.minStdDevEM = minStdDevEM;
    }

    public int getNumClustersEM() {
        return numClustersEM;
    }

    public void setNumClustersEM(int numClustersEM) {
        this.numClustersEM = numClustersEM;
    }

    public int getNumExecutionSlotsEM() {
        return numExecutionSlotsEM;
    }

    public void setNumExecutionSlotsEM(int numExecutionSlotsEM) {
        this.numExecutionSlotsEM = numExecutionSlotsEM;
    }

    public int getNumFoldsEM() {
        return numFoldsEM;
    }

    public void setNumFoldsEM(int numFoldsEM) {
        this.numFoldsEM = numFoldsEM;
    }

    public int getSeedEM() {
        return seedEM;
    }

    public void setSeedEM(int seedEM) {
        this.seedEM = seedEM;
    }

    public boolean isDebugFF() {
        return debugFF;
    }

    public void setDebugFF(boolean debugFF) {
        this.debugFF = debugFF;
    }

    public boolean isDoNotCheckCapabilitiesFF() {
        return doNotCheckCapabilitiesFF;
    }

    public void setDoNotCheckCapabilitiesFF(boolean doNotCheckCapabilitiesFF) {
        this.doNotCheckCapabilitiesFF = doNotCheckCapabilitiesFF;
    }

    public int getNumClustersFF() {
        return numClustersFF;
    }

    public void setNumClustersFF(int numClustersFF) {
        this.numClustersFF = numClustersFF;
    }

    public int getSeedFF() {
        return seedFF;
    }

    public void setSeedFF(int seedFF) {
        this.seedFF = seedFF;
    }

    public boolean isDebugHC() {
        return debugHC;
    }

    public void setDebugHC(boolean debugHC) {
        this.debugHC = debugHC;
    }

    public boolean isDoNotCheckCapabilitiesHC() {
        return doNotCheckCapabilitiesHC;
    }

    public void setDoNotCheckCapabilitiesHC(boolean doNotCheckCapabilitiesHC) {
        this.doNotCheckCapabilitiesHC = doNotCheckCapabilitiesHC;
    }

    public boolean isDistanceIsBranchLengthHC() {
        return distanceIsBranchLengthHC;
    }

    public void setDistanceIsBranchLengthHC(boolean distanceIsBranchLengthHC) {
        this.distanceIsBranchLengthHC = distanceIsBranchLengthHC;
    }

    public String getDistanceFunctionHC() {
        return distanceFunctionHC;
    }

    public void setDistanceFunctionHC(String distanceFunctionHC) {
        this.distanceFunctionHC = distanceFunctionHC;
    }

    public String getLinkTypeHC() {
        return linkTypeHC;
    }

    public void setLinkTypeHC(String linkTypeHC) {
        this.linkTypeHC = linkTypeHC;
    }

    public int getNumClustersHC() {
        return numClustersHC;
    }

    public void setNumClustersHC(int numClustersHC) {
        this.numClustersHC = numClustersHC;
    }

    public boolean isPrintNewickHC() {
        return printNewickHC;
    }

    public void setPrintNewickHC(boolean printNewickHC) {
        this.printNewickHC = printNewickHC;
    }

    public int getCanopyMaxCanopiesToHoldInMemorySKM() {
        return canopyMaxCanopiesToHoldInMemorySKM;
    }

    public void setCanopyMaxCanopiesToHoldInMemorySKM(int canopyMaxCanopiesToHoldInMemorySKM) {
        this.canopyMaxCanopiesToHoldInMemorySKM = canopyMaxCanopiesToHoldInMemorySKM;
    }

    public double getCanopyMinimumCanopyDensitySKM() {
        return canopyMinimumCanopyDensitySKM;
    }

    public void setCanopyMinimumCanopyDensitySKM(double canopyMinimumCanopyDensitySKM) {
        this.canopyMinimumCanopyDensitySKM = canopyMinimumCanopyDensitySKM;
    }

    public int getCanopyPeriodicPruningRateSKM() {
        return canopyPeriodicPruningRateSKM;
    }

    public void setCanopyPeriodicPruningRateSKM(int canopyPeriodicPruningRateSKM) {
        this.canopyPeriodicPruningRateSKM = canopyPeriodicPruningRateSKM;
    }

    public double getCanopyT1SKM() {
        return canopyT1SKM;
    }

    public void setCanopyT1SKM(double canopyT1SKM) {
        this.canopyT1SKM = canopyT1SKM;
    }

    public double getCanopyT2SKM() {
        return canopyT2SKM;
    }

    public void setCanopyT2SKM(double canopyT2SKM) {
        this.canopyT2SKM = canopyT2SKM;
    }

    public boolean isDebugSKM() {
        return debugSKM;
    }

    public void setDebugSKM(boolean debugSKM) {
        this.debugSKM = debugSKM;
    }

    public boolean isDisplayStdDevsSKM() {
        return displayStdDevsSKM;
    }

    public void setDisplayStdDevsSKM(boolean displayStdDevsSKM) {
        this.displayStdDevsSKM = displayStdDevsSKM;
    }

    public String getDistanceFunctionSKM() {
        return distanceFunctionSKM;
    }

    public void setDistanceFunctionSKM(String distanceFunctionSKM) {
        this.distanceFunctionSKM = distanceFunctionSKM;
    }

    public boolean isDoNotCheckCapabilitiesSKM() {
        return doNotCheckCapabilitiesSKM;
    }

    public void setDoNotCheckCapabilitiesSKM(boolean doNotCheckCapabilitiesSKM) {
        this.doNotCheckCapabilitiesSKM = doNotCheckCapabilitiesSKM;
    }

    public boolean isDontReplaceMissingValuesSKM() {
        return dontReplaceMissingValuesSKM;
    }

    public void setDontReplaceMissingValuesSKM(boolean dontReplaceMissingValuesSKM) {
        this.dontReplaceMissingValuesSKM = dontReplaceMissingValuesSKM;
    }

    public boolean isFastDistanceCalcSKM() {
        return fastDistanceCalcSKM;
    }

    public void setFastDistanceCalcSKM(boolean fastDistanceCalcSKM) {
        this.fastDistanceCalcSKM = fastDistanceCalcSKM;
    }

    public String getInitializationMethodSKM() {
        return initializationMethodSKM;
    }

    public void setInitializationMethodSKM(String initializationMethodSKM) {
        this.initializationMethodSKM = initializationMethodSKM;
    }

    public int getMaxIterationsSKM() {
        return maxIterationsSKM;
    }

    public void setMaxIterationsSKM(int maxIterationsSKM) {
        this.maxIterationsSKM = maxIterationsSKM;
    }

    public int getNumClustersSKM() {
        return numClustersSKM;
    }

    public void setNumClustersSKM(int numClustersSKM) {
        this.numClustersSKM = numClustersSKM;
    }

    public int getNumExecutionSlotsSKM() {
        return numExecutionSlotsSKM;
    }

    public void setNumExecutionSlotsSKM(int numExecutionSlotsSKM) {
        this.numExecutionSlotsSKM = numExecutionSlotsSKM;
    }

    public boolean isPreserveInstancesOrderSKM() {
        return preserveInstancesOrderSKM;
    }

    public void setPreserveInstancesOrderSKM(boolean preserveInstancesOrderSKM) {
        this.preserveInstancesOrderSKM = preserveInstancesOrderSKM;
    }

    public boolean isReduceNumberOfDistanceCalcsViaCanopiesSKM() {
        return reduceNumberOfDistanceCalcsViaCanopiesSKM;
    }

    public void setReduceNumberOfDistanceCalcsViaCanopiesSKM(boolean reduceNumberOfDistanceCalcsViaCanopiesSKM) {
        this.reduceNumberOfDistanceCalcsViaCanopiesSKM = reduceNumberOfDistanceCalcsViaCanopiesSKM;
    }

    public int getSeedSKM() {
        return seedSKM;
    }

    public void setSeedSKM(int seedSKM) {
        this.seedSKM = seedSKM;
    }

    public String getAttributeIndicesEuclideanDistance() {
        return attributeIndicesEuclideanDistance;
    }

    public void setAttributeIndicesEuclideanDistance(String attributeIndicesEuclideanDistance) {
        this.attributeIndicesEuclideanDistance = attributeIndicesEuclideanDistance;
    }

    public boolean isDontNormalizeEuclideanDistance() {
        return dontNormalizeEuclideanDistance;
    }

    public void setDontNormalizeEuclideanDistance(boolean dontNormalizeEuclideanDistance) {
        this.dontNormalizeEuclideanDistance = dontNormalizeEuclideanDistance;
    }

    public boolean isInvertSelectionEuclideanDistance() {
        return invertSelectionEuclideanDistance;
    }

    public void setInvertSelectionEuclideanDistance(boolean invertSelectionEuclideanDistance) {
        this.invertSelectionEuclideanDistance = invertSelectionEuclideanDistance;
    }

    public String getAttributeIndicesManhattanDistance() {
        return attributeIndicesManhattanDistance;
    }

    public void setAttributeIndicesManhattanDistance(String attributeIndicesManhattanDistance) {
        this.attributeIndicesManhattanDistance = attributeIndicesManhattanDistance;
    }

    public boolean isDontNormalizeManhattanDistance() {
        return dontNormalizeManhattanDistance;
    }

    public void setDontNormalizeManhattanDistance(boolean dontNormalizeManhattanDistance) {
        this.dontNormalizeManhattanDistance = dontNormalizeManhattanDistance;
    }

    public boolean isInvertSelectionManhattanDistance() {
        return invertSelectionManhattanDistance;
    }

    public void setInvertSelectionManhattanDistance(boolean invertSelectionManhattanDistance) {
        this.invertSelectionManhattanDistance = invertSelectionManhattanDistance;
    }

    public String getAttributeIndicesChebyshevDistance() {
        return attributeIndicesChebyshevDistance;
    }

    public void setAttributeIndicesChebyshevDistance(String attributeIndicesChebyshevDistance) {
        this.attributeIndicesChebyshevDistance = attributeIndicesChebyshevDistance;
    }

    public boolean isDontNormalizeChebyshevDistance() {
        return dontNormalizeChebyshevDistance;
    }

    public void setDontNormalizeChebyshevDistance(boolean dontNormalizeChebyshevDistance) {
        this.dontNormalizeChebyshevDistance = dontNormalizeChebyshevDistance;
    }

    public boolean isInvertSelectionChebyshevDistance() {
        return invertSelectionChebyshevDistance;
    }

    public void setInvertSelectionChebyshevDistance(boolean invertSelectionChebyshevDistance) {
        this.invertSelectionChebyshevDistance = invertSelectionChebyshevDistance;
    }

    public String getAttributeIndicesMinkowskiDistance() {
        return attributeIndicesMinkowskiDistance;
    }

    public void setAttributeIndicesMinkowskiDistance(String attributeIndicesMinkowskiDistance) {
        this.attributeIndicesMinkowskiDistance = attributeIndicesMinkowskiDistance;
    }

    public boolean isDontNormalizeMinkowskiDistance() {
        return dontNormalizeMinkowskiDistance;
    }

    public void setDontNormalizeMinkowskiDistance(boolean dontNormalizeMinkowskiDistance) {
        this.dontNormalizeMinkowskiDistance = dontNormalizeMinkowskiDistance;
    }

    public boolean isInvertSelectionMinkowskiDistance() {
        return invertSelectionMinkowskiDistance;
    }

    public void setInvertSelectionMinkowskiDistance(boolean invertSelectionMinkowskiDistance) {
        this.invertSelectionMinkowskiDistance = invertSelectionMinkowskiDistance;
    }

    public double getOrderMinkowskiDistance() {
        return orderMinkowskiDistance;
    }

    public void setOrderMinkowskiDistance(double orderMinkowskiDistance) {
        this.orderMinkowskiDistance = orderMinkowskiDistance;
    }

}
