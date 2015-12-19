/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import util.Node;
import util.UtilFunctions;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import managedBeans.GraphicControlMB;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import util.PageEventPdf;
import util.TitlePdf;
import util.filters.AttributeRow;
import util.filters.ComparationAttributes;
import util.filters.NumericRange;
import util.filters.ValueRow;
import weka.core.AttributeStats;
import weka.core.Instances;

/**
 *
 * @author santos
 */
public class DataAnalisis extends UtilFunctions {

    //---------------------CURRENT RELATION -----------------------------
    private String relationName = "No file loaded.";
    private String relationNumAttributes = "";
    private String relationNumInstances = "";
    private String relationSumWeights = "";
    //---------------------ATTRIBUTES -----------------------------------
    private ArrayList<AttributeRow> listAttributes = new ArrayList<>();
    private AttributeRow selectedAtrribute = new AttributeRow(null, 0);
    protected AttributeStats[] m_AttributeStats;
    //---------------------SELECTED ATTRIBUTE----------------------------
    private ArrayList<ValueRow> listValues = new ArrayList<>();
    private String fileName = "No file loaded.";

    private final Node currentNode;
    private GraphicControlMB graphicControlMB = null;//acceso a la clase principal
    private Instances data = null;//datos de instancias actuales

    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private int idAtributeClassSelected = -1;

    private StreamedContent streamImage;
    private StreamedContent streamImageLarge;//imagen Grande
    private JFreeChart chartSmall;
    private JFreeChart chartLarge;
    private String titleGraph = "";

    private com.itextpdf.text.Font fontNormal10 = null;
    private com.itextpdf.text.Font fontCursiva10 = null;
    private com.itextpdf.text.Font fontBold10 = null;
    private com.itextpdf.text.Font fontLink = null;
    private com.itextpdf.text.Font fontNormal12 = null;
    private com.itextpdf.text.Font fontCursiva12 = null;
    private com.itextpdf.text.Font fontBold12 = null;

    public void changeForm() {//hay un cambi en el formulario(esto para que envie los datos al nodo correspondiente)
        //    System.out.println("Cambio");
    }

    public DataAnalisis(Node p, GraphicControlMB g) {

        currentNode = p;
        graphicControlMB = g;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Run", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.selectedNode.dataAnalisis.runProcess}", "Config file", ":IdFormDialogsDataAnalisis", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgViewDataAnalisis').show(); PF('wvContextMenu').hide();", null, "View data", ":IdFormDialogsDataAnalisis", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.FALSE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove Node", "", "fa fa-remove"));
        submenu.addElement(createDefaultMenuItem("Help", Boolean.FALSE, "PF('wvDlgPlainTextHelp').show(); PF('wvContextMenu').hide();", null, "Help", "", "fa fa-question-circle"));
        menuModel.addElement(submenu);

        createFonts();
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
        data = new Instances(nodeParent.getData());
        //initialData = new Instances(nodeParent.getData());
        loadInstancesData(data);
        currentNode.setStateNode("_v");
        changeDisabledOption("View", Boolean.FALSE, submenu);//se habilita view
        changeDisabledOption("Run", Boolean.TRUE, submenu);//se deshabilita run

        fileName = "";
        idAtributeClassSelected = -1;
        currentNode.repaintGraphic();
    }

    private void loadInstancesData(Instances in) {
        relationName = fileName;
        listAttributes = new ArrayList<>();
        int numAttributes = in.numAttributes();
        relationNumAttributes = String.valueOf(numAttributes);
        relationNumInstances = String.valueOf(in.numInstances());
        relationSumWeights = String.valueOf(in.sumOfWeights());
        AttributeRow rowFileData;
        for (int j = 0; j < numAttributes; j++) {
            rowFileData = new AttributeRow(in, j);
            listAttributes.add(rowFileData);
        }
        if (!listAttributes.isEmpty()) {
            selectedAtrribute = listAttributes.get(0);
        } else {
            selectedAtrribute = null;
        }
        changeSelectAtrribute();
    }

    private int determinePositionNominal(List<ValueRow> valuesList, String value) {//determina la posicion del valor de un atributo
        for (int i = 0; i < valuesList.size(); i++) {
            if (valuesList.get(i).getLabel().compareTo(value) == 0) {
                return i;
            }
        }
        return 0;
    }

    //DETERMINAR SI ES POSIBLE CREAR EL GRAFICO SI LOS DATOS NO SUPERAN UN NUMERO DETERMINADO
    private boolean isPossibleCreateGraphic() {
        titleGraph = "Too many values to display";
        if (idAtributeClassSelected == -1 || !listAttributes.get(idAtributeClassSelected).isIsNominalData()) {//no class   || attributeClass no es nominal
            if (selectedAtrribute.getListValuesData().size() > 80) {
                return false;//NO SE PUEDE CREAR GRAFICO
            }
        } else {//hay clase
            if (selectedAtrribute.getListValuesData().size() > 80 || listAttributes.get(idAtributeClassSelected).getListValuesData().size() > 80) {
                return false;//NO SE PUEDE CREAR GRAFICO
            }
        }
        return true;//SE PUEDE CREAR GRAFICO
    }

    private DefaultCategoryDataset createNominalDataSetToPng() {//DATASET PARA JFREECHART, selectedAtrribute= ATRIBUTO SELECCIONADO, listAttributes.get(idAtributeClassSelected)= CLASE USADA (-1 = no class)

        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        if (idAtributeClassSelected == -1 || !listAttributes.get(idAtributeClassSelected).isIsNominalData()) {//no class   || attributeClass no es nominal
            titleGraph = "Attribute: " + selectedAtrribute.getAttributeName();
            for (ValueRow value : selectedAtrribute.getListValuesData()) {
                dataSet.setValue(value.getCount(), selectedAtrribute.getAttributeName(), value.getLabel());
            }
        } else {//hay clase
            titleGraph = "Attribute: " + selectedAtrribute.getAttributeName() + " - Class: " + listAttributes.get(idAtributeClassSelected).getAttributeName();
            ArrayList<ComparationAttributes> valuesGraph = new ArrayList<>();//values graph contiene la multiplicacion de la cantidad de valores del atributo seleccionado y el atributoClase
            for (ValueRow valueAttribute : selectedAtrribute.getListValuesData()) {
                for (ValueRow valueAttributeClass : listAttributes.get(idAtributeClassSelected).getListValuesData()) {
                    valuesGraph.add(new ComparationAttributes(valueAttribute.getLabel(), valueAttributeClass.getLabel(), 0));
                }
            }
            int posAttribute;
            int posAttributeClass;
            int posValuesGraph;
            for (int k = 0; k < data.numInstances(); k++) {
                if (!data.instance(k).isMissing(selectedAtrribute.getIdAttribute())) {//attribute
                    if (!data.instance(k).isMissing(idAtributeClassSelected)) {//class                        
                        posAttribute = determinePositionNominal(selectedAtrribute.getListValuesData(), data.instance(k).stringValue(selectedAtrribute.getIdAttribute()));
                        posAttributeClass = determinePositionNominal(listAttributes.get(idAtributeClassSelected).getListValuesData(), data.instance(k).stringValue(idAtributeClassSelected));
                        posValuesGraph = (posAttribute * listAttributes.get(idAtributeClassSelected).getListValuesData().size()) + posAttributeClass;
                        valuesGraph.get(posValuesGraph).setCount(valuesGraph.get(posValuesGraph).getCount() + 1);
                    }
                }
            }
            for (ComparationAttributes valueGraph : valuesGraph) {
                dataSet.setValue(valueGraph.getCount(), valueGraph.getClassValue(), valueGraph.getAttributeValue());//System.out.println(valueGraph.getAttributeValue() + "," + valueGraph.getClassValue() + ": " + valueGraph.getCount());
            }
        }
        return dataSet;
    }

    private int determinePositionRange(ArrayList<NumericRange> ranges, double valueInstance) {
        for (int i = 0; i < ranges.size(); i++) {
            if (valueInstance >= ranges.get(i).getMin() && valueInstance < ranges.get(i).getMax()) {
                return i;
            }
        }
        return 0;
    }

    private DefaultCategoryDataset createNumericDataSetToPng() {//CUANDO SE TRATA DE VALORES NUMERICOS SE GENERA UN HISTOGRAMA
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        int intervals;//This uses the M.P.Wand's method to calculate the histogram's interval width. See "Data-Based Choice of Histogram Bin Width", in: The American Statistician, Vol. 51, No. 1, Feb., 1997, pp. 59-64.
        double intervalWidth;//intervalWidth = Math.pow(6D/( -psi(2, g21())*m_data.numInstances()),1/3D );        
        AttributeStats m_as = selectedAtrribute.getAttributeStats();
        double starValue = m_as.numericStats.min;
        double endValue;

        int posAttribute;
        int posAttributeClass;
        int posValuesGraph;
        ArrayList<ComparationAttributes> valuesGraph = new ArrayList<>();

        //CALCULO DE INTERVALO Y LONGITUD INTERVALO -----------------
        intervalWidth = 3.49 * m_as.numericStats.stdDev * Math.pow(data.numInstances(), -1 / 3D);//This uses the Scott's method to calculate the histogram's interval width. See "On optimal and data-based histograms". See Biometrika, 66, 605-610 OR see the same paper mentioned above.                    
        intervals = Math.max(1, (int) Math.round((m_as.numericStats.max - m_as.numericStats.min) / intervalWidth));//The Math.max is introduced to remove the possibility of intervals=0 and =NAN that can happen if respectively all the numeric values are the same or the interval width is evaluated to zero.
        //System.out.println("Max: " + m_as.numericStats.max + " Min: " + m_as.numericStats.min + " stdDev: " + m_as.numericStats.stdDev + "intervalWidth: " + intervalWidth + "intervals: " + intervals);

        //CREACION DE RANGOS PARA EL GRAFICO-----------------
        endValue = starValue + intervalWidth;
        ArrayList<NumericRange> ranges = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            ranges.add(new NumericRange(starValue, endValue));
            starValue = endValue;
            endValue = starValue + intervalWidth;
        }
        ranges.get(intervals - 1).setMax(m_as.numericStats.max);//colocar ultimo maximo = maximo de numericStats

        if (idAtributeClassSelected == -1 || !listAttributes.get(idAtributeClassSelected).isIsNominalData()) {//no class   || attributeClass no es nominal
            titleGraph = "Attribute: " + selectedAtrribute.getAttributeName();
            //CREAR COMBINACIONES
            for (NumericRange range : ranges) {
                valuesGraph.add(new ComparationAttributes(selectedAtrribute.getAttributeName(), range.getRangeStr(), 0));
            }
            for (int k = 0; k < data.numInstances(); k++) {
                if (!data.instance(k).isMissing(selectedAtrribute.getIdAttribute())) {//attribute
                    posAttribute = determinePositionRange(ranges, data.instance(k).value(selectedAtrribute.getIdAttribute()));
                    valuesGraph.get(posAttribute).setCount(valuesGraph.get(posAttribute).getCount() + 1);
                }
            }
        } else {//el atributo clase es nominal
            titleGraph = "Attribute: " + selectedAtrribute.getAttributeName() + " - Class: " + listAttributes.get(idAtributeClassSelected).getAttributeName();
            for (NumericRange range : ranges) {
                for (ValueRow valueAttributeClass : listAttributes.get(idAtributeClassSelected).getListValuesData()) {
                    valuesGraph.add(new ComparationAttributes(valueAttributeClass.getLabel(), range.getRangeStr(), 0));
                }
            }
            for (int k = 0; k < data.numInstances(); k++) {
                if (!data.instance(k).isMissing(selectedAtrribute.getIdAttribute())) {//attribute
                    if (!data.instance(k).isMissing(idAtributeClassSelected)) {//class                        
                        posAttribute = determinePositionRange(ranges, data.instance(k).value(selectedAtrribute.getIdAttribute()));
                        posAttributeClass = determinePositionNominal(listAttributes.get(idAtributeClassSelected).getListValuesData(), data.instance(k).stringValue(idAtributeClassSelected));
                        posValuesGraph = (posAttribute * listAttributes.get(idAtributeClassSelected).getListValuesData().size()) + posAttributeClass;
                        valuesGraph.get(posValuesGraph).setCount(valuesGraph.get(posValuesGraph).getCount() + 1);
                    }
                }
            }
        }
        //
        for (ComparationAttributes valueGraph : valuesGraph) {
            dataSet.setValue(valueGraph.getCount(), valueGraph.getAttributeValue(), valueGraph.getClassValue());
        }

        return dataSet;
    }

    private DefaultCategoryDataset createNumericDataSetToPdf(AttributeRow selectAttribute) {//CUANDO SE TRATA DE VALORES NUMERICOS SE GENERA UN HISTOGRAMA, PARA EL PDF NO SE USA CLASE
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        int intervals;//This uses the M.P.Wand's method to calculate the histogram's interval width. See "Data-Based Choice of Histogram Bin Width", in: The American Statistician, Vol. 51, No. 1, Feb., 1997, pp. 59-64.
        double intervalWidth;//intervalWidth = Math.pow(6D/( -psi(2, g21())*m_data.numInstances()),1/3D );        
        AttributeStats m_as = selectAttribute.getAttributeStats();
        double starValue = m_as.numericStats.min;
        double endValue;
        int posAttribute;
        ArrayList<ComparationAttributes> valuesGraph = new ArrayList<>();
        //CALCULO DE INTERVALO Y LONGITUD INTERVALO -----------------
        intervalWidth = 3.49 * m_as.numericStats.stdDev * Math.pow(data.numInstances(), -1 / 3D);//This uses the Scott's method to calculate the histogram's interval width. See "On optimal and data-based histograms". See Biometrika, 66, 605-610 OR see the same paper mentioned above.                    
        intervals = Math.max(1, (int) Math.round((m_as.numericStats.max - m_as.numericStats.min) / intervalWidth));//The Math.max is introduced to remove the possibility of intervals=0 and =NAN that can happen if respectively all the numeric values are the same or the interval width is evaluated to zero.
        //System.out.println("Max: " + m_as.numericStats.max + " Min: " + m_as.numericStats.min + " stdDev: " + m_as.numericStats.stdDev + "intervalWidth: " + intervalWidth + "intervals: " + intervals);
        //CREACION DE RANGOS PARA EL GRAFICO-----------------
        endValue = starValue + intervalWidth;
        ArrayList<NumericRange> ranges = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            ranges.add(new NumericRange(starValue, endValue));
            starValue = endValue;
            endValue = starValue + intervalWidth;
        }
        ranges.get(intervals - 1).setMax(m_as.numericStats.max);//colocar ultimo maximo = maximo de numericStats        
        for (NumericRange range : ranges) {//CREAR COMBINACIONES
            valuesGraph.add(new ComparationAttributes(selectAttribute.getAttributeName(), range.getRangeStr(), 0));
        }
        for (int k = 0; k < data.numInstances(); k++) {
            if (!data.instance(k).isMissing(selectAttribute.getIdAttribute())) {//attribute
                posAttribute = determinePositionRange(ranges, data.instance(k).value(selectAttribute.getIdAttribute()));
                valuesGraph.get(posAttribute).setCount(valuesGraph.get(posAttribute).getCount() + 1);
            }
        }
        for (ComparationAttributes valueGraph : valuesGraph) {
            dataSet.setValue(valueGraph.getCount(), valueGraph.getAttributeValue(), valueGraph.getClassValue());
        }
        return dataSet;
    }

    private DefaultCategoryDataset createEmptyDataSet() {
        titleGraph = "Attribute: " + selectedAtrribute.getAttributeName();
        return new DefaultCategoryDataset();
    }

    private void createChartPNG() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        boolean createGraphic = isPossibleCreateGraphic();
        if (createGraphic) {
            if (selectedAtrribute != null) {
                if (selectedAtrribute.isIsNominalData()) {
                    dataset = createNominalDataSetToPng();
                } else if (selectedAtrribute.isIsNumericData()) {
                    dataset = createNumericDataSetToPng();
                } else {
                    dataset = createEmptyDataSet();
                }
            }
        }
        //----------------CONSTRUCCION DEL GRAFICO---------------------------

        chartSmall = ChartFactory.createStackedBarChart(titleGraph, titleGraph, "Count", dataset, PlotOrientation.VERTICAL, false, false, false);
        chartLarge = ChartFactory.createStackedBarChart(titleGraph, titleGraph, "Count", dataset, PlotOrientation.VERTICAL, true, false, false);

        chartLarge.getTitle().setFont(new Font("arial", Font.PLAIN, 15));//fuente titulo

        CategoryPlot plotSmall = (CategoryPlot) chartSmall.getPlot();
        CategoryPlot plotLarge = (CategoryPlot) chartLarge.getPlot();

        chartSmall.getTitle().visible = false;
        //chartLarge.getTitle().visible = false;

        ((BarRenderer) plotSmall.getRenderer()).setBarPainter(new StandardBarPainter());//quitar gradiente            
        ((BarRenderer) plotLarge.getRenderer()).setBarPainter(new StandardBarPainter());//quitar gradiente            

        plotSmall.setBackgroundPaint(Color.white);//fondo blanco
        plotLarge.setBackgroundPaint(Color.white);//fondo blanco

        plotSmall.setOutlineVisible(false);//grafico sin borde                         
        plotLarge.setOutlineVisible(false);//grafico sin borde                         

        ((CategoryAxis) plotSmall.getDomainAxis()).setVisible(false);//MOSTRAR EJE X DEL GRAFICO
        ((ValueAxis) plotSmall.getRangeAxis()).setVisible(false);//MOSTRAR EJE Y DEL GRAFICO

        ((CategoryAxis) plotLarge.getDomainAxis()).setLabel("");
        ((ValueAxis) plotLarge.getRangeAxis()).setLabel("");

        ((CategoryAxis) plotLarge.getDomainAxis()).setCategoryLabelPositions(CategoryLabelPositions.UP_45);//rotar etiquetas

        plotSmall.getRenderer().setItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0")));//formato de los items. {1}NOMBRE SERIE {2}=CONTEO DE LA SERIE {3}=PORCENTAGE EN LA SERIE
        plotLarge.getRenderer().setItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0")));//formato de los items. {1}NOMBRE SERIE {2}=CONTEO DE LA SERIE {3}=PORCENTAGE EN LA SERIE

        plotSmall.getRenderer().setItemLabelsVisible(true);//mostrar items(conteo en cada serie de un grafico)
        plotSmall.getRenderer().setItemLabelFont(new Font("arial", Font.BOLD, 9));//fuente de los items mostrados
        plotLarge.getRenderer().setItemLabelsVisible(true);//mostrar items(conteo en cada serie de un grafico)
        plotLarge.getRenderer().setItemLabelFont(new Font("arial", Font.BOLD, 9));//fuente de los items mostrados

        //----------GENERAR PNG A PARTIR DEL CHART
        streamImage = createStreamImage("grafico", chartSmall, 300, 385);
        streamImageLarge = createStreamImage("graficoLarge", chartLarge, 600, 500);
    }

    private StreamedContent createStreamImage(String name, JFreeChart chart, int width, int height) {
        try {
            File chartFile = new File(name);
            ChartUtilities.saveChartAsPNG(chartFile, chart, width, height);
            return new DefaultStreamedContent(new FileInputStream(chartFile), "image/png");
        } catch (IOException ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

    private JFreeChart createChartPDF(AttributeRow selectAttribute, List<ValueRow> listValuesOrdered) {
        //CREA UN CHART QUE SERA GRAFICADO EN UN ARCHVO PDF, DE selectAttribute SE OBTINEN EL TIPO DE DATO Y DE listValuesOrdered LOS VALORES DEL DATASET ORDENADO
        JFreeChart chartPdf;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        //boolean createGraphic = isPossibleCreateGraphic();
        //if (createGraphic) {
        if (selectAttribute != null) {
            if (selectAttribute.isIsNominalData()) {
                dataset = new DefaultCategoryDataset();
                for (ValueRow value : listValuesOrdered) {
                    dataset.setValue(value.getCount(), selectAttribute.getAttributeName(), value.getLabel());
                }
            } else if (selectAttribute.isIsNumericData()) {
                dataset = createNumericDataSetToPdf(selectAttribute);
            } else {
                dataset = new DefaultCategoryDataset();
            }
        }
        //}
        //----------------CONSTRUCCION DEL GRAFICO------------------------------
        chartPdf = ChartFactory.createStackedBarChart(titleGraph, titleGraph, "Count", dataset, PlotOrientation.VERTICAL, false, false, false);
        CategoryPlot plotPdf = (CategoryPlot) chartPdf.getPlot();
        chartPdf.getTitle().visible = false;
        ((BarRenderer) plotPdf.getRenderer()).setBarPainter(new StandardBarPainter());//quitar gradiente            
        ((BarRenderer) plotPdf.getRenderer()).setSeriesPaint(0, Color.lightGray);
        ((BarRenderer) plotPdf.getRenderer()).setDrawBarOutline(true);
        plotPdf.setBackgroundPaint(Color.white);//fondo blanco
        plotPdf.setOutlineVisible(false);//grafico sin borde                                 
        plotPdf.getDomainAxis().setTickLabelFont(new Font("arial", Font.PLAIN, 5));
        plotPdf.getRangeAxis().setTickLabelFont(new Font("arial", Font.PLAIN, 5));
        ((CategoryAxis) plotPdf.getDomainAxis()).setLabel("");
        ((ValueAxis) plotPdf.getRangeAxis()).setLabel("");
        ((CategoryAxis) plotPdf.getDomainAxis()).setCategoryLabelPositions(CategoryLabelPositions.UP_90);//rotar etiquetas
        return chartPdf;
    }

    public void changeSelectAtrribute() {//cambia atributo en el panel Attributes
        createChartPNG();
        RequestContext.getCurrentInstance().update("IdFormDialogsDataAnalisis:IdPanelImageChart");
        RequestContext.getCurrentInstance().update("IdFormDialogsDataAnalisis:IdPanelImageChartLarge");
    }

    public void changeAtributeClass() {//cambia clase en el panel visualizacion
        createChartPNG();
        RequestContext.getCurrentInstance().update("IdFormDialogsDataAnalisis:IdPanelImageChart");
        RequestContext.getCurrentInstance().update("IdFormDialogsDataAnalisis:IdPanelImageChartLarge");
    }

    private Chunk createChunkAndInsertTitle(ArrayList<TitlePdf> titlesList, String txt, com.itextpdf.text.Font font, int pageNumber, String missing, int missingInt, boolean isAttribute) {
        //SE CREA UN Chunk PARA SER INSERTADO EN LA PAGINA, Y SE INTRODUCE EN LA LISTA DE TITULOS DISPONIBLES
        Chunk c = new Chunk(txt, font);
        c.setLocalDestination(txt);
        titlesList.add(new TitlePdf(c, pageNumber, missing, missingInt, isAttribute));
        return c;
    }

    private List<ValueRow> orderValues(AttributeRow attribute) {
        //ESCOGER LOS 10 MAS REPRESENTATIVOS, INCLUIDO NULL, Y EVALUANDO SI EL 100% SON UNICOS        
        ArrayList<ValueRow> valuesOrdered = new ArrayList<>();
        if (attribute.getMissingInt() != 0) {//SE INGRESA EL CONTEO DE NULOS
            valuesOrdered.add(new ValueRow(0, "NULL", attribute.getMissingInt(), 0, "NULL"));
        }
        boolean found;
        for (ValueRow val : attribute.getListValuesData()) {
            if (valuesOrdered.isEmpty()) {
                valuesOrdered.add(val);
            } else {
                found = false;
                for (int j = 0; j < valuesOrdered.size(); j++) {
                    if (val.getCount() >= valuesOrdered.get(j).getCount()) {
                        valuesOrdered.add(j, val);
                        if (valuesOrdered.size() > 10) {
                            valuesOrdered.remove(9);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {//SE AGREGA COMO 
                    valuesOrdered.add(val);
                }
            }

        }

        return valuesOrdered;
    }

    public void createPdf() throws IOException {// CREACION DEL DOCUMENTO PDF(CALCULO DE NUMERO DE PAGINAS)
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        ArrayList<PdfReader> readerList = new ArrayList<>();
        PdfReader reader;
        int idImagePdfCreated = 0;
        ArrayList<TitlePdf> titlesList = new ArrayList<>();

        try {
            removeFile(graphicControlMB.getRealPath() + "dataAnalisis.pdf");
            Document content = new Document(PageSize.A4, 50, 50, 50, 50);
            String path = graphicControlMB.getRealPath() + "dataAnalisis.pdf";

            FileOutputStream os = new FileOutputStream(path);
            PdfWriter contentWriter = PdfWriter.getInstance(content, os);

            PageEventPdf pageEvent = new PageEventPdf(fontCursiva12);
            contentWriter.setPageEvent(pageEvent);
            content.open();

            PdfPTable tableAttribute;
            PdfPTable tableValues;
            BaseColor colorCell;
            PdfPCell cellStatisc;
            PdfPCell cellChart;
            PdfPCell cellValues;
            List<ValueRow> listValuesOrdered = new ArrayList<>();

            content.add(createChunkAndInsertTitle(titlesList, "1. DATA ANALISIS", fontBold12, pageEvent.getPageNumber(), "", 0, false));
            content.add(new Paragraph(" ", fontNormal12));
            content.add(new Paragraph(" ", fontNormal12));
            for (AttributeRow selectAttribute : listAttributes) {
                tableAttribute = new PdfPTable(2);
                tableAttribute.setWidthPercentage(95);
                //--------------------------------------------------------------
                //NOMBRE ATTRIBUTO----------------------------------------------
                //cellStatisc = new PdfPCell();
                //cellStatisc.setBorder(Rectangle.NO_BORDER);
                //cellStatisc.setColspan(2);
//                content.add(createChunkAndInsertTitle(titlesList,
//                        "   1." + (selectAttribute.getIdAttribute() + 1) + " " + selectAttribute.getAttributeName(),
//                        fontBold12, pageEvent.getPageNumber(), selectAttribute.getMissing(), selectAttribute.getMissingInt(), true));
//                tableAttribute.addCell(cellStatisc);
                //----------------------------------------------------------
                //ESTADISTICAS----------------------------------------------
                cellStatisc = new PdfPCell();
                cellStatisc.setBorder(Rectangle.NO_BORDER);
                cellStatisc.addElement(createChunkAndInsertTitle(titlesList,//NOMBRE ATTRIBUTO----------------------------------------------
                        "   1." + (selectAttribute.getIdAttribute() + 1) + " " + selectAttribute.getAttributeName(),
                        fontBold12, pageEvent.getPageNumber(), selectAttribute.getMissing(), selectAttribute.getMissingInt(), true));
                cellStatisc.addElement(new Paragraph("      Type: " + selectAttribute.getType(), fontCursiva10));
                cellStatisc.addElement(new Paragraph("      Distinct values: " + selectAttribute.getDistinct(), fontCursiva10));
                cellStatisc.addElement(new Paragraph("      Missing: " + selectAttribute.getMissing(), fontCursiva10));
                cellStatisc.addElement(new Paragraph("      Unique: " + selectAttribute.getUnique(), fontCursiva10));
                cellStatisc.addElement(new Paragraph(" ", fontCursiva10));
                tableAttribute.addCell(cellStatisc);

                //----------------------------------------------------------
                //TABLA VALOR FRECUENCIA------------------------------------
                cellValues = new PdfPCell();
                cellValues.setBorder(Rectangle.NO_BORDER);
                if (selectAttribute.isIsNominalData()) {//ATRIBUTO NOMINAL----                   
                    tableValues = new PdfPTable(3);
                    tableValues.setWidths(new int[]{1, 7, 2});
                    tableValues.setWidthPercentage(100);
                    tableValues.addCell(createCell(BaseColor.WHITE, "id", Rectangle.TOP));
                    tableValues.addCell(createCell(BaseColor.WHITE, "Value", Rectangle.TOP));
                    tableValues.addCell(createCell(BaseColor.WHITE, "Freq", Rectangle.TOP));
                    listValuesOrdered = orderValues(selectAttribute);
                    for (int i = 0; i < listValuesOrdered.size(); i++) {
                        if (i % 2 != 0) {
                            colorCell = new BaseColor(240, 240, 240);
                        } else {
                            colorCell = BaseColor.WHITE;
                        }
                        if (i != 0) {
                            tableValues.addCell(createCell(colorCell, String.valueOf(listValuesOrdered.get(i).getIdValue()), Rectangle.NO_BORDER));
                            tableValues.addCell(createCell(colorCell, listValuesOrdered.get(i).getLabel(), Rectangle.NO_BORDER));
                            tableValues.addCell(createCell(colorCell, String.valueOf(listValuesOrdered.get(i).getCount()), Rectangle.NO_BORDER));
                        } else {
                            tableValues.addCell(createCell(colorCell, String.valueOf(listValuesOrdered.get(i).getIdValue()), Rectangle.TOP));
                            tableValues.addCell(createCell(colorCell, listValuesOrdered.get(i).getLabel(), Rectangle.TOP));
                            tableValues.addCell(createCell(colorCell, String.valueOf(listValuesOrdered.get(i).getCount()), Rectangle.TOP));
                        }
                    }
                    tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
                    tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
                    tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
                    cellValues.addElement(tableValues);
                } else if (selectAttribute.isIsNumericData()) {//NUMERICO-----
                    tableValues = new PdfPTable(2);
                    tableValues.setWidths(new int[]{4, 6});
                    tableValues.setWidthPercentage(100);
                    tableValues.addCell(createCell(BaseColor.WHITE, "Statics", Rectangle.TOP));
                    tableValues.addCell(createCell(BaseColor.WHITE, "Value", Rectangle.TOP));
                    for (int i = 0; i < selectAttribute.getListValuesData().size(); i++) {
                        if (i % 2 != 0) {
                            colorCell = new BaseColor(240, 240, 240);
                        } else {
                            colorCell = BaseColor.WHITE;
                        }
                        if (i != 0) {
                            tableValues.addCell(createCell(colorCell, String.valueOf(selectAttribute.getListValuesData().get(i).getLabel()), Rectangle.NO_BORDER));
                            tableValues.addCell(createCell(colorCell, selectAttribute.getListValuesData().get(i).getValue(), Rectangle.NO_BORDER));
                        } else {
                            tableValues.addCell(createCell(colorCell, String.valueOf(selectAttribute.getListValuesData().get(i).getLabel()), Rectangle.TOP));
                            tableValues.addCell(createCell(colorCell, selectAttribute.getListValuesData().get(i).getValue(), Rectangle.TOP));
                        }
                    }
                    tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
                    tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
                    cellValues.addElement(tableValues);
                } else {//OTRO TIPO DE DATO-------------------------------------
                    tableValues = new PdfPTable(1);
                    tableValues.setWidthPercentage(100);
                    tableValues.addCell(createCell(BaseColor.WHITE, "Other data type", Rectangle.TOP));
                    tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
                    cellValues.addElement(tableValues);
                }

                //----------------------------------------------------------
                //GRAFICA---------------------------------------------------
                if (selectAttribute.isIsNominalData() || selectAttribute.isIsNumericData()) {//ATRIBUTO NOMINAL----                   

                    removeFile(graphicControlMB.getRealPath() + "imagePdf" + idImagePdfCreated + ".pdf");//eliminar si existe
                    createImageAsPdf(graphicControlMB.getRealPath() + "imagePdf" + idImagePdfCreated + ".pdf", createChartPDF(selectAttribute, listValuesOrdered), 300, 200);//crear pdf con el grafico
                    reader = new PdfReader(graphicControlMB.getRealPath() + "imagePdf" + idImagePdfCreated + ".pdf");//abro el lectos del graficoPdf
                    readerList.add(reader);//adiciono los lectores a una lista para posteriormente cerrarlos                
                    PdfImportedPage page = contentWriter.getImportedPage(reader, 1);
                    Image img = Image.getInstance(page);
                    img.scaleToFit(260f, 300f);
                    cellChart = new PdfPCell(img);
                    cellChart.setRowspan(2);
                    cellChart.setBorder(Rectangle.NO_BORDER);
                    idImagePdfCreated++;
                } else {
                    cellChart = createCell(BaseColor.WHITE, " ", Rectangle.NO_BORDER);
                }
                tableAttribute.addCell(cellChart);
                tableAttribute.addCell(cellValues);
                content.add(tableAttribute);
                content.add(new Paragraph(" ", fontNormal12));
            }
            content.newPage();
            //------------------------------------------------------------------
            //CREACION DE RESUMEN DE NULOS--------------------------------------
            content.add(createChunkAndInsertTitle(titlesList, "2. MISSING SUMMARY", fontBold12, pageEvent.getPageNumber(), "", 0, false));
            ArrayList<TitlePdf> titlesOrdered = new ArrayList<>();
            for (TitlePdf t : titlesList) {//ORDENAR TITULOS QUE CALCULEN PERDIDOS(calculateMissing=true)
                if (t.isCalculateMissing()) {
                    if (titlesOrdered.isEmpty()) {
                        titlesOrdered.add(t);
                    } else {
                        for (int j = 0; j < titlesOrdered.size(); j++) {
                            if (t.getMissingInt() >= titlesOrdered.get(j).getMissingInt()) {
                                titlesOrdered.add(j, t);
                                break;
                            }
                        }
                    }
                }
            }
            Chunk link;
            tableValues = new PdfPTable(3);
            tableValues.setWidths(new int[]{6, 2, 2});
            tableValues.addCell(createCell(BaseColor.WHITE, "Attribute", Rectangle.TOP));
            tableValues.addCell(createCell(BaseColor.WHITE, "Missing", Rectangle.TOP));
            tableValues.addCell(createCell(BaseColor.WHITE, "Page", Rectangle.TOP));
            tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
            tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
            tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));

            for (TitlePdf t : titlesOrdered) {
                link = new Chunk(t.getLocalDestination().getContent(), fontNormal10);
                link.setLocalGoto(t.getLocalDestination().getContent());//enlace IR A   
                tableValues.addCell(createCell(BaseColor.WHITE, link, Rectangle.NO_BORDER));
                link = new Chunk(t.getMissing(), fontNormal10);
                link.setLocalGoto(t.getLocalDestination().getContent());//enlace IR A   
                tableValues.addCell(createCell(BaseColor.WHITE, link, Rectangle.NO_BORDER));
                link = new Chunk(String.valueOf(t.getPageNumber()), fontNormal10);
                link.setLocalGoto(t.getLocalDestination().getContent());//enlace IR A   
                tableValues.addCell(createCell(BaseColor.WHITE, link, Rectangle.NO_BORDER));
            }
            content.add(tableValues);
            content.newPage();
            //------------------------------------------------------------------
            //CREACION DE LA TABLA DE CONTENIDO---------------------------------
            tableValues = new PdfPTable(2);
            tableValues.setWidths(new int[]{8, 2});
            tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
            tableValues.addCell(createCell(BaseColor.WHITE, "Page", Rectangle.TOP));
            tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
            tableValues.addCell(createCell(BaseColor.WHITE, " ", Rectangle.TOP));
            content.add(new Paragraph("TABLE OF CONTENTS", fontBold12));
            content.add(new Paragraph(" ", fontBold12));
            for (TitlePdf t : titlesList) {
                link = new Chunk(t.getLocalDestination().getContent(), fontNormal10);
                link.setLocalGoto(t.getLocalDestination().getContent());//enlace IR A                 
                tableValues.addCell(createCell(BaseColor.WHITE, link, Rectangle.NO_BORDER));
                link = new Chunk(String.valueOf(t.getPageNumber()), fontNormal10);
                link.setLocalGoto(t.getLocalDestination().getContent());//enlace IR A                 
                tableValues.addCell(createCell(BaseColor.WHITE, link, Rectangle.NO_BORDER));
            }
            content.add(tableValues);
            //------------------------------------------------------------------
            //CIERRE DE VARIABLES-----------------------------------------------
            content.close();//CIERRO DOCUMENTO PDF DE ITEXT
            os.close();//CIERRO OUTPUT STREAM USADO PARA ESCRIBIR ARCHIVO
            for (PdfReader r : readerList) {//CIERRO LECTORES USADOS PARA GRAFICOS
                r.close();
            }
            //------------------------------------------------------------------
            //ENVIO LAS RESPUESTA DEL SERVIDOR----------------------------------
            writePdfInResponse(graphicControlMB.getRealPath(), "dataAnalisis.pdf", response);
        } catch (DocumentException | IOException e) {
            printError(e, this);
        }
        facesContext.responseComplete();
    }

    private PdfPCell createCell(BaseColor colorCell, String txt, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(txt, fontNormal10));
        cell.setBackgroundColor(colorCell);
        cell.setBorder(border);
        return cell;
    }

    private PdfPCell createCell(BaseColor colorCell, Chunk c, int border) {
        PdfPCell cell = new PdfPCell();
        cell.addElement(c);
        cell.setBackgroundColor(colorCell);
        cell.setBorder(border);
        return cell;
    }

    private void createFonts() {
        fontNormal10 = createFont(graphicControlMB.getRealPath() + "recursos\\fonts\\cmu_normal.ttf", 9, BaseColor.BLACK);
        fontCursiva10 = createFont(graphicControlMB.getRealPath() + "recursos\\fonts\\cmu_cursiva.ttf", 9, BaseColor.BLACK);
        fontBold10 = createFont(graphicControlMB.getRealPath() + "recursos\\fonts\\cmu_bold.ttf", 9, BaseColor.BLACK);
        fontNormal12 = createFont(graphicControlMB.getRealPath() + "recursos\\fonts\\cmu_normal.ttf", 11, BaseColor.BLACK);
        fontCursiva12 = createFont(graphicControlMB.getRealPath() + "recursos\\fonts\\cmu_cursiva.ttf", 11, BaseColor.BLACK);
        fontBold12 = createFont(graphicControlMB.getRealPath() + "recursos\\fonts\\cmu_bold.ttf", 11, BaseColor.BLACK);
        fontLink = createFont(graphicControlMB.getRealPath() + "recursos\\fonts\\cmu_cursiva.ttf", 11, BaseColor.BLUE);
    }

    public com.itextpdf.text.Font createFont(String urlFont, int size, BaseColor color) {
        BaseFont baseFont = FontFactory.getFont(urlFont,
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                0.8f,
                com.itextpdf.text.Font.NORMAL,
                BaseColor.BLUE).getBaseFont();
        return new com.itextpdf.text.Font(baseFont, size, com.itextpdf.text.Font.NORMAL, color);
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //-------------------- FUNCIONES GET AND SET -------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------       
    public Instances getData() {
        return data;
    }

    public void setData(Instances data) {
        this.data = data;
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<AttributeRow> getListAttributes() {
        return listAttributes;
    }

    public void setListAttributes(ArrayList<AttributeRow> listAttributes) {
        this.listAttributes = listAttributes;
    }

    public AttributeRow getSelectedAtrribute() {
        return selectedAtrribute;
    }

    public void setSelectedAtrribute(AttributeRow selectedAtrribute) {
        this.selectedAtrribute = selectedAtrribute;
    }

    public ArrayList<ValueRow> getListValues() {
        return listValues;
    }

    public void setListValues(ArrayList<ValueRow> listValues) {
        this.listValues = listValues;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
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

    public int getIdAtributeClassSelected() {
        return idAtributeClassSelected;
    }

    public void setIdAtributeClassSelected(int idAtributeClassSelected) {
        this.idAtributeClassSelected = idAtributeClassSelected;
    }

    public StreamedContent getStreamImage() {
        return streamImage;
    }

    public void setStreamImage(StreamedContent streamImage) {
        this.streamImage = streamImage;
    }

    public StreamedContent getStreamImageLarge() {
        return streamImageLarge;
    }

    public void setStreamImageLarge(StreamedContent streamImageLarge) {
        this.streamImageLarge = streamImageLarge;
    }

}
