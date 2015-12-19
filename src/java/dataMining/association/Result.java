/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataMining.association;

/**
 *
 * @author santos
 */
public class Result {

    String nameResult = "";
    String txtResult = "";
    String id = "";
    String treeGraph = "";

    public Result(String id, String nameResult, String txtResult,String treeGraph) {
        this.id = id;
        this.nameResult = nameResult;
        this.txtResult = txtResult;
        this.treeGraph = treeGraph;
    }

    public String getNameResult() {
        return nameResult;
    }

    public void setNameResult(String nameResult) {
        this.nameResult = nameResult;
    }

    public String getTxtResult() {
        return txtResult;
    }

    public void setTxtResult(String txtResult) {
        this.txtResult = txtResult;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTreeGraph() {
        return treeGraph;
    }

    public void setTreeGraph(String treeGraph) {
        this.treeGraph = treeGraph;
    }

}
