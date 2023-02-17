package com.jvmfuzzing.apigetter;

import java.util.*;
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

    public List<ClassInfo> existInTable(String scope, String callerName){
        List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);
        String sql_for_class = String.format("SELECT * FROM Method_Info WHERE Class_Page LIKE '%%/%s.html'",scope);
        // System.out.println("Check sql_for_class:"+sql_for_class);
        List<ClassInfo> classInfoList = new ArrayList<ClassInfo>();
        try{
            ResultSet rs = queryFromTable(stmt,sql_for_class);
            if(rs == null)
                return classInfoList;
            rs.last();
            int rows = rs.getRow();
            if(rows > 0){
                // System.out.println("This is a standard class!");
                ResultSet rs_for_return_type = queryFromTable(stmt, String.format("SELECT * FROM Method_Info WHERE Class_Page LIKE '%%/%s.html' AND Method_Name = '%s'",scope, callerName));
                String callerType = "";
                while(rs_for_return_type.next())
                    callerType = rs_for_return_type.getString("Return_Type");
                String sql_for_methods = String.format("SELECT * FROM Method_Info WHERE Class_Page LIKE '%%/%s.html' AND Return_Type = '%s'",scope, callerType);
                methodInfoList.addAll(getMethodInfo(sql_for_methods));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // System.out.println("Check methodinfo list size:"+methodInfoList.size());
        classInfoList.add(new ClassInfo(scope, callerName, null, null, methodInfoList));
        return classInfoList;
    }

    public List<MethodInfo> getMethodInfo(String sql){
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);   
        List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
        try{
            ResultSet rs = queryFromTable(stmt,sql);
            while(rs.next()){
                String methodName = rs.getString("Method_Name");
                String returnTypeStr = rs.getString("Return_Type");
                String paramInfoStr = rs.getString("Param_Info");
                String importStr = rs.getString("Import_Stmt");
                MethodInfo MethodInfo = new MethodInfo(methodName,returnTypeStr,paramInfoStr,importStr);
                methodInfoList.add(MethodInfo);
                // System.out.println("Check method info here:"+MethodInfo.getMethodName());
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return methodInfoList;
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
}