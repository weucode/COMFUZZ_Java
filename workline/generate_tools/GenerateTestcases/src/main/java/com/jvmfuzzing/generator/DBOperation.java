package com.jvmfuzzing.generator;

import java.util.*;
import java.sql.*;
 
public class DBOperation { 
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://comfort_mysql:3306/JVMFuzzing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

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

    public List<String> getAllMethods() {
        Statement stmt = null;
        Connection conn = null;
        List<String> methodList = new ArrayList<String>();
        try{
            conn = createConnection();
            stmt = conn.createStatement();
            String query = "SELECT * FROM Table_Function;";
            ResultSet rs = stmt.executeQuery(query);            
            while (rs.next()) {
                methodList.add(rs.getString("Function_content"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return methodList;
    }
 
    public void insertIntoTable(List<String> contentList) {
        Statement stmt = null;
        Connection conn = null;
        try{
            conn = createConnection();
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            conn.setAutoCommit(false); 
            PreparedStatement pstmt = conn.prepareStatement("insert into Table_Testcase (Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method, Mutation_times, Interesting_times, Probability) values (?, ?, 0, 0, 0, 0, 0, 0)"); 
            for (int i = 0; i < contentList.size(); i++) {     
                pstmt.clearParameters();     
                pstmt.setString(1, contentList.get(i));     
                pstmt.setInt(2, i+1);
                pstmt.execute();
                if (i % 1000 == 0) {         
                    conn.commit();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            // Clean-up environment
            try{
                stmt.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ConstructorInfo getConstructorInfo(String className) {
        Statement stmt = null;
        Connection conn = null;
        String result = "",implementingClassName = "",packageName = "";
        ConstructorInfo constructorInfo = null;
        try{
            conn = createConnection();
            stmt = conn.createStatement();
            String query = "SELECT * FROM Constructor_Info WHERE Class_Name = '"+className+"';";
            // System.out.println("query:"+query);
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                // result = rs.getString("Constructor_Stmt");
                String classDesc = rs.getString("Class_Description");
                // System.out.println("class desc:"+classDesc);
                if(classDesc.contains("abstract class")){
                    // System.out.println("1");
                    String queryAgain = String.format("SELECT * FROM Constructor_Info WHERE Class_Description like '%%extends %s' and Constructor_Stmt not like '%%(%s%%';",className,className);
                    // System.out.println("query again:"+queryAgain);
                    ResultSet rsAgain = stmt.executeQuery(queryAgain);
                    while(rsAgain.next()){
                        result = rsAgain.getString("Constructor_Stmt");
                        implementingClassName = rsAgain.getString("Class_Name");
                        packageName = rsAgain.getString("Package_Name");
                        break;
                    }
                    constructorInfo = new ConstructorInfo(implementingClassName, result, packageName);
                    break;
                }
                else{                
                    // System.out.println("2");
                    result = rs.getString("Constructor_Stmt");
                    packageName = rs.getString("Package_Name");
                    constructorInfo = new ConstructorInfo(className, result, packageName);
                    break;
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            // Clean-up environment
            try{
                stmt.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return constructorInfo;
    }
}