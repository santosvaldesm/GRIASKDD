/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataMining.classification;

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
import managedBeans.GraphicControlMB;
import static org.apache.jasper.Constants.DEFAULT_BUFFER_SIZE;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.ClassifierEnum;
import util.DinamicTable;
import util.Node;
import util.UtilFunctions;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.HoeffdingTree;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.BatchPredictor;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.gui.CostMatrixEditor;
import weka.gui.Logger;
import weka.gui.SysErrLog;
import weka.gui.TaskLogger;
import weka.gui.explorer.ClassifierErrorsPlotInstances;
import weka.gui.explorer.ExplorerDefaults;

/**
 *
 * @author santos
 */
public class Classification extends UtilFunctions {

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
    private boolean disabledVisualizeTree = true;

    private List<SelectItem> listClassifiers = new ArrayList<>();
    private String selectedClassifier = "";

    //TEST OPTIONS
    private String testOption = "Use training set";//(Use full trainin set, Cross validation)  
    private boolean disabledCrossValidationFolds = true;//cuando se deshabilita el radio buton
    private boolean disabledPercentageSplit = true;//cuando se deshabilitan los controles de folds y seed
    private int crossValidationFolds = 10;
    private double percentageSplit = 66;
    //RESULT LIST
    private List<Result> resultList = new ArrayList<>();//listado de resultados de procesos
    private String selectedResult;
    private int numberResults = 0;//numero de resultados generados

    //CONFIGURATION J48---------------------------------------------------------
    private J48 j48Obj = null;
    private boolean binarySplitsJ48 = false;
    private boolean collapseTreeJ48 = true;
    private double confidenceFactorJ48 = 0.25;
    private boolean debugJ48 = false;
    private boolean doNotCheckCapabilitiesJ48 = false;
    private boolean doNotMakeSplitPointActualValueJ48 = false;
    private int minNumObjectsJ48 = 2;
    private int numFoldsJ48 = 3;
    private boolean reduceErrorPruningJ48 = false;
    private boolean saveInstanceDataJ48 = false;
    private int seedJ48 = 1;
    private boolean subTreeRasingJ48 = true;
    private boolean unprunedJ48 = false;
    private boolean useLaplaceJ48 = false;
    private boolean useMDLcorrectionJ48 = true;
    //CONFIGURATION RANDOM TREE ------------------------------------------------
    private RandomTree randomTreeObj = null;
    private int KValueRT = 0;
    private boolean allowUnclasifiedInstancesRT = false;
    private boolean debugRT = false;
    private boolean doNotCheckCapabilitiesRT = false;
    private int maxDepthRT = 0;
    private double minNumRT = 1.0;
    private double minVariancePropRT = 0.001;
    private int numFoldsRT = 0;
    private int seedRT = 1;
    //CONFIGURATION ID3---------------------------------------------------------
    private ID3 ID3Obj = null;
    //CONFIGURATION LMT---------------------------------------------------------
    private LMT LMTObj = null;
    private boolean convertNominalLMT = false;
    private boolean debugLMT = false;
    private boolean doNotCheckCapabilitiesLMT = false;
    private boolean doNotMakeSplitPointActualValueLMT = false;
    private boolean errorOnProbabilitiesLMT = false;
    private boolean fastRegressionLMT = true;
    private int minNumInstancesLMT = 15;
    private int numBoostingIterationsLMT = -1;
    private boolean splitOnResidualsLMT = false;
    private boolean useAICLMT = false;
    private double weightTrimBetaLMT = 0.0;
    //CONFIGURATION DecisionStump-----------------------------------------------
    private DecisionStump decisionStumpObj = null;
    private boolean debugDS = false;
    private boolean doNotCheckCapabilitiesDS = false;    
    //CONFIGURATION HoeffdingTree-----------------------------------------------
    private HoeffdingTree hoeffdingTreeObj = null;
    private boolean debugHT = false;
    private boolean doNotCheckCapabilitiesHT = false;
    private double gracePeriodHT = 200.0;
    private double hoeffdingTieThresholdHT = 200.0;
    private String leafPredictionStrategyHT = "";
    private double minimumFractionOfWeightInfoGainHT = 0.01;
    private double naiveBayesPredictionThresholdHT = 0.01;
    private boolean printLeafModelsHT = false;
    private double splitConfidenceHT = 1.0E-7;
    private String splitCriterionHT = "";
    //CONFIGURATION M5P---------------------------------------------------------
    private M5P M5PObj = null;
    private boolean builRegressionTreeM5P = false;
    private boolean debugM5P = false;
    private boolean doNotCheckCapabilitiesM5P = false;
    private int minNumInstancesM5P = 15;
    private boolean saveInstancesM5P = false;
    private boolean unprunedM5P = false;
    private boolean useUnsmoothedM5P = false;
    //CONFIGURATION randomForest---------------------------------------------------------
    private RandomForest randomForestObj = null;
    private boolean debugRF = false;
    private boolean doNotCheckCapabilitiesRF = false;
    private boolean dontCalculateOutOfBagErrorRF = false;
    private int maxDepthRF = 0;
    private int numExecutionsSlotsRF = 1;
    private int numFeaturesRF = 0;
    private int numTreesRF = 10;
    private boolean printTreesRF = false;
    private int seedRF = 1;
    //CONFIGURATION REPTree---------------------------------------------------------
    private REPTree REPTreeObj = null;
    private boolean debugREPT = false;
    private boolean doNotCheckCapabilitiesREPT = false;
    private double initialCountREPT = 0.0;
    private int maxDepthREPT = -1;
    private double minNumREPT = 2.0;
    private double minVariancePropREPT = 0.001;
    private boolean noPruningREPT = false;
    private int numFoldsREPT = 3;
    private int seedREPT = 3;
    private boolean spreadInitialCountREPT = false;

    public void changeForm() {//hay un cambio en el formulario(esto para que envie los datos al nodo correspondiente)
    }

    public void stopProcess() {
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

    public Classification(Node p, GraphicControlMB g) {
        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public void changeTestMode() {
        //disabledCrossValidationFoldsSeed = attributeSelectionMode.compareTo("Cross validation") != 0;
        if (testOption.compareTo("Cross validation") == 0) {
            disabledCrossValidationFolds = false;
            disabledPercentageSplit = true;
        } else if (testOption.compareTo("Percentage split") == 0) {
            disabledCrossValidationFolds = true;
            disabledPercentageSplit = false;
        } else if (testOption.compareTo("Use training set") == 0) {
            disabledCrossValidationFolds = true;
            disabledPercentageSplit = true;
        } else if (testOption.compareTo("m_TestSplitBut") == 0) {
            disabledCrossValidationFolds = true;
            disabledPercentageSplit = true;
        } else {
            printError(new Exception("Unknown test mode"), this);
        }
    }

    public final void reset() {
        listClassifiers = new ArrayList<>();
        listClassifiers.add(new SelectItem("J48", "J48"));
        listClassifiers.add(new SelectItem("ID3", "ID3"));
        listClassifiers.add(new SelectItem("LMT", "LMT"));
        listClassifiers.add(new SelectItem("M5P", "M5P"));
        listClassifiers.add(new SelectItem("DecisionStump", "DecisionStump"));
        listClassifiers.add(new SelectItem("HoeffdingTree", "HoeffdingTree"));
        listClassifiers.add(new SelectItem("RandomForest", "RandomForest"));
        listClassifiers.add(new SelectItem("RandomTree", "RandomTree"));
        listClassifiers.add(new SelectItem("REPTree", "REPTree"));
        selectedClassifier = "J48";

        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataClassification.showConfigure}", "Configure", "", "fa fa-cogs"));
        //submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewClassification').show(); PF('wvContextMenu').hide();", null, "View data ", ":IdFormDialogsClassification", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgClassificationHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);
    }

    public void clickConfigureClassifier() {
        switch (ClassifierEnum.convert(selectedClassifier)) {
            case J48:
                RequestContext.getCurrentInstance().execute("PF('wvDlgJ48').show()");
                break;
            case ID3:
                RequestContext.getCurrentInstance().execute("PF('wvDlgID3').show()");
                break;
            case LMT:
                RequestContext.getCurrentInstance().execute("PF('wvDlgLMT').show()");
                break;
            case M5P:
                RequestContext.getCurrentInstance().execute("PF('wvDlgM5P').show()");
                break;
            case DecisionStump:
                RequestContext.getCurrentInstance().execute("PF('wvDlgDecisionStump').show()");
                break;
            case HoeffdingTree:
                RequestContext.getCurrentInstance().execute("PF('wvDlgHoeffdingTree').show()");
                break;            
            case RandomForest:
                RequestContext.getCurrentInstance().execute("PF('wvDlgRandomForest').show()");
                break;
            case RandomTree:
                RequestContext.getCurrentInstance().execute("PF('wvDlgRandomTree').show()");
                break;
            case REPTree:
                RequestContext.getCurrentInstance().execute("PF('wvDlgREPTree').show()");
                break;
            case NOVALUE:
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
        RequestContext.getCurrentInstance().update("IdFormDialogsClassification");
        RequestContext.getCurrentInstance().execute("PF('wvDlgClassification').show()");
    }

    private final Logger m_Log = new SysErrLog();

    //protected JTextArea m_OutText = new JTextArea(20, 40);//The output area for classification results.
    //protected ResultHistoryPanel m_History = new ResultHistoryPanel(m_OutText);//A panel controlling results viewing.
    protected CostMatrixEditor m_CostMatrixEditor = new CostMatrixEditor();//The cost matrix editor for evaluation costs.
    protected List<String> m_selectedEvalMetrics = Evaluation.getAllEvaluationMetricNames();
    protected String m_RandomSeedText = "1";
    protected JCheckBox m_PreserveOrderBut = new JCheckBox("Preserve order for % Split");// Whether randomization is turned off to preserve order.
    String grph = null;

    //protected Instances m_Instances;
    public Classifier createAndConfigureClassifier() {
        switch (ClassifierEnum.convert(selectedClassifier)) {
            case J48:
                j48Obj = new J48();
                j48Obj.setBinarySplits(binarySplitsJ48);
                j48Obj.setCollapseTree(collapseTreeJ48);
                j48Obj.setConfidenceFactor((float) confidenceFactorJ48);
                j48Obj.setDebug(debugJ48);
                j48Obj.setDoNotCheckCapabilities(doNotCheckCapabilitiesJ48);
                j48Obj.setDoNotMakeSplitPointActualValue(doNotMakeSplitPointActualValueJ48);
                j48Obj.setMinNumObj(minNumObjectsJ48);
                j48Obj.setNumFolds(numFoldsJ48);
                j48Obj.setReducedErrorPruning(reduceErrorPruningJ48);
                j48Obj.setSaveInstanceData(saveInstanceDataJ48);
                j48Obj.setSeed(seedJ48);
                j48Obj.setSubtreeRaising(subTreeRasingJ48);
                j48Obj.setUnpruned(unprunedJ48);
                j48Obj.setUseLaplace(useLaplaceJ48);
                j48Obj.setUseMDLcorrection(useMDLcorrectionJ48);
                return j48Obj;
            case ID3:
                ID3Obj = new ID3();
                return ID3Obj;
            case LMT:
                LMTObj = new LMT();
                return LMTObj;
            case M5P:
                M5PObj = new M5P();
                return M5PObj;
            case DecisionStump:
                decisionStumpObj = new DecisionStump();
                return decisionStumpObj;
            case HoeffdingTree:
                hoeffdingTreeObj = new HoeffdingTree();
                return hoeffdingTreeObj;
            case RandomForest:
                randomForestObj = new RandomForest();
                return randomForestObj;
            case RandomTree:
                randomTreeObj = new RandomTree();
                randomTreeObj.setKValue(KValueRT);
                randomTreeObj.setAllowUnclassifiedInstances(allowUnclasifiedInstancesRT);
                randomTreeObj.setDebug(debugRT);
                randomTreeObj.setDoNotCheckCapabilities(doNotCheckCapabilitiesRT);
                randomTreeObj.setMaxDepth(maxDepthRT);
                randomTreeObj.setMinNum(minNumRT);
                randomTreeObj.setMinVarianceProp(minVariancePropRT);
                randomTreeObj.setNumFolds(numFoldsRT);
                randomTreeObj.setSeed(seedRT);
                return randomTreeObj;
            case REPTree:
                REPTreeObj = new REPTree();
                return REPTreeObj;
            case NOVALUE:
                return null;
        }
        return null;
    }

    public void runProcess() {
        try {
            CostMatrix costMatrix = null;
            Instances inst = new Instances(initialData);
            ClassifierErrorsPlotInstances plotInstances = null;
            // for timing
            long trainTimeStart, trainTimeElapsed = 0;
            long testTimeStart, testTimeElapsed = 0;
            boolean outputModel = true;// m_OutputModelBut.isSelected();
            boolean outputConfusion = true;//m_OutputConfusionBut.isSelected();
            boolean outputPerClass = true;//m_OutputPerClassBut.isSelected();
            boolean outputSummary = true;
            boolean outputEntropy = true;//m_OutputEntropyBut.isSelected();
            boolean saveVis = true;//m_StorePredictionsBut.isSelected();

            grph = null;
            int testMode = 0;
            inst.setClassIndex(classIndex);

            Classifier classifier = createAndConfigureClassifier();//(weka.classifiers.Classifier) m_ClassifierEditor.getValue();            
            Classifier template = null;
            try {
                template = AbstractClassifier.makeCopy(classifier);
            } catch (Exception ex) {
                System.out.println("Problem copying classifier: " + ex.getMessage());//m_Log.logMessage("Problem copying classifier: " + ex.getMessage());
            }
            weka.classifiers.Classifier fullClassifier = null;
            StringBuilder outBuff = new StringBuilder();
            AbstractOutput classificationOutput = null;

            String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
            String cname;
            String cmd;
            Evaluation eval = null;
            try {
                if (testOption.compareTo("Cross validation") == 0) {
                    testMode = 1;
                    //numFolds = Integer.parseInt("10");
                    if (crossValidationFolds <= 1) {
                        throw new Exception("Number of folds must be greater than 1");
                    }
                } else if (testOption.compareTo("Percentage split") == 0) {
                    testMode = 2;
                    if ((percentageSplit <= 0) || (percentageSplit >= 100)) {
                        throw new Exception("Percentage must be between 0 and 100");
                    }
                } else if (testOption.compareTo("Use training set") == 0) {
                    testMode = 3;
                } else if (testOption.compareTo("m_TestSplitBut") == 0) {
                    testMode = 4;//SUPLIED TEST SET NO TRABAJARE POR EL MOMENTO(POR ESO SE ELIMINA CODIGO weka.gui.explorer.ClassifierPanel.)
                } else {
                    throw new Exception("Unknown test mode");
                }
                cname = classifier.getClass().getName();
                if (cname.startsWith("weka.classifiers.")) {
                    name += cname.substring("weka.classifiers.".length());
                } else {
                    name += cname;
                }
                cmd = classifier.getClass().getName();
                if (classifier instanceof OptionHandler) {
                    cmd += " " + Utils.joinOptions(((OptionHandler) classifier).getOptions());
                }
                // set up the structure of the plottable instances for visualization
                plotInstances = ExplorerDefaults.getClassifierErrorsPlotInstances();
                plotInstances.setInstances(inst);
                plotInstances.setClassifier(classifier);
                plotInstances.setClassIndex(inst.classIndex());
                plotInstances.setSaveForVisualization(saveVis);//plotInstances.setPointSizeProportionalToMargin(m_errorPlotPointSizeProportionalToMargin);

                // Output some header information
                m_Log.logMessage("Started " + cname);
                m_Log.logMessage("Command: " + cmd);
                if (m_Log instanceof TaskLogger) {
                    ((TaskLogger) m_Log).taskStarted();
                }
                outBuff.append("=== Run information ===\n\n");
                outBuff.append("Scheme:       ").append(cname);
                if (classifier instanceof OptionHandler) {
                    String[] o = ((OptionHandler) classifier).getOptions();
                    outBuff.append(" ").append(Utils.joinOptions(o));
                }
                outBuff.append("\n");
                outBuff.append("Relation:     ").append(inst.relationName()).append('\n');
                outBuff.append("Instances:    ").append(inst.numInstances()).append('\n');
                outBuff.append("Attributes:   ").append(inst.numAttributes()).append('\n');
                if (inst.numAttributes() < 100) {
                    for (int i = 0; i < inst.numAttributes(); i++) {
                        outBuff.append("              ").append(inst.attribute(i).name()).append('\n');
                    }
                } else {
                    outBuff.append("              [list of attributes omitted]\n");
                }

                outBuff.append("Test mode:    ");
                switch (testMode) {
                    case 3: // Test on training
                        outBuff.append("evaluate on training data\n");
                        break;
                    case 1: // CV mode
                        outBuff.append(crossValidationFolds).append("-fold cross-validation\n");
                        break;
                    case 2: // Percent split
                        outBuff.append("split ").append(percentageSplit).append("% train, remainder test\n");
                        break;
                    case 4: // Test on user split
                        break;
                }
                if (costMatrix != null) {
                    outBuff.append("Evaluation cost matrix:\n").append(costMatrix.toString()).append("\n");
                }
                outBuff.append("\n");
                //m_History.addResult(name, outBuff);
                //m_History.setSingle(name);

                // Build the model and output it.
                if (outputModel || (testMode == 3) || (testMode == 4)) {
                    m_Log.statusMessage("Building model on training data...");
                    trainTimeStart = System.currentTimeMillis();
                    classifier.buildClassifier(inst);
                    trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
                }

                if (outputModel) {
                    outBuff.append("=== Classifier model (full training set) ===\n\n")
                            .append(classifier.toString()).append("\n")
                            .append("\nTime taken to build model: ")
                            .append(Utils.doubleToString(trainTimeElapsed / 1000.0, 2))
                            .append(" seconds\n\n");
                    //m_History.updateResult(name);
                    if (classifier instanceof Drawable) {
                        grph = null;
                        try {
                            grph = ((Drawable) classifier).graph();
                        } catch (Exception ex) {
                            printError(ex, this);
                        }
                    }
                    // copy full model for output
                    SerializedObject so = new SerializedObject(classifier);
                    fullClassifier = (weka.classifiers.Classifier) so.getObject();
                }

                switch (testMode) {
                    case 3: // Test on training
                        m_Log.statusMessage("Evaluating on training data...");
                        eval = new Evaluation(inst, costMatrix);
                        // make adjustments if the classifier is an InputMappedClassifier
                        eval = setupEval(eval, classifier, inst, costMatrix, plotInstances, classificationOutput, false);
                        eval.setMetricsToDisplay(m_selectedEvalMetrics);

                        plotInstances.setUp();
                        testTimeStart = System.currentTimeMillis();
                        if (classifier instanceof BatchPredictor) {//&& ((BatchPredictor) classifier).implementsMoreEfficientBatchPrediction()) {
                            Instances toPred = new Instances(inst);
                            for (int i = 0; i < toPred.numInstances(); i++) {
                                toPred.instance(i).setClassMissing();
                            }
                            double[][] predictions = ((BatchPredictor) classifier).distributionsForInstances(toPred);
                            plotInstances.process(inst, predictions, eval);
                        } else {
                            for (int jj = 0; jj < inst.numInstances(); jj++) {
                                plotInstances.process(inst.instance(jj), classifier, eval);
                                if ((jj % 100) == 0) {
                                    m_Log.statusMessage("Evaluating on training data. Processed " + jj + " instances...");
                                }
                            }
                        }
                        testTimeElapsed = System.currentTimeMillis() - testTimeStart;
                        outBuff.append("=== Evaluation on training set ===\n");
                        break;
                    case 1: // CV mode
                        m_Log.statusMessage("Randomizing instances...");
                        int rnd;
                        try {
                            rnd = Integer.parseInt(m_RandomSeedText.trim());//AQUI VA SEDD(SEMILLA)
                            // System.err.println("Using random seed "+rnd);
                        } catch (Exception ex) {
                            m_Log.logMessage("Trouble parsing random seed value");
                            rnd = 1;
                        }
                        Random random = new Random(rnd);
                        inst.randomize(random);
                        if (inst.attribute(classIndex).isNominal()) {
                            m_Log.statusMessage("Stratifying instances...");
                            inst.stratify(crossValidationFolds);
                        }
                        eval = new Evaluation(inst, costMatrix);
                        // make adjustments if the classifier is an InputMappedClassifier
                        eval = setupEval(eval, classifier, inst, costMatrix, plotInstances, classificationOutput, false);
                        eval.setMetricsToDisplay(m_selectedEvalMetrics);
                        // plotInstances.setEvaluation(eval);
                        plotInstances.setUp();
                        // Make some splits and do a CV
                        for (int fold = 0; fold < crossValidationFolds; fold++) {
                            m_Log.statusMessage("Creating splits for fold " + (fold + 1) + "...");
                            Instances train = inst.trainCV(crossValidationFolds, fold, random);
                            // make adjustments if the classifier is an
                            // InputMappedClassifier
                            eval = setupEval(eval, classifier, train, costMatrix, plotInstances, classificationOutput, true);
                            eval.setMetricsToDisplay(m_selectedEvalMetrics);
                            // eval.setPriors(train);
                            m_Log.statusMessage("Building model for fold " + (fold + 1) + "...");
                            Classifier current = null;
                            try {
                                current = AbstractClassifier.makeCopy(template);
                            } catch (Exception ex) {
                                m_Log.logMessage("Problem copying classifier: " + ex.getMessage());
                            }
                            current.buildClassifier(train);
                            Instances test = inst.testCV(crossValidationFolds, fold);
                            m_Log.statusMessage("Evaluating model for fold " + (fold + 1) + "...");

                            if (classifier instanceof BatchPredictor) {//&& ((BatchPredictor) classifier).implementsMoreEfficientBatchPrediction()) {
                                Instances toPred = new Instances(test);
                                for (int i = 0; i < toPred.numInstances(); i++) {
                                    toPred.instance(i).setClassMissing();
                                }
                                double[][] predictions = ((BatchPredictor) current).distributionsForInstances(toPred);
                                plotInstances.process(test, predictions, eval);
                            } else {
                                for (int jj = 0; jj < test.numInstances(); jj++) {
                                    plotInstances.process(test.instance(jj), current, eval);
                                }
                            }
                        }
                        if (inst.attribute(classIndex).isNominal()) {
                            outBuff.append("=== Stratified cross-validation ===\n");
                        } else {
                            outBuff.append("=== Cross-validation ===\n");
                        }
                        break;
                    case 2: // Percent split
                        if (!m_PreserveOrderBut.isSelected()) {
                            m_Log.statusMessage("Randomizing instances...");
                            try {
                                rnd = Integer.parseInt(m_RandomSeedText.trim());
                            } catch (Exception ex) {
                                m_Log.logMessage("Trouble parsing random seed value");
                                rnd = 1;
                            }
                            inst.randomize(new Random(rnd));
                        }
                        int trainSize
                                = (int) Math.round(inst.numInstances() * percentageSplit / 100);
                        int testSize = inst.numInstances() - trainSize;
                        Instances train = new Instances(inst, 0, trainSize);
                        Instances test = new Instances(inst, trainSize, testSize);
                        m_Log.statusMessage("Building model on training split (" + trainSize + " instances)...");
                        weka.classifiers.Classifier current = null;
                        try {
                            current = AbstractClassifier.makeCopy(template);
                        } catch (Exception ex) {
                            m_Log.logMessage("Problem copying classifier: " + ex.getMessage());
                        }
                        current.buildClassifier(train);
                        eval = new Evaluation(train, costMatrix);
                        // make adjustments if the classifier is an InputMappedClassifier
                        eval = setupEval(eval, classifier, train, costMatrix, plotInstances, classificationOutput, false);
                        eval.setMetricsToDisplay(m_selectedEvalMetrics);
                        // plotInstances.setEvaluation(eval);
                        plotInstances.setUp();
                        m_Log.statusMessage("Evaluating on test split...");
                        testTimeStart = System.currentTimeMillis();
                        if (classifier instanceof BatchPredictor) {//&& ((BatchPredictor) classifier).implementsMoreEfficientBatchPrediction()) {
                            Instances toPred = new Instances(test);
                            for (int i = 0; i < toPred.numInstances(); i++) {
                                toPred.instance(i).setClassMissing();
                            }
                            double[][] predictions = ((BatchPredictor) current).distributionsForInstances(toPred);
                            plotInstances.process(test, predictions, eval);
                        } else {
                            for (int jj = 0; jj < test.numInstances(); jj++) {
                                plotInstances.process(test.instance(jj), current, eval);
                                if ((jj % 100) == 0) {
                                    m_Log.statusMessage("Evaluating on test split. Processed "
                                            + jj + " instances...");
                                }
                            }
                        }
                        testTimeElapsed = System.currentTimeMillis() - testTimeStart;
                        outBuff.append("=== Evaluation on test split ===\n");
                        break;
                    case 4: // Test on user split
                        m_Log.statusMessage("Evaluating on test data...");
                        outBuff.append("=== Evaluation on test set ===\n");
                        break;
                    default:
                        throw new Exception("Test mode not implemented");
                }

                if (testMode != 1) {
                    String mode = "";
                    if (testMode == 2) {
                        mode = "training split";
                    } else if (testMode == 3) {
                        mode = "training data";
                    } else if (testMode == 4) {
                        mode = "supplied test set";
                    }
                    outBuff.append("\nTime taken to test model on ").append(mode).append(": ")
                            .append(Utils.doubleToString(testTimeElapsed / 1000.0, 2)).append(" seconds\n\n");
                }
                if (outputSummary) {
                    outBuff.append(eval.toSummaryString(outputEntropy)).append("\n");
                }

                if (inst.attribute(classIndex).isNominal()) {
                    if (outputPerClass) {
                        outBuff.append(eval.toClassDetailsString()).append("\n");
                    }
                    if (outputConfusion) {
                        outBuff.append(eval.toMatrixString()).append("\n");
                    }
                }
                //m_History.updateResult(name);
                m_Log.logMessage("Finished " + cname);
                m_Log.statusMessage("OK");
            } catch (Exception ex) {
                m_Log.logMessage(ex.getMessage());
                printMessage("Errror", "Problem evaluating classifier:\n" + ex.getMessage(), FacesMessage.SEVERITY_ERROR);
                m_Log.statusMessage("Problem evaluating classifier");
            } finally {
                try {
                    if (!saveVis && outputModel) {
                        ArrayList<Object> vv = new ArrayList<>();
                        vv.add(fullClassifier);
                        Instances trainHeader = new Instances(inst, 0);
                        trainHeader.setClassIndex(classIndex);
                        vv.add(trainHeader);
                        if (grph != null) {
                            vv.add(grph);
                        }
                        //    m_History.addObject(name, vv);
                    } else if (saveVis && plotInstances != null
                            && plotInstances.canPlot(false)) {
//                        m_CurrentVis = new VisualizePanel();
//                        m_CurrentVis.setName(name + " (" + inst.relationName() + ")");
//                        m_CurrentVis.setLog(m_Log);
//                        m_CurrentVis.addPlot(plotInstances.getPlotData(cname));
//                        // m_CurrentVis.setColourIndex(plotInstances.getPlotInstances().classIndex()+1);
//                        m_CurrentVis.setColourIndex(plotInstances.getPlotInstances().classIndex());
                        plotInstances.cleanUp();

                        ArrayList<Object> vv = new ArrayList<>();
                        if (outputModel) {
                            vv.add(fullClassifier);
                            Instances trainHeader = new Instances(inst, 0);
                            trainHeader.setClassIndex(classIndex);
                            vv.add(trainHeader);
                            if (grph != null) {
                                vv.add(grph);
                            }
                        }
                        //vv.add(m_CurrentVis);
                        vv.add("");

                        if ((eval != null) && (eval.predictions() != null)) {
                            vv.add(eval.predictions());
                            vv.add(inst.classAttribute());
                        }
                        //m_History.addObject(name, vv);
                    }
                } catch (Exception ex) {
                    printError(ex, this);
                }
                if (m_Log instanceof TaskLogger) {
                    ((TaskLogger) m_Log).taskFinished();
                }
            }
            txtOutput = outBuff.toString();
        } catch (Exception ex) {
            txtOutput = ex.toString();
        }
        resultList.add(new Result(String.valueOf(++numberResults),//IDENTIFICADOR
                (new SimpleDateFormat("HH:mm:ss - ")).format(new Date()) + " " + selectedClassifier,//NOMBRE
                txtOutput, grph));//TEXTO Y ARBOL
        selectedResult = String.valueOf(numberResults);
        changeResultList();
        currentNode.setStateNode("_v");
        RequestContext.getCurrentInstance().update("IdFormDialogsClassification:IdDlgClassification:IdPanelConfigurationClassification");
    }

    protected static Evaluation setupEval(Evaluation eval, weka.classifiers.Classifier classifier,
            Instances inst, CostMatrix costMatrix,
            ClassifierErrorsPlotInstances plotInstances,
            AbstractOutput classificationOutput, boolean onlySetPriors)
            throws Exception {

        if (classifier instanceof weka.classifiers.misc.InputMappedClassifier) {
            Instances mappedClassifierHeader
                    = ((weka.classifiers.misc.InputMappedClassifier) classifier)
                    .getModelHeader(new Instances(inst, 0));

            if (classificationOutput != null) {
                classificationOutput.setHeader(mappedClassifierHeader);
            }

            if (!onlySetPriors) {
                if (costMatrix != null) {
                    eval
                            = new Evaluation(new Instances(mappedClassifierHeader, 0), costMatrix);
                } else {
                    eval = new Evaluation(new Instances(mappedClassifierHeader, 0));
                }
            }

            if (!eval.getHeader().equalHeaders(inst)) {
                // When the InputMappedClassifier is loading a model,
                // we need to make a new dataset that maps the training instances to
                // the structure expected by the mapped classifier - this is only
                // to ensure that the structure and priors computed by
                // evaluation object is correct with respect to the mapped classifier
                Instances mappedClassifierDataset
                        = ((weka.classifiers.misc.InputMappedClassifier) classifier)
                        .getModelHeader(new Instances(mappedClassifierHeader, 0));
                for (int zz = 0; zz < inst.numInstances(); zz++) {
                    Instance mapped
                            = ((weka.classifiers.misc.InputMappedClassifier) classifier)
                            .constructMappedInstance(inst.instance(zz));
                    mappedClassifierDataset.add(mapped);
                }
                eval.setPriors(mappedClassifierDataset);
                if (!onlySetPriors) {
                    if (plotInstances != null) {
                        plotInstances.setInstances(mappedClassifierDataset);
                        plotInstances.setClassifier(classifier);
                        /*
                         * int mappedClass =
                         * ((weka.classifiers.misc.InputMappedClassifier)classifier
                         * ).getMappedClassIndex(); System.err.println("Mapped class index "
                         * + mappedClass);
                         */
                        plotInstances.setClassIndex(mappedClassifierDataset.classIndex());
                        plotInstances.setEvaluation(eval);
                    }
                }
            } else {
                eval.setPriors(inst);
                if (!onlySetPriors) {
                    if (plotInstances != null) {
                        plotInstances.setInstances(inst);
                        plotInstances.setClassifier(classifier);
                        plotInstances.setClassIndex(inst.classIndex());
                        plotInstances.setEvaluation(eval);
                    }
                }
            }
        } else {
            eval.setPriors(inst);
            if (!onlySetPriors) {
                if (plotInstances != null) {
                    plotInstances.setInstances(inst);
                    plotInstances.setClassifier(classifier);
                    plotInstances.setClassIndex(inst.classIndex());
                    plotInstances.setEvaluation(eval);
                }
            }
        }

        return eval;
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

    public void createPdfTree() throws IOException {//genera un pdf de una historia seleccionada en el historial         
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

    public boolean isDisabledCrossValidationFolds() {
        return disabledCrossValidationFolds;
    }

    public void setDisabledCrossValidationFolds(boolean disabledCrossValidationFolds) {
        this.disabledCrossValidationFolds = disabledCrossValidationFolds;
    }

    public boolean isDisabledPercentageSplit() {
        return disabledPercentageSplit;
    }

    public void setDisabledPercentageSplit(boolean disabledPercentageSplit) {
        this.disabledPercentageSplit = disabledPercentageSplit;
    }

    public int getCrossValidationFolds() {
        return crossValidationFolds;
    }

    public void setCrossValidationFolds(int crossValidationFolds) {
        this.crossValidationFolds = crossValidationFolds;
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

    public J48 getJ48Obj() {
        return j48Obj;
    }

    public void setJ48Obj(J48 j48Obj) {
        this.j48Obj = j48Obj;
    }

    public boolean isBinarySplitsJ48() {
        return binarySplitsJ48;
    }

    public void setBinarySplitsJ48(boolean binarySplitsJ48) {
        this.binarySplitsJ48 = binarySplitsJ48;
    }

    public boolean isCollapseTreeJ48() {
        return collapseTreeJ48;
    }

    public void setCollapseTreeJ48(boolean collapseTreeJ48) {
        this.collapseTreeJ48 = collapseTreeJ48;
    }

    public double getConfidenceFactorJ48() {
        return confidenceFactorJ48;
    }

    public void setConfidenceFactorJ48(double confidenceFactorJ48) {
        this.confidenceFactorJ48 = confidenceFactorJ48;
    }

    public boolean isDebugJ48() {
        return debugJ48;
    }

    public void setDebugJ48(boolean debugJ48) {
        this.debugJ48 = debugJ48;
    }

    public boolean isDoNotCheckCapabilitiesJ48() {
        return doNotCheckCapabilitiesJ48;
    }

    public void setDoNotCheckCapabilitiesJ48(boolean doNotCheckCapabilitiesJ48) {
        this.doNotCheckCapabilitiesJ48 = doNotCheckCapabilitiesJ48;
    }

    public boolean isDoNotMakeSplitPointActualValueJ48() {
        return doNotMakeSplitPointActualValueJ48;
    }

    public void setDoNotMakeSplitPointActualValueJ48(boolean doNotMakeSplitPointActualValueJ48) {
        this.doNotMakeSplitPointActualValueJ48 = doNotMakeSplitPointActualValueJ48;
    }

    public int getMinNumObjectsJ48() {
        return minNumObjectsJ48;
    }

    public void setMinNumObjectsJ48(int minNumObjectsJ48) {
        this.minNumObjectsJ48 = minNumObjectsJ48;
    }

    public boolean isReduceErrorPruningJ48() {
        return reduceErrorPruningJ48;
    }

    public void setReduceErrorPruningJ48(boolean reduceErrorPruningJ48) {
        this.reduceErrorPruningJ48 = reduceErrorPruningJ48;
    }

    public boolean isSaveInstanceDataJ48() {
        return saveInstanceDataJ48;
    }

    public void setSaveInstanceDataJ48(boolean saveInstanceDataJ48) {
        this.saveInstanceDataJ48 = saveInstanceDataJ48;
    }

    public int getSeedJ48() {
        return seedJ48;
    }

    public void setSeedJ48(int seedJ48) {
        this.seedJ48 = seedJ48;
    }

    public boolean isSubTreeRasingJ48() {
        return subTreeRasingJ48;
    }

    public void setSubTreeRasingJ48(boolean subTreeRasingJ48) {
        this.subTreeRasingJ48 = subTreeRasingJ48;
    }

    public boolean isUnprunedJ48() {
        return unprunedJ48;
    }

    public void setUnprunedJ48(boolean unprunedJ48) {
        this.unprunedJ48 = unprunedJ48;
    }

    public boolean isUseLaplaceJ48() {
        return useLaplaceJ48;
    }

    public void setUseLaplaceJ48(boolean useLaplaceJ48) {
        this.useLaplaceJ48 = useLaplaceJ48;
    }

    public boolean isUseMDLcorrectionJ48() {
        return useMDLcorrectionJ48;
    }

    public void setUseMDLcorrectionJ48(boolean useMDLcorrectionJ48) {
        this.useMDLcorrectionJ48 = useMDLcorrectionJ48;
    }

    public int getNumFoldsJ48() {
        return numFoldsJ48;
    }

    public void setNumFoldsJ48(int numFoldsJ48) {
        this.numFoldsJ48 = numFoldsJ48;
    }

    public String getGrph() {
        return grph;
    }

    public void setGrph(String grph) {
        this.grph = grph;
    }

    public int getKValueRT() {
        return KValueRT;
    }

    public void setKValueRT(int KValueRT) {
        this.KValueRT = KValueRT;
    }

    public boolean isAllowUnclasifiedInstancesRT() {
        return allowUnclasifiedInstancesRT;
    }

    public void setAllowUnclasifiedInstancesRT(boolean allowUnclasifiedInstancesRT) {
        this.allowUnclasifiedInstancesRT = allowUnclasifiedInstancesRT;
    }

    public boolean isDebugRT() {
        return debugRT;
    }

    public void setDebugRT(boolean debugRT) {
        this.debugRT = debugRT;
    }

    public boolean isDoNotCheckCapabilitiesRT() {
        return doNotCheckCapabilitiesRT;
    }

    public void setDoNotCheckCapabilitiesRT(boolean doNotCheckCapabilitiesRT) {
        this.doNotCheckCapabilitiesRT = doNotCheckCapabilitiesRT;
    }

    public int getMaxDepthRT() {
        return maxDepthRT;
    }

    public void setMaxDepthRT(int maxDepthRT) {
        this.maxDepthRT = maxDepthRT;
    }

    public double getMinNumRT() {
        return minNumRT;
    }

    public void setMinNumRT(double minNumRT) {
        this.minNumRT = minNumRT;
    }

    public double getMinVariancePropRT() {
        return minVariancePropRT;
    }

    public void setMinVariancePropRT(double minVariancePropRT) {
        this.minVariancePropRT = minVariancePropRT;
    }

    public int getNumFoldsRT() {
        return numFoldsRT;
    }

    public void setNumFoldsRT(int numFoldsRT) {
        this.numFoldsRT = numFoldsRT;
    }

    public int getSeedRT() {
        return seedRT;
    }

    public void setSeedRT(int seedRT) {
        this.seedRT = seedRT;
    }

    public ID3 getID3Obj() {
        return ID3Obj;
    }

    public void setID3Obj(ID3 ID3Obj) {
        this.ID3Obj = ID3Obj;
    }

    public boolean isConvertNominalLMT() {
        return convertNominalLMT;
    }

    public void setConvertNominalLMT(boolean convertNominalLMT) {
        this.convertNominalLMT = convertNominalLMT;
    }

    public boolean isDebugLMT() {
        return debugLMT;
    }

    public void setDebugLMT(boolean debugLMT) {
        this.debugLMT = debugLMT;
    }

    public boolean isDoNotCheckCapabilitiesLMT() {
        return doNotCheckCapabilitiesLMT;
    }

    public void setDoNotCheckCapabilitiesLMT(boolean doNotCheckCapabilitiesLMT) {
        this.doNotCheckCapabilitiesLMT = doNotCheckCapabilitiesLMT;
    }

    public boolean isDoNotMakeSplitPointActualValueLMT() {
        return doNotMakeSplitPointActualValueLMT;
    }

    public void setDoNotMakeSplitPointActualValueLMT(boolean doNotMakeSplitPointActualValueLMT) {
        this.doNotMakeSplitPointActualValueLMT = doNotMakeSplitPointActualValueLMT;
    }

    public boolean isErrorOnProbabilitiesLMT() {
        return errorOnProbabilitiesLMT;
    }

    public void setErrorOnProbabilitiesLMT(boolean errorOnProbabilitiesLMT) {
        this.errorOnProbabilitiesLMT = errorOnProbabilitiesLMT;
    }

    public boolean isFastRegressionLMT() {
        return fastRegressionLMT;
    }

    public void setFastRegressionLMT(boolean fastRegressionLMT) {
        this.fastRegressionLMT = fastRegressionLMT;
    }

    public int getMinNumInstancesLMT() {
        return minNumInstancesLMT;
    }

    public void setMinNumInstancesLMT(int minNumInstancesLMT) {
        this.minNumInstancesLMT = minNumInstancesLMT;
    }

    public int getNumBoostingIterationsLMT() {
        return numBoostingIterationsLMT;
    }

    public void setNumBoostingIterationsLMT(int numBoostingIterationsLMT) {
        this.numBoostingIterationsLMT = numBoostingIterationsLMT;
    }

    public boolean isSplitOnResidualsLMT() {
        return splitOnResidualsLMT;
    }

    public void setSplitOnResidualsLMT(boolean splitOnResidualsLMT) {
        this.splitOnResidualsLMT = splitOnResidualsLMT;
    }

    public boolean isUseAICLMT() {
        return useAICLMT;
    }

    public void setUseAICLMT(boolean useAICLMT) {
        this.useAICLMT = useAICLMT;
    }

    public double getWeightTrimBetaLMT() {
        return weightTrimBetaLMT;
    }

    public void setWeightTrimBetaLMT(double weightTrimBetaLMT) {
        this.weightTrimBetaLMT = weightTrimBetaLMT;
    }

    public boolean isDebugDS() {
        return debugDS;
    }

    public void setDebugDS(boolean debugDS) {
        this.debugDS = debugDS;
    }

    public boolean isDoNotCheckCapabilitiesDS() {
        return doNotCheckCapabilitiesDS;
    }

    public void setDoNotCheckCapabilitiesDS(boolean doNotCheckCapabilitiesDS) {
        this.doNotCheckCapabilitiesDS = doNotCheckCapabilitiesDS;
    }

    public boolean isDebugHT() {
        return debugHT;
    }

    public void setDebugHT(boolean debugHT) {
        this.debugHT = debugHT;
    }

    public boolean isDoNotCheckCapabilitiesHT() {
        return doNotCheckCapabilitiesHT;
    }

    public void setDoNotCheckCapabilitiesHT(boolean doNotCheckCapabilitiesHT) {
        this.doNotCheckCapabilitiesHT = doNotCheckCapabilitiesHT;
    }

    public double getGracePeriodHT() {
        return gracePeriodHT;
    }

    public void setGracePeriodHT(double gracePeriodHT) {
        this.gracePeriodHT = gracePeriodHT;
    }

    public double getHoeffdingTieThresholdHT() {
        return hoeffdingTieThresholdHT;
    }

    public void setHoeffdingTieThresholdHT(double hoeffdingTieThresholdHT) {
        this.hoeffdingTieThresholdHT = hoeffdingTieThresholdHT;
    }

    public String getLeafPredictionStrategyHT() {
        return leafPredictionStrategyHT;
    }

    public void setLeafPredictionStrategyHT(String leafPredictionStrategyHT) {
        this.leafPredictionStrategyHT = leafPredictionStrategyHT;
    }

    public double getMinimumFractionOfWeightInfoGainHT() {
        return minimumFractionOfWeightInfoGainHT;
    }

    public void setMinimumFractionOfWeightInfoGainHT(double minimumFractionOfWeightInfoGainHT) {
        this.minimumFractionOfWeightInfoGainHT = minimumFractionOfWeightInfoGainHT;
    }

    public double getNaiveBayesPredictionThresholdHT() {
        return naiveBayesPredictionThresholdHT;
    }

    public void setNaiveBayesPredictionThresholdHT(double naiveBayesPredictionThresholdHT) {
        this.naiveBayesPredictionThresholdHT = naiveBayesPredictionThresholdHT;
    }

    public boolean isPrintLeafModelsHT() {
        return printLeafModelsHT;
    }

    public void setPrintLeafModelsHT(boolean printLeafModelsHT) {
        this.printLeafModelsHT = printLeafModelsHT;
    }

    public double getSplitConfidenceHT() {
        return splitConfidenceHT;
    }

    public void setSplitConfidenceHT(double splitConfidenceHT) {
        this.splitConfidenceHT = splitConfidenceHT;
    }

    public String getSplitCriterionHT() {
        return splitCriterionHT;
    }

    public void setSplitCriterionHT(String splitCriterionHT) {
        this.splitCriterionHT = splitCriterionHT;
    }

    public boolean isBuilRegressionTreeM5P() {
        return builRegressionTreeM5P;
    }

    public void setBuilRegressionTreeM5P(boolean builRegressionTreeM5P) {
        this.builRegressionTreeM5P = builRegressionTreeM5P;
    }

    public boolean isDebugM5P() {
        return debugM5P;
    }

    public void setDebugM5P(boolean debugM5P) {
        this.debugM5P = debugM5P;
    }

    public boolean isDoNotCheckCapabilitiesM5P() {
        return doNotCheckCapabilitiesM5P;
    }

    public void setDoNotCheckCapabilitiesM5P(boolean doNotCheckCapabilitiesM5P) {
        this.doNotCheckCapabilitiesM5P = doNotCheckCapabilitiesM5P;
    }

    public int getMinNumInstancesM5P() {
        return minNumInstancesM5P;
    }

    public void setMinNumInstancesM5P(int minNumInstancesM5P) {
        this.minNumInstancesM5P = minNumInstancesM5P;
    }

    public boolean isSaveInstancesM5P() {
        return saveInstancesM5P;
    }

    public void setSaveInstancesM5P(boolean saveInstancesM5P) {
        this.saveInstancesM5P = saveInstancesM5P;
    }

    public boolean isUnprunedM5P() {
        return unprunedM5P;
    }

    public void setUnprunedM5P(boolean unprunedM5P) {
        this.unprunedM5P = unprunedM5P;
    }

    public boolean isUseUnsmoothedM5P() {
        return useUnsmoothedM5P;
    }

    public void setUseUnsmoothedM5P(boolean useUnsmoothedM5P) {
        this.useUnsmoothedM5P = useUnsmoothedM5P;
    }

    public boolean isDebugRF() {
        return debugRF;
    }

    public void setDebugRF(boolean debugRF) {
        this.debugRF = debugRF;
    }

    public boolean isDoNotCheckCapabilitiesRF() {
        return doNotCheckCapabilitiesRF;
    }

    public void setDoNotCheckCapabilitiesRF(boolean doNotCheckCapabilitiesRF) {
        this.doNotCheckCapabilitiesRF = doNotCheckCapabilitiesRF;
    }

    public boolean isDontCalculateOutOfBagErrorRF() {
        return dontCalculateOutOfBagErrorRF;
    }

    public void setDontCalculateOutOfBagErrorRF(boolean dontCalculateOutOfBagErrorRF) {
        this.dontCalculateOutOfBagErrorRF = dontCalculateOutOfBagErrorRF;
    }

    public int getMaxDepthRF() {
        return maxDepthRF;
    }

    public void setMaxDepthRF(int maxDepthRF) {
        this.maxDepthRF = maxDepthRF;
    }

    public int getNumExecutionsSlotsRF() {
        return numExecutionsSlotsRF;
    }

    public void setNumExecutionsSlotsRF(int numExecutionsSlotsRF) {
        this.numExecutionsSlotsRF = numExecutionsSlotsRF;
    }

    public int getNumFeaturesRF() {
        return numFeaturesRF;
    }

    public void setNumFeaturesRF(int numFeaturesRF) {
        this.numFeaturesRF = numFeaturesRF;
    }

    public int getNumTreesRF() {
        return numTreesRF;
    }

    public void setNumTreesRF(int numTreesRF) {
        this.numTreesRF = numTreesRF;
    }

    public boolean isPrintTreesRF() {
        return printTreesRF;
    }

    public void setPrintTreesRF(boolean printTreesRF) {
        this.printTreesRF = printTreesRF;
    }

    public int getSeedRF() {
        return seedRF;
    }

    public void setSeedRF(int seedRF) {
        this.seedRF = seedRF;
    }

    public boolean isDebugREPT() {
        return debugREPT;
    }

    public void setDebugREPT(boolean debugREPT) {
        this.debugREPT = debugREPT;
    }

    public boolean isDoNotCheckCapabilitiesREPT() {
        return doNotCheckCapabilitiesREPT;
    }

    public void setDoNotCheckCapabilitiesREPT(boolean doNotCheckCapabilitiesREPT) {
        this.doNotCheckCapabilitiesREPT = doNotCheckCapabilitiesREPT;
    }

    public double getInitialCountREPT() {
        return initialCountREPT;
    }

    public void setInitialCountREPT(double initialCountREPT) {
        this.initialCountREPT = initialCountREPT;
    }

    public int getMaxDepthREPT() {
        return maxDepthREPT;
    }

    public void setMaxDepthREPT(int maxDepthREPT) {
        this.maxDepthREPT = maxDepthREPT;
    }

    public double getMinNumREPT() {
        return minNumREPT;
    }

    public void setMinNumREPT(double minNumREPT) {
        this.minNumREPT = minNumREPT;
    }

    public double getMinVariancePropREPT() {
        return minVariancePropREPT;
    }

    public void setMinVariancePropREPT(double minVariancePropREPT) {
        this.minVariancePropREPT = minVariancePropREPT;
    }

    public boolean isNoPruningREPT() {
        return noPruningREPT;
    }

    public void setNoPruningREPT(boolean noPruningREPT) {
        this.noPruningREPT = noPruningREPT;
    }

    public int getNumFoldsREPT() {
        return numFoldsREPT;
    }

    public void setNumFoldsREPT(int numFoldsREPT) {
        this.numFoldsREPT = numFoldsREPT;
    }

    public int getSeedREPT() {
        return seedREPT;
    }

    public void setSeedREPT(int seedREPT) {
        this.seedREPT = seedREPT;
    }

    public boolean isSpreadInitialCountREPT() {
        return spreadInitialCountREPT;
    }

    public void setSpreadInitialCountREPT(boolean spreadInitialCountREPT) {
        this.spreadInitialCountREPT = spreadInitialCountREPT;
    }

}
