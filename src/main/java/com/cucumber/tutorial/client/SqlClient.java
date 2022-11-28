package com.cucumber.tutorial.client;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlClient {
    private static final int MAX_ROWS = 100;
    private final String url;
    private final String user;
    private final String pwd;
    private final String driverClassName;

    private Connection conn;
    private PreparedStatement pst;
    private String sql;

    public SqlClient(String url, String user, String pwd, String driverClassName) {
        this.url = url;
        this.user = user;
        this.pwd = pwd;
        this.driverClassName = driverClassName;
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect() throws SQLException {
        conn = DriverManager.getConnection(url, user, pwd);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (conn == null) {
            throw new RuntimeException("Connection not initialised");
        }
        if (pst != null) {
            pst.close();
        }
        this.sql = sql;
        pst = conn.prepareStatement(sql);
        pst.setMaxRows(MAX_ROWS);
        return pst;
    }

    public List<Map<String, Object>> executeQueryAndGetRsAsList() throws SQLException {
        List<Map<String, Object>> tableData = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = pst.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columns; i++) {
                    rowData.put(md.getColumnLabel(i), rs.getObject(i));
                }
                tableData.add(rowData);
            }
            return tableData;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ResultSet executeQuery() throws SQLException {
        return pst.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        int affected = 0;
        affected = pst.executeUpdate();
        return affected;
    }

    public Connection getConnection() {
        return conn;
    }

    public void close() throws SQLException {
        if (pst != null) {
            pst.close();
        }
        if (conn != null) {
            conn.close();
        }
    }
}