package com.jvmfuzzing.mutation_by_all;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;


public class SaveToDB_new {
    public static void insertIntoTable(List<TestcaseInfo> testcaseList) {
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:MySQL://comfuzz_mysql:3306/JVMFuzzing?useUnicode=true&characterEncoding=utf8";
        String username = "root";
        String password = "mysql123";
        try{
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            // java.sql.Statement statement = conn.createStatement();
            conn.setAutoCommit(false); 
            PreparedStatement pstmt = conn.prepareStatement("insert into Table_Testcase (Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method, Mutation_times, Interesting_times, Probability) values (?, ?, ?, ?, ?, ?, ?, ?)"); 
            for (int i = 0; i < testcaseList.size(); i++) {    
                TestcaseInfo testcase = testcaseList.get(i);
                pstmt.clearParameters();     
                pstmt.setString(1, testcase.getTestcaseContent());
                pstmt.setString(2, String.valueOf(testcase.getSourceId()));
                pstmt.setString(3, String.valueOf(testcase.getSourceId()));
                pstmt.setString(4, "0");
                pstmt.setString(5, String.valueOf(testcase.getMutationMethod()));     
                pstmt.setString(6, String.valueOf(testcase.getMutationTimes()));     
                pstmt.setString(7, "0");     
                pstmt.setString(8, "0");     
                pstmt.execute();
                if (i % 1000 == 0) {         
                    conn.commit();  
                } 
            } 
            conn.commit();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connection(List<String> fileconnect)throws SQLException, ClassNotFoundException, SQLSyntaxErrorException {
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:MySQL://comfuzz_mysql:3306/JVMFuzzing?useUnicode=true&characterEncoding=utf8";
        String username = "root";
        String password = "mysql123";
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            java.sql.Statement statement = conn.createStatement();
            int i = 0;
            String Insert_for = "insert into Table_Testcase3(Testcase_context,Fuzzing_times,Mutation_method)";
            String Insert_2 = "values(%s,%s,%s)";
            for (String connect : fileconnect) {
                try {
                    String stmt_change = connect;
                    String sql_for = "";
                    // System.out.println(connect);
                    if (connect.contains("'") || connect.contains("/") || connect.contains("\\\\")) {
                        stmt_change = connect.replaceAll("'", "''");
                        stmt_change = connect.replaceAll("/", "'/");
                        stmt_change = connect.replaceAll("\\\\", "\\\\\\\\ ");
                    }
                    String For_content = "'" + stmt_change + "'";
                    if(connect.contains("MutateByJump_Break")||connect.contains("MutateByJump_continue")){
                        sql_for = Insert_for + String.format(Insert_2, For_content, 1, 0);
                    }else if(connect.contains("MutateByInsertionAndTimes")){
                        sql_for = Insert_for + String.format(Insert_2, For_content, 1, 1);
                    }else if(connect.contains("MutateByTrueAndInsertion")){
                        sql_for = Insert_for + String.format(Insert_2, For_content, 1, 2);
                    }else if(connect.contains("MutationbyInsertAndTypeCasting")){
                        sql_for = Insert_for + String.format(Insert_2, For_content, 1, 3);
                    }else if(connect.contains("MutateByReplaceAPI")){
                        sql_for = Insert_for + String.format(Insert_2, For_content, 1,4);
                    }else if (connect.contains("MutateByReplaceVar")){
                        sql_for = Insert_for + String.format(Insert_2, For_content, 1, 5);
                    }else{
                        sql_for = Insert_for + String.format(Insert_2, For_content, 1, 6);
                    }
                    // System.out.println("######################");
                    // System.out.println("hahhaha");
                    // System.out.println(sql_for);
                    i++;
                    // System.out.println("i:\t" + i);
                    statement.execute(sql_for);
                } catch (SQLSyntaxErrorException e) {
                    // System.out.println("SQLSyntaxErrorException");
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            conn.close();
        }catch(

    ClassNotFoundException e)
    {
        // System.out.println("not find DriverManager");
        e.printStackTrace();
    }catch(
    Exception e)
    {
        e.printStackTrace();
    }
    // catch(SQLSyntaxErrorException e){
    // // System.out.println("SQLSyntaxErrorException");
    // e.printStackTrace();
    // }
    }

    public static List<String> Get(String Path) {
        File file = new File(Path);
        List<String> list_get = new ArrayList<String>();
        File[] filePathLists = file.listFiles();
        String context = "";
        for (File test : filePathLists) {
            context = readTxtFileIntoStringArrList(test.toString());
            // System.out.println(context);
            list_get.add(context);
        }
        return list_get;
    }

    private static String readTxtFileIntoStringArrList(String filePath) {
        List<String> list = new ArrayList<String>();
        String test = "";
        try {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    list.add(lineTxt);
                }
                for (String what : list) {
                    test = test + what + "\n";
                }
                bufferedReader.close();
                read.close();
            } else {
                // System.out.println("can't find file");
            }
        } catch (Exception e) {
            // System.out.println("real connection error");
            e.printStackTrace();
        }

        return test;
    }

    public static void SaveBinary(List<String> Binary_list, List<String> Parm_list)
            throws SQLException, ClassNotFoundException, SQLSyntaxErrorException {
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:MySQL://comfuzz_mysql:3306/JVMFuzzing?useUnicode=true&characterEncoding=utf8";
        String username = "root";
        String password = "mysql123";
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            java.sql.Statement statement = conn.createStatement();
            int i = 0;

            String Insert_for = "insert into Table_BinaryExpr(BinaryExpr_content,Variable_Type_BinaryExpr)";
            String Insert_2 = "values(%s,%s)";
            for (String bin : Binary_list) {
                try {
                    String For_content = "'" + bin + "'";
                    String sql_for = Insert_for + String.format(Insert_2, For_content, Parm_list.get(i));
                    // System.out.println("######################");
                    // System.out.println(sql_for);
                    i++;
                    // System.out.println("i:\t" + i);
                    statement.execute(sql_for);
                } catch (SQLSyntaxErrorException e) {
                    // System.out.println("SQLSyntaxErrorException");
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
