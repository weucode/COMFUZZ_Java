package com.jvmfuzzing.mutation;

import java.util.List;
import java.util.ArrayList;

import java.sql.*;
 
public class DBOperation { 
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://comfuzz_mysql:3306/JVMFuzzing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    static final String USER = "root";
    static final String PASS = "mysql123";

    public Connection createConnection(){
        Connection conn = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        }catch(Exception e){
            e.printStackTrace();
        }
        return conn;
    }

    public Statement createStatement(Connection conn){
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stmt;
    }
 
    public void insertManyIntoTable(String sql, List<TestcaseInfo> newTestcaseList) {
        // Excute sql operation.
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);
        try{
            conn.setAutoCommit(false); 
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < newTestcaseList.size(); i++) {     
                pstmt.clearParameters();     
                pstmt.setString(1, newTestcaseList.get(i).getTestcase());
                pstmt.setInt(2, newTestcaseList.get(i).getSourceTestcaseId());
                pstmt.setInt(3, newTestcaseList.get(i).getMutationMethod());
                pstmt.setInt(4, newTestcaseList.get(i).getMutationTimes());
                pstmt.execute();     
                if (i % 1000 == 0) {         
                    conn.commit();  
                } 
            } 
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Clean-up environment
        try{
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet queryFromTable(Statement stmt, String sql){
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public List<TestcaseInfo> getAllTestcases(String sql){
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);   
        // Excute and get result.
        ResultSet rs = queryFromTable(stmt,sql);
        List<TestcaseInfo> testcaseList = new ArrayList<TestcaseInfo>();
        try{
            // Process results.
            while(rs.next()){
                int id  = rs.getInt("id");
                String testcaseContext = rs.getString("Testcase_context");
                int mutationTime = rs.getInt("Mutation_times");
                if(testcaseContext.equals(""))
                    continue;
                TestcaseInfo testcaseInfo = new TestcaseInfo(id,testcaseContext,mutationTime);
                testcaseList.add(testcaseInfo);
            }
            // Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testcaseList;
    }

    public List<UsefulMethodInfo> getMethodInfo(String sql){
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);   
        List<UsefulMethodInfo> methodInfoList = new ArrayList<UsefulMethodInfo>();
        try{
            // ResultSet quickRs = queryFromTable(stmt, quickSql);
            ResultSet rs = queryFromTable(stmt,sql);
            while(rs.next()){
                String methodName = rs.getString("Method_Name");
                String paramInfoStr = rs.getString("Param_Info");
                String boundaryValueStr = rs.getString("Boundary_Value");
                UsefulMethodInfo usefulMethodInfo = new UsefulMethodInfo(methodName,paramInfoStr,boundaryValueStr);
                methodInfoList.add(usefulMethodInfo);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return methodInfoList;
    }  

    public List<UsefulMethodInfo> getMethodInfo(Statement stmt, String sql){
        // Connection conn = createConnection();
        // Statement stmt = createStatement(conn);   
        List<UsefulMethodInfo> methodInfoList = new ArrayList<UsefulMethodInfo>();
        try{
            // ResultSet quickRs = queryFromTable(stmt, quickSql);
            ResultSet rs = queryFromTable(stmt,sql);
            while(rs.next()){
                String methodName = rs.getString("Method_Name");
                String paramInfoStr = rs.getString("Param_Info");
                String boundaryValueStr = rs.getString("Boundary_Value");
                UsefulMethodInfo usefulMethodInfo = new UsefulMethodInfo(methodName,paramInfoStr,boundaryValueStr);
                methodInfoList.add(usefulMethodInfo);
            }
            rs.close();
            // stmt.close();
            // conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return methodInfoList;
    }    
}