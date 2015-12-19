/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.filters;

import java.util.ArrayList;

/**
 *
 * @author santos
 */
public class NodeTreeEquipAsso {

    private int idNodeTreeEquipAsso = -1;                //identificador unico dentro del arbol(-1 para nodos vacios)
    private NodeTreeEquipAsso nodeNext = null;                 //siguiente hermano
    private NodeTreeEquipAsso nodePrevious = null;             //anterior hermano
    private NodeTreeEquipAsso nodeParent = null;               //padre
    private ArrayList<NodeTreeEquipAsso> nodeChilds = new ArrayList<>();     //hijos
    private String value = "";
    private int count = 0;                              //cantidad de apariciones (confianza)
    private String attributeName = "";
    private int attributeIndex = 0;                 //seria lo mismo que nivel en el arbol

    public NodeTreeEquipAsso() {
    }

    public boolean isMissing() {
        return value.length() == 0;
    }

    public void increaseCount() {
        count++;
    }

    public void addChildNode(NodeTreeEquipAsso child) {
        if (!nodeChilds.isEmpty()) {//tiene hijos, se debe conectar a ultimo hijo
            NodeTreeEquipAsso nodeBrother = nodeChilds.get(nodeChilds.size() - 1);
            nodeBrother.setNodeNext(child);
            child.setNodePrevious(nodeBrother);
        }
        child.setNodeParent(this);
        nodeChilds.add(child);

    }

    public NodeTreeEquipAsso getFirstChild() {
        if (!nodeChilds.isEmpty()) {
            return nodeChilds.get(0);
        } else {
            return null;
        }
    }

    public NodeTreeEquipAsso getNodeNext() {
        return nodeNext;
    }

    public void setNodeNext(NodeTreeEquipAsso nodeNext) {
        this.nodeNext = nodeNext;
    }

    public NodeTreeEquipAsso getNodePrevious() {
        return nodePrevious;
    }

    public void setNodePrevious(NodeTreeEquipAsso nodePrevious) {
        this.nodePrevious = nodePrevious;
    }

    public NodeTreeEquipAsso getNodeParent() {
        return nodeParent;
    }

    public void setNodeParent(NodeTreeEquipAsso nodeParent) {
        this.nodeParent = nodeParent;
    }

    public ArrayList<NodeTreeEquipAsso> getNodeChilds() {
        return nodeChilds;
    }

    public void setNodeChilds(ArrayList<NodeTreeEquipAsso> nodeChilds) {
        this.nodeChilds = nodeChilds;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public int getAttributeIndex() {
        return attributeIndex;
    }

    public void setAttributeIndex(int attributeIndex) {
        this.attributeIndex = attributeIndex;
    }

    public int getIdNodeTreeEquipAsso() {
        return idNodeTreeEquipAsso;
    }

    public void setIdNodeTreeEquipAsso(int idNodeTreeEquipAsso) {
        this.idNodeTreeEquipAsso = idNodeTreeEquipAsso;
    }

}
