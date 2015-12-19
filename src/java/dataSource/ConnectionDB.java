/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataSource;

import util.DinamicTable;
import util.Node;
import util.UtilFunctions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import org.primefaces.context.RequestContext;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;
import weka.core.Instances;

/**
 *
 * @author santos
 */
public class ConnectionDB extends UtilFunctions {

    private ArrayList<String> selectedRowDataTable = new ArrayList<>();
    private UploadedFile file;
    private String newFileName = "";
    private DinamicTable dinamicTable = new DinamicTable();
    private Node currentNode;
    public Connection conn;
    private Statement st;
    private ResultSet rs;
    private String msj;
    private String user = "";
    private String db = "";
    private String password = "";
    private String server = "";
    private String port = "";
    private String url = "";
    private String sql = "";
    private boolean showMessages = true;//determinar si mostrar o no los mensajes de error
    private MenuModel menuModel = new DefaultMenuModel();
    private DefaultSubMenu submenu = new DefaultSubMenu("Options");
    private Instances data = null;

    public ConnectionDB(Node p) {
        currentNode = p;
        reset();
    }

    public final void reset() {
        menuModel = new DefaultMenuModel();
        submenu = new DefaultSubMenu("Options");
        submenu.addElement(createDefaultMenuItem("Configure", Boolean.FALSE, "PF('wvDlgConfConnection').show(); PF('wvContextMenu').hide();", null, "Settings connection", "", "fa fa-cogs"));
        submenu.addElement(createDefaultMenuItem("View", Boolean.TRUE, "PF('wvDlgConnectionDataSet').show(); PF('wvContextMenu').hide();", null, "Connection data set", ":IdFormDialogsConnectionDB", "fa fa-search"));
        submenu.addElement(createDefaultMenuItem("Connect", Boolean.TRUE, "cambiarConectando('true'); PF('wvContextMenu').hide();", null, "Connect to other node", "", "fa fa-link"));
        submenu.addElement(createDefaultMenuItem("Disconnect", Boolean.FALSE, "PF('wvContextMenu').hide();", "#{graphicControlMB.disconnectSelectedNode}", "Disconnect node", "", "fa fa-unlink"));        
        submenu.addElement(createDefaultMenuItem("Remove", Boolean.FALSE, "eliminarNodo();  PF('wvContextMenu').hide();", null, "Remove this node", "", "fa fa-remove"));
        menuModel.addElement(submenu);
    }

    public void changeDisabledOption(String value, boolean disabled) {//cambia estado de uno de los items
        for (MenuElement item : submenu.getElements()) {
            DefaultMenuItem menuItem = (DefaultMenuItem) item;
            if (menuItem.getValue().toString().compareTo(value) == 0) {
                menuItem.setDisabled(disabled);
                break;
            }
        }
        menuModel = new DefaultMenuModel();
        menuModel.addElement(submenu);
    }

    @PreDestroy
    public synchronized void disconnectDB() {
        try {
            if (!conn.isClosed()) {
                conn.close();
                System.out.println("Cerrada conexion a base de datos " + url + " ... OK  " + this.getClass().getName());
            }
        } catch (Exception e) {
            System.out.println("Error al cerrar conexion a base de datos " + url + " ... " + e.toString());
        }
    }

    public boolean connectToDb() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("org.postgresql.Driver").newInstance();// seleccionar SGBD
                url = "jdbc:postgresql://" + server + "/" + db;
                conn = DriverManager.getConnection(url, user, password);//conectarse a bodega de datos                
                System.out.println("Inicia conexion a base de datos " + url + " ... OK  " + this.getClass().getName());
                printMessage("Correct", "Connection open", FacesMessage.SEVERITY_INFO);
                currentNode.setStateNode("_a");
            } else {
                printMessage("Correct", "Connection is already open", FacesMessage.SEVERITY_INFO);
            }
            return true;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            printMessage("Error", "Connection open " + e.toString(), FacesMessage.SEVERITY_ERROR);
            System.out.println(e.toString() + " --- Clase: " + this.getClass().getName());
            msj = e.toString();
            return false;
        }
    }

    public void excecuteSql() {
        currentNode.resetChildrenNodes();
        rs = consult(sql);
        if (rs != null) {
            try {
                int numCols = rs.getMetaData().getColumnCount();
                ArrayList<String> rowFileData;
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<ArrayList<String>> listOfRecords = new ArrayList<>();
                for (int i = 0; i < numCols; i++) {
                    titles.add(rs.getMetaData().getColumnName(i + 1));
                }
                while (rs.next()) {
                    rowFileData = new ArrayList<>();
                    for (int i = 0; i < numCols; i++) {
                        rowFileData.add(rs.getString(i + 1));
                    }
                    listOfRecords.add(rowFileData);
                    System.out.println(Arrays.toString(rowFileData.toArray()));
                }
                dinamicTable = new DinamicTable(listOfRecords, titles);
                RequestContext.getCurrentInstance().execute("PF('wvDlgConnectionDataSet').hide();");
                currentNode.getDataPlainText();

            } catch (SQLException ex) {
                printMessage("Error", ex.getMessage(), FacesMessage.SEVERITY_ERROR);
            }
        }
    }

    public ResultSet consult(String query) {
        msj = "";
        try {
            if (conn != null) {
                st = conn.createStatement();
                rs = st.executeQuery(query);
                printMessage("Correcto", "Consuta ejecutada: " + query, FacesMessage.SEVERITY_INFO);
                return rs;
            } else {
                printMessage("Error", "There don't exist connection", FacesMessage.SEVERITY_ERROR);
                msj = "There don't exist connection";
                return null;
            }
        } catch (SQLException e) {
            printMessage("Error", e.getMessage(), FacesMessage.SEVERITY_ERROR);
            System.out.println("Error: " + e.toString() + " - Clase: " + this.getClass().getName());
            msj = "ERROR: " + e.getMessage() + "---- CONSULTA:" + query;
            return null;
        }
    }

    public int non_query(String query) {
        msj = "";
        int reg;
        reg = 0;
        try {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    reg = stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            if (showMessages) {
                System.out.println("Error: " + e.toString() + " -- Clase: " + this.getClass().getName() + " -  " + query);
            }
            msj = "ERROR: " + e.getMessage();
        }
        return reg;
    }

    public String insert(String Tabla, String elementos, String registro) {
        msj = "";
        int reg = 1;
        String success;
        try {
            if (conn != null) {
                st = conn.createStatement();
                st.execute("INSERT INTO " + Tabla + " (" + elementos + ") VALUES (" + registro + ")");
                if (reg > 0) {
                    success = "true";
                } else {
                    success = "false";
                }
                st.close();
            } else {
                success = "false";
                msj = "ERROR: There don't exist connection...";
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.toString() + " --- Clase: " + this.getClass().getName());
            System.out.println("numero: " + e.getErrorCode());
            success = e.getMessage();
            msj = "ERROR: " + e.getMessage();
        }
        return success;
    }

    public void remove(String Tabla, String condicion) {
        msj = "";

        int reg;
        try {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + Tabla + " WHERE " + condicion)) {
                    reg = stmt.executeUpdate();
                    if (reg > 0) {
                    } else {
                    }
                }
            } else {
                msj = "ERROR: There don't exist connection";
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.toString() + " ---- Clase: " + this.getClass().getName());
            msj = "ERROR: " + e.getMessage();
        }
    }

    public void update(String Tabla, String campos, String donde) {
        msj = "";
        int reg;
        try {
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE " + Tabla + " SET " + campos + " WHERE " + donde)) {
                    reg = stmt.executeUpdate();
                    if (reg > 0) {
                    } else {
                    }
                }
            } else {
                msj = "ERROR: There don't exist connection";
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.toString() + " ----- Clase: " + this.getClass().getName());
            msj = "ERROR: " + e.getMessage();
        }
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    //-------------------- FUNCIONES GET AND SET -------------------------------
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------    
    public DinamicTable getDinamicTable() {
        return dinamicTable;
    }

    public void setDinamicTable(DinamicTable dinamicTable) {
        this.dinamicTable = dinamicTable;
    }

    public ArrayList<String> getSelectedRowDataTable() {
        return selectedRowDataTable;
    }

    public void setSelectedRowDataTable(ArrayList<String> selectedRowDataTable) {
        this.selectedRowDataTable = selectedRowDataTable;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection con) {
        this.conn = con;
    }

    public String getMsj() {
        return msj;
    }

    public void setMsj(String mens) {
        msj = mens;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

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

}
