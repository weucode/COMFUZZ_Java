package com.jvmfuzzing.mutation_by_all;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

public class SelectDB {
    PreparedStatement ps = null;
    ResultSet rs = null;
    Connection conn = null;
    String forconnect = "";
    String needforString = "";
    List<String> needfulparmList = new ArrayList<String>();
    List<String> bin_par_trueList = new ArrayList<String>();
    List<String> bin_expr_trueList = new ArrayList<String>();
    List<String> bin_par_falseList = new ArrayList<String>();
    List<String> bin_expr_falseList = new ArrayList<String>();
    List<HashMap<String, String>> needsetVarName_list = new ArrayList<>();

    public List<String> select() {
        List<String> results = new ArrayList<String>();
        try {
            String driver = "com.mysql.cj.jdbc.Driver";
            String url = "jdbc:MySQL://comfort_mysql:3306/JVMFuzzing?useUnicode=true&characterEncoding=utf8";
            String username = "root";
            String password = "mysql123";
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
            // String sql1 = "select For_content,Variable_Type_for from table_insert_new
            // WHERE ID ="+ID;
            String sql1 = "select For_content from Table_Insert";
            String sql2 = "select Variable_Type_for from Table_Insert ";
            ps = conn.prepareStatement(sql1);
            rs = ps.executeQuery();

            ResultSetMetaData md = (ResultSetMetaData) rs.getMetaData();
            int columnSize = md.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnSize; i++) {
                    // System.out.printf("%-12s",rs.getObject(i));
                    results.add(rs.getObject(i).toString());
                }
            }
            ps = conn.prepareStatement(sql2);
            rs = ps.executeQuery();

            md = (ResultSetMetaData) rs.getMetaData();
            columnSize = md.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnSize; i++) {
                    needfulparmList.add(rs.getObject(i).toString());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public List<String> select_boolean_true() {
        List<String> results = new ArrayList<String>();
        try {
            String driver = "com.mysql.cj.jdbc.Driver";
            String url = "jdbc:MySQL://comfort_mysql:3306/JVMFuzzing?useUnicode=true&characterEncoding=utf8";
            String username = "root";
            String password = "mysql123";
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
            // String sql1 = "select For_content,Variable_Type_for from table_insert_new
            // WHERE ID ="+ID;
            String sql1 = "select BinaryExpr_content from Table_BinaryExpr_True";
            String sql2 = "select Variable_Type_BinaryExpr from Table_BinaryExpr_True ";
            ps = conn.prepareStatement(sql1);
            rs = ps.executeQuery();

            ResultSetMetaData md = (ResultSetMetaData) rs.getMetaData();
            int columnSize = md.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnSize; i++) {
                    // System.out.printf("%-12s",rs.getObject(i));
                    bin_expr_trueList.add(rs.getObject(i).toString());
                }
            }
            ps = conn.prepareStatement(sql2);
            rs = ps.executeQuery();

            md = (ResultSetMetaData) rs.getMetaData();
            columnSize = md.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnSize; i++) {
                    bin_par_trueList.add(rs.getObject(i).toString());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public List<String> select_boolean_false() {
        List<String> results = new ArrayList<String>();
        try {
            String driver = "com.mysql.cj.jdbc.Driver";
            String url = "jdbc:MySQL://comfort_mysql:3306/JVMFuzzing?useUnicode=true&characterEncoding=utf8";
            String username = "root";
            String password = "mysql123";
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
            // String sql1 = "select For_content,Variable_Type_for from table_insert_new
            // WHERE ID ="+ID;
            String sql1 = "select BinaryExpr_content from Table_BinaryExpr_False";
            String sql2 = "select Variable_Type_BinaryExpr from Table_BinaryExpr_False ";
            ps = conn.prepareStatement(sql1);
            rs = ps.executeQuery();

            ResultSetMetaData md = (ResultSetMetaData) rs.getMetaData();
            int columnSize = md.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnSize; i++) {
                    // System.out.printf("%-12s",rs.getObject(i));
                    bin_expr_falseList.add(rs.getObject(i).toString());
                }
            }
            ps = conn.prepareStatement(sql2);
            rs = ps.executeQuery();

            md = (ResultSetMetaData) rs.getMetaData();
            columnSize = md.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnSize; i++) {
                    bin_par_falseList.add(rs.getObject(i).toString());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public HashMap<String, String> getneedfulparmList(String context) {
        // System.out.println("in getneedfulparmList:\n"+context);
        HashMap<String, String> needsetVarName = new HashMap<>();
        needforString = context;
        needforString = needforString.substring(1);
        needforString = needforString.substring(0, needforString.length() - 1);
        String[] arr1 = needforString.split(",");
        for (int index = 0; index < arr1.length; index++) {
            if (index % 2 != 0) {
                needsetVarName.put(arr1[index - 1].trim(), arr1[index].trim());
            }
        }
        return needsetVarName;
    }

    public List<HashMap<String, String>> getNeedfHashMaps() {
        // System.out.println("length of needfulparmList:"+needfulparmList.size());
        // needfulparmList.clear();
        HashSet h = new HashSet(needfulparmList);
        needfulparmList.clear();
        needfulparmList.addAll(h);
        for (String context : needfulparmList) {
            needsetVarName_list.add(getneedfulparmList(context));
        }
        return needsetVarName_list;
    }

    public HashMap<String, String> GetTrue(){
        HashMap<String,String> True = new HashMap<>();
        int i=0;
        for(String bin_expr: bin_expr_trueList){
            String str1 = bin_par_trueList.get(i).substring(1, bin_par_trueList.get(i).length()-1);
            String str2 = bin_expr;
            True.put(str1,str2);
            i++;
        }
        return True;
    }

    public HashMap<String, String> GetFalse(){
        HashMap<String,String> False = new HashMap<>();
        int i=0;
        for(String bin_expr: bin_expr_falseList){
            String str1 = bin_par_falseList.get(i).substring(1, bin_par_falseList.get(i).length()-1);
            String str2 = bin_expr;
            False.put(str1,str2);
            i++;
        }
        return False;
    }

    public List<TestcaseInfo> getAllTestcases(String query) {
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:MySQL://comfort_mysql:3306/JVMFuzzing?useUnicode=true&characterEncoding=utf8";
        String username = "root";
        String password = "mysql123";
        List<TestcaseInfo> testcaseList = new ArrayList<TestcaseInfo>();
        try{
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                TestcaseInfo tmpInfo = new TestcaseInfo(rs.getInt("id"), rs.getString("Testcase_context"), 
                    rs.getInt("SourceTestcase_id"), rs.getInt("Mutation_times"), 
                    rs.getInt("interesting_times"), rs.getInt("Probability"));
                testcaseList.add(tmpInfo);
            }
            String update = "UPDATE Table_Testcase SET Fuzzing_times = 99 WHERE Fuzzing_times=1;";
            PreparedStatement ps = conn.prepareStatement(update);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testcaseList;
    }
}
