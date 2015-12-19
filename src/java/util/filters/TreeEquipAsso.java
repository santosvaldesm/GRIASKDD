package util.filters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import weka.core.Instance;
import weka.core.Instances;

public class TreeEquipAsso {

    private NodeTreeEquipAsso root = null;
    ArrayList<NodeTreeEquipAsso> queue = new ArrayList<>();//cola
    private int numInstances = 0;
    private DecimalFormat df = null;
    int nodesCreated = 0;
    int maxLevel = 2;
    double support;
    double confidence;
    double maxNumRules;
    int classIndex;

    public TreeEquipAsso(int maxLevel, double support, double confidence, int maxNumRules, int classIndex) {
        df = new DecimalFormat("0.00");
        root = new NodeTreeEquipAsso();
        root.setIdNodeTreeEquipAsso(0);
        this.maxLevel = maxLevel;
        this.support = support;
        this.confidence = confidence;
        this.maxNumRules = maxNumRules;
        this.classIndex = classIndex;
    }

    int inserciones = 0;

    private void insertCombinationInTree(ArrayList<NodeTreeEquipAsso> combination, Instances data) {
        //INSERTAR UNA COMBINACION EN EL ARBOL //CUANDO NO PUEDE INSERTAR EMPIEZA A CONSTRUIR NODOS     
        inserciones++;
        String str = "";
        for (NodeTreeEquipAsso combination1 : combination) {
            str = str + "(" + combination1.getValue() + ")";
        }
        //System.out.println("COMBINACION INSERTADA "+inserciones+": " + str);

        NodeTreeEquipAsso parentNode = root;
        NodeTreeEquipAsso currentNode = parentNode.getFirstChild();
        boolean combiantionFound = true;
        for (int i = 0; i < combination.size(); i++) {
            while (true) {//CICLO PARA BUSCAR SI UN HIJO DE parentNode ES IGUAL A combination[i]
                if (currentNode == null) {
                    break;
                }
                if (currentNode.getValue().compareTo(combination.get(i).getValue()) == 0) {
                    break;
                } else {
                    currentNode = currentNode.getNodeNext();
                }
            }
            if (currentNode != null) {//EL VALOR FUE ENCONTRADO EN LOS HIJOS DE parentNode, SE PROSIGUE TOMANDO EL PRIMER HIJO
                parentNode = currentNode;
                currentNode = currentNode.getFirstChild();
            } else {//NO SE ENCONTRO VALOR HAY QUE INSERTARLO EN EL ARBOL ASI COMO LOS SIGUIENTES DE combination                
                combiantionFound = false;
                for (int j = i; j < combination.size(); j++) {
                    currentNode = new NodeTreeEquipAsso();
                    currentNode.setIdNodeTreeEquipAsso(++nodesCreated);
                    currentNode.setAttributeName(data.attribute(j).name());
                    currentNode.setAttributeIndex(data.attribute(j).index());
                    currentNode.setValue(combination.get(j).getValue());
                    currentNode.increaseCount();//quede en 1;
                    parentNode.addChildNode(currentNode);
                    parentNode = currentNode;
                }
                break;
            }
        }
        if (combiantionFound) {//SE ENCONTRO LA COMBINACION
            parentNode.increaseCount();//SE INCREMENTA A PADRE POR QUE currentNode=null YA QUE ESTE ULTIMO AVANZO
        }
    }

    private NodeTreeEquipAsso popQueue() {//sacar de pila
        NodeTreeEquipAsso nodeReturn = queue.get(queue.size() - 1);
        queue.remove(queue.size() - 1);
        return nodeReturn;
    }

    public void determineFrequentItemSets(Instances data) {//DETERMINA TODOS LOS ITEMSETS FRECUENTES
        ArrayList<ArrayList<NodeTreeEquipAsso>> combinations;
        numInstances = data.numInstances();
        int countProcess = 0;
        for (Instance selectInstance : data) {
            combinations = createCombinationsInstance(convertInstanceToArray(selectInstance));
            for (ArrayList<NodeTreeEquipAsso> combination : combinations) {
                insertCombinationInTree(combination, data);
            }
            //countProcess++;
            //    System.out.println("COMPLETADOS PROGRESO " + df.format(countProcess * 100 / data.numInstances()));

        }
        //System.out.println(printTreeCombinations());
    }

    private int countCombinationByArray(ArrayList<NodeTreeEquipAsso> combination, String nameCombination) {//BUSCAR EL CONTEO DE UNA COMBINACION
        NodeTreeEquipAsso currentNode = root;
        String out = "";
        for (NodeTreeEquipAsso nodeValue : combination) {
            if (currentNode == null) {
                break;
            } else {
                currentNode = currentNode.getFirstChild();
            }
            while (true) {
                if (currentNode == null) {
                    //for (NodeTreeEquipAsso nodeValue2 : combination) {
                    //    out = out + nodeValue2.getAttributeName() + "(" + nodeValue2.getValue() + ") ";
                    //}
                    //System.err.println("ERROR: No se encontro combinacion " + nameCombination + ": " + out);
                    //CONSECUENTE PUEDE SER -1(NO ESTA EN ARBOL) POR QUE SE GENERA A PARTIR DE LOS QUE NO TENGA ANTECEDENTE SE SACAN DE ITEMSET FRECUENTE
                    //OSEA QUE PUEDE GENERAR UNA COMBINACION QUE NO VINO EN LOS REGISTROS POR ELLO NO SE ENCUENTRA EN EL ARBOL
                    break;
                }
                if (currentNode.getValue().compareTo(nodeValue.getValue()) == 0) {
                    break;
                } else {
                    currentNode = currentNode.getNodeNext();
                }
            }
        }
        if (currentNode != null) {
            return currentNode.getCount();
        } else {
            return -1;
        }
    }

    //IMPRESION DEL ARBOL MEDIANTE RECORRIDO INORDEN
    public String printTreeInOrder() {
        String strReturn = "\nSTART TREE INORDER-------------------------\n";

        queue = new ArrayList<>();
        NodeTreeEquipAsso selectNode;
        queue.add(root);//root no es nodo
        while (!queue.isEmpty()) {
            selectNode = popQueue();//sacar de pila
            strReturn = strReturn + "\n ID:" + selectNode.getIdNodeTreeEquipAsso() + "(" + selectNode.getValue() + ") \t\t";
            if (selectNode.getNodeParent() != null) {
                strReturn = strReturn + " ID_PARENT:" + selectNode.getNodeParent().getIdNodeTreeEquipAsso() + " VALUE:" + selectNode.getValue() + " COUNT:" + selectNode.getCount();
            }
            if (!selectNode.getNodeChilds().isEmpty()) {//TIENE HIJO
                queue.add(selectNode.getFirstChild());
            }
            if (selectNode.getNodeNext() != null) {//TIENE HERMANO
                queue.add(selectNode.getNodeNext());
            }
        }
        strReturn = strReturn + "\nEND TREE INORDER-------------------------\n";
        return strReturn;
    }

    //IMPRESION DE LAS COMBINACIONES QUE REPRESENTA EL ARBOL
    public String printLargeItemSets() {
        //String strReturn = "\nSTART TREE COMBINATIONS-------------------------\n";
        String s;
        int numValuesCombination;
        ArrayList<ArrayList<String>> combinationsByLevel = new ArrayList<>();
        for (int i = 0; i < maxLevel; i++) {
            combinationsByLevel.add(new ArrayList<String>());
        }

        queue = new ArrayList<>();
        NodeTreeEquipAsso selectNode;
        NodeTreeEquipAsso auxNode;
        queue.add(root);//root no es nodo
        while (!queue.isEmpty()) {
            selectNode = popQueue();//sacar de pila            
            if (!selectNode.getNodeChilds().isEmpty()) {//TIENE HIJO
                queue.add(selectNode.getFirstChild());
            } else {//SI NO TIENE UN HIJO ES POR QUE ES UNA HOJA
                auxNode = selectNode;
                s = "" + auxNode.getCount();
                numValuesCombination = 0;
                while (auxNode != null) {
                    if (auxNode.getIdNodeTreeEquipAsso() != 0) {//NO TOMAR DATO DE NODO ROOT(NIVEL 0)
                        if (!auxNode.isMissing()) {
                            numValuesCombination++;
                            s = auxNode.getAttributeName() + "=" + auxNode.getValue() + " " + s;
                        }
                    }
                    auxNode = auxNode.getNodeParent();
                }
                combinationsByLevel.get(numValuesCombination - 1).add(s);
            }
            if (selectNode.getNodeNext() != null) {//TIENE HERMANO
                queue.add(selectNode.getNodeNext());
            }
        }
        String strReturn = "Generated sets of large itemsets:\n\n";
        for (int i = 0; i < combinationsByLevel.size(); i++) {
            strReturn = strReturn + "Size of large itemsets L(" + (i + 1) + "): " + combinationsByLevel.get(i).size() + "\n\n";
            strReturn = strReturn + "Large Itemsets L(" + (i + 1) + "):\n";
            for (String get : combinationsByLevel.get(i)) {
                strReturn = strReturn + get + "\n";
            }
            strReturn = strReturn + "\n";
        }
        return strReturn;
    }

    private ArrayList<NodeTreeEquipAsso> convertInstanceToArray(Instance selectInstance) {//CONVERTIR UNA INSTACIA DE WEKA EN UN ARREGLO
        ArrayList<NodeTreeEquipAsso> instanceValues = new ArrayList<>();//String[selectInstance.numValues()];
        for (int i = 0; i < selectInstance.numValues(); i++) {
            NodeTreeEquipAsso newNode = new NodeTreeEquipAsso();
            newNode.setAttributeIndex(selectInstance.attribute(i).index());
            newNode.setAttributeName(selectInstance.attribute(i).name());
            if (!selectInstance.isMissing(i)) {
                newNode.setValue(selectInstance.stringValue(i));
            } else {
                newNode.setValue("");
            }
            instanceValues.add(newNode);
        }
        return instanceValues;
    }

    private ArrayList<NodeTreeEquipAsso> convertNodeToArray(NodeTreeEquipAsso selectNode) {//A PARTIR DE UN NODE(ES HOJA) DEL ARBOL SACAR UN ARREGLO(SUBIENDO POR LOS PADRES)
        ArrayList<NodeTreeEquipAsso> arrayValues = new ArrayList<>();
        arrayValues.add(selectNode);
        NodeTreeEquipAsso nodeParent = selectNode.getNodeParent();
        while (nodeParent != null) {
            arrayValues.add(0, nodeParent);
            nodeParent = nodeParent.getNodeParent();
            if (nodeParent.getIdNodeTreeEquipAsso() == 0) {
                break;
            }
        }
        return arrayValues;
    }

    private ArrayList<ArrayList<NodeTreeEquipAsso>> createCombinationsInstance(ArrayList<NodeTreeEquipAsso> instanceValues) {//CREA TODAS LAS COMBIANACIONES A PARTIR DE UN ARRAY Y DE NIVEL IGUAL O INFERIOR A maxLevel
        int numValues = instanceValues.size();
        int currentLevel = 2;//NUMERO DE ELEMENTOS QUE SE DEBEN TOMAR(SE INICIA CON DOS)
        int initialPos = 0;//posicion inicial tomada
        int endPosition;//POSICION DONDE TERMINAN LOS TOMADOS Y EL PIVOTE(OSEA DESDE DONDE TOCA COMPLETAR CON VACIOS)
        int numTakeElemnts;//NUMERO DE ELEMNTOS A TOMADOS( SIN PIVOTE OSEA MAXIMO currentLevel - 1 )
        int pivot;
        ArrayList<ArrayList<NodeTreeEquipAsso>> combinationsList = new ArrayList<>();//LISTA DE COMBIANACIONES (SE RETORNARA)
        ArrayList<NodeTreeEquipAsso> combination;//NUEVA COMBINACION A AGREGAR A LA LISTA DE COMBINACIONES
        ArrayList<NodeTreeEquipAsso> combinationTmp;//COMBINACION FORMADA POR LOS ELEMENTOS A TOMAR(SIN HABER MOVIDO EL PIVOTE)        

        boolean continueProcess = true;

        for (int i = 0; i < numValues; i++) {//ARMAR COMBINACION DE NIVEL 1                    
            combinationTmp = new ArrayList<>();
            if (!instanceValues.get(i).isMissing()) {
                for (int j = 0; j < numValues; j++) {
                    if (j == i) {
                        combinationTmp.add(instanceValues.get(i));
                    } else {
                        combinationTmp.add(new NodeTreeEquipAsso());
                    }
                }
                combinationsList.add(combinationTmp);//AGREGAR COMBINACION DE NIVEL 1
            }
        }

        while (continueProcess) {
            combinationTmp = new ArrayList<>();
            numTakeElemnts = 0;
            if (instanceValues.get(initialPos).isMissing()) {//esta vacio
                if (initialPos + 1 == numValues) {//se debe aumentar el nivel
                    if (currentLevel < numValues) {
                        initialPos = 0;
                        currentLevel++;
                        if (currentLevel > maxLevel) {
                            continueProcess = false;
                        }
                    } else {
                        continueProcess = false;//SE FINALIZA PROCESO
                    }
                } else {
                    initialPos++;
                }
            } else {
                if (initialPos + 1 == numValues) {//se debe aumentar el nivel
                    if (currentLevel < numValues) {
                        initialPos = 0;
                        currentLevel++;
                        if (currentLevel > maxLevel) {
                            continueProcess = false;
                        }
                    } else {
                        continueProcess = false;//SE FINALIZA PROCESO
                    }
                } else {
                    for (int i = 0; i < initialPos; i++) {//SE LLENA DE VACIOS HASTA initialPos
                        combinationTmp.add(new NodeTreeEquipAsso());
                    }
                    pivot = -1;
                    for (int i = initialPos; i < numValues; i++) {//SE AGREGAN ELEMENTOS A TOMAR
                        combinationTmp.add(instanceValues.get(i));
                        if (!instanceValues.get(i).isMissing()) {//NO ESTA VACIO                        
                            numTakeElemnts++;
                        }
                        if (numTakeElemnts == currentLevel - 1) {//SE COMPLETO EL NUMERO DE ELEMENTOS A TOMAR(currentLevel-1  EL ULTIMO ES PIVOTE)
                            if (i < numValues - 1) {//SE PUEDE ASIGNAR PIVOTE
                                pivot = i + 1;
                            }
                            break;
                        }
                    }
                    if (pivot == -1) {//SI ES -1 YA NO SE PUEDE REALIZAR MAS COMBINACIONES
                        if (initialPos + 1 == numValues) {//SE DEBE AUMENTAR EL NIVEL
                            if (currentLevel < numValues) {
                                initialPos = 0;
                                currentLevel++;
                                if (currentLevel > maxLevel) {
                                    continueProcess = false;
                                }
                            } else {
                                continueProcess = false;//SE FINALIZA PROCESO
                            }
                        } else {//SOLO SE AUMENTA LA POSICION INICIAL
                            initialPos++;
                        }
                    } else {//SE PUEDE REALIZAR COMBINACIONES MOVIENDO EL PIVOTE
                        while (true) {
                            combination = new ArrayList();
                            combination.addAll(combinationTmp);
                            endPosition = -1;
                            for (int i = combination.size(); i < pivot; i++) {//SE COMPLETA CON VACIOS HASTA DONDE ESTE PIVOT
                                combination.add(new NodeTreeEquipAsso());
                            }
                            for (int i = pivot; i < numValues; i++) {//SE AGREGA MOVIENDO EL PIVOTE
                                if (!instanceValues.get(i).isMissing()) {
                                    combination.add(instanceValues.get(i));
                                    endPosition = i;
                                    break;
                                } else {
                                    combination.add(new NodeTreeEquipAsso());
                                    pivot++;
                                }
                            }
                            if (endPosition == -1) {//NO SE PUDO MOVER EL PIVOTE                            
                                initialPos++;//AUMENTAR LA POSICION INICIAL
                                break;//DEJAR DE CALCULAR COMBINACIONES 
                            } else {//SE PUDO MOVER EL PIVOTE
                                for (int i = endPosition + 1; i < numValues; i++) {//SE COMPLETA LA COMBINACION CON VACIOS
                                    combination.add(new NodeTreeEquipAsso());
                                }
                                combinationsList.add(combination);
                                pivot++;//dar 15 veces para llegar aqui
                            }
                        }
                    }
                }
            }
        }
        return combinationsList;
    }

    public String generateRules() {
        //soporte     = apariciones con respecto a todos los resultados
        //confianza   = se aplica a la regla(apariciones itemsetFrecuente/apariciones antecedente)
        double minSupport = numInstances * (support / 100);
        double currentSupport;
        String rules = "Rules found:\n\n";
        ArrayList<ArrayList<NodeTreeEquipAsso>> combinations;
        ArrayList<NodeTreeEquipAsso> itemSetFrecuent = new ArrayList<>();
        ArrayList<NodeTreeEquipAsso> consequent;
        int countAntecedent;
        int countConsecuent = 0;
        int countItemSetFrecuent;
        double calculatedConfidence;
        int sizeConsecuent;
        boolean continueProcces;
        String rule;
        String antecedentStr;
        String consequentStr;
        int rulesGenerated = 0;//Cantidad de reglas generadas debe ser menor a maxNumRules+1
        queue = new ArrayList<>();
        NodeTreeEquipAsso selectNode;
        queue.add(root);//root no es nodo

        while (!queue.isEmpty()) {
            continueProcces = true;
            selectNode = popQueue();//sacar de pila
            if (!selectNode.getNodeChilds().isEmpty()) {//TIENE HIJO
                queue.add(selectNode.getFirstChild());
            } else {//SI NO TIENE UN HIJO ES POR QUE ES UNA HOJA                
                if (continueProcces) {//DETERMINO CUALES SUPERA EL SOPORTE MINIMO
                    if (selectNode.getCount() < minSupport) {
                        continueProcces = false;
                    }
                }
                if (continueProcces) {//SI SE DEBE EVALUAR LA CLASE, EL VALOR NO DEBE SER MISSING EN LA COMBINACION
                    itemSetFrecuent = convertNodeToArray(selectNode);
                    if (classIndex != -1) {//SE DEBE MANEJAR CLASE
                        if (itemSetFrecuent.get(classIndex).isMissing()) {//LA COMBINACION NO CONTIENE LA CLASE
                            continueProcces = false;
                        }
                    }
                }
                if (continueProcces) {
                    currentSupport = (selectNode.getCount() * 100 / numInstances);
                    combinations = createCombinationsInstance(itemSetFrecuent);//GENERO COMBIACIONES DEL ITEMSET FRECUENTE
                    for (ArrayList<NodeTreeEquipAsso> antecedent : combinations) {//de cada combinacion sale una regla                        
                        consequent = new ArrayList<>();
                        sizeConsecuent = 0;
                        for (int i = 0; i < itemSetFrecuent.size(); i++) {
                            if (antecedent.get(i).isMissing()) {
                                consequent.add(itemSetFrecuent.get(i));
                                if (!itemSetFrecuent.get(i).isMissing()) {
                                    sizeConsecuent++;
                                }
                            } else {
                                consequent.add(new NodeTreeEquipAsso());
                            }
                        }
                        if (sizeConsecuent > 0) {//SE AGREGA REGLA SI HAY CONSECUENTE
                            continueProcces = true;
                            if (classIndex != -1) {//SI HAY CLASE: CONSECUENTE DEBE SER 1 Y NO ESTAR VACIO EN LA POSICION DE classIndex
                                if (sizeConsecuent != 1) {//CONSECUENTE DEBE SER 1
                                    continueProcces = false;
                                }
                                if (consequent.get(classIndex).isMissing()) {//CONSECUENTE NO CONTIENE LA CLASE
                                    continueProcces = false;
                                }
                            }
                            if (continueProcces) {
                                countConsecuent = countCombinationByArray(consequent, "Consecuent");//determino los conteos de consecuente 
                                //CONSECUENTE PUEDE SER -1(NO ESTA EN ARBOL) POR QUE SE GENERA A PARTIR DE LOS QUE NO TENGA ANTECEDENTE SE SACAN DE ITEMSET FRECUENTE
                                //OSEA QUE PUEDE GENERAR UNA COMBINACION QUE NO VINO EN LOS REGISTROS POR ELLO NO SE ENCUENTRA EN EL ARBOL
                                if (countConsecuent == -1) {
                                    continueProcces = false;
                                }
                            }
                            if (continueProcces) {
                                countAntecedent = countCombinationByArray(antecedent, "Antecedent");//determino los conteos de antecedente 

                                countItemSetFrecuent = countCombinationByArray(itemSetFrecuent, "itemSetFrecuent");//determino los conteos de itemSetFrecuent 
                                calculatedConfidence = (countItemSetFrecuent * 100) / countAntecedent;
                                if (calculatedConfidence >= confidence) {//SE AGREGA A LAS REGLAS SI SUPERA LA CONFIANZA
                                    rulesGenerated++;
                                    antecedentStr = rulesGenerated + ". ";
                                    for (NodeTreeEquipAsso ant : antecedent) {
                                        if (!ant.isMissing()) {
                                            antecedentStr = antecedentStr + ant.getAttributeName() + "=" + ant.getValue() + " , ";
                                        }
                                    }
                                    antecedentStr = antecedentStr.substring(0, antecedentStr.length() - 2);
                                    antecedentStr = antecedentStr + " " + countAntecedent + " ==> ";
                                    consequentStr = "";
                                    for (NodeTreeEquipAsso con : consequent) {
                                        if (!con.isMissing()) {
                                            consequentStr = consequentStr + con.getAttributeName() + "=" + con.getValue() + " , ";
                                        }
                                    }
                                    consequentStr = consequentStr.substring(0, consequentStr.length() - 2);
                                    rule = antecedentStr + consequentStr + "" + countConsecuent + " Sup. " + df.format(currentSupport / 100) + " Conf. " + df.format(calculatedConfidence / 100) + " (" + countItemSetFrecuent + "/" + countAntecedent + ")\n";
                                    rules = rules + rule;
                                    if (rulesGenerated >= maxNumRules) {//SE COMPLETARON EL NUMERO DE REGLAS QUE SOLICITO EL USUARIO
                                        selectNode = null;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (selectNode == null) {
                break;
            }
            if (selectNode.getNodeNext() != null) {//TIENE HERMANO
                queue.add(selectNode.getNodeNext());
            }
        }
        return rules;
    }

}
