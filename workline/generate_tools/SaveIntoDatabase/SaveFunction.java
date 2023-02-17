import java.util.*;
import java.sql.*;
import java.io.*;


public class SaveFunction {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://comfuzz_mysql:3306/JVMFuzzing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
 
    static final String USER = "root";
    static final String PASS = "mysql123";

    public static List<String> getFiles(String path) {
        List<String> fileList = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                fileList.add(tempList[i].toString());
            }
            if (tempList[i].isDirectory()) {
                String newPath = path + "/" + tempList[i].getName();
                List<String> moreFileList = new ArrayList<String>();
                moreFileList = getFiles(newPath);
                fileList.addAll(moreFileList); 
            }
        }
        return fileList;
    }

    public static List<String> readText(String filename){
        BufferedReader reader;
        List<String> textContent = new ArrayList<String>();
        try {
                reader = new BufferedReader(new FileReader(filename));
                String line = reader.readLine();
                textContent.add(line);
                while (line != null) {
                    line = reader.readLine();
                    textContent.add(line);
                }
                reader.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
        textContent.remove(textContent.size()-1);
        return textContent;
    }

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
 
    public void insertManyIntoTable(String sql, List<String> textList) {
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);
        try{
            conn.setAutoCommit(false); 
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < textList.size(); i++) {     
                pstmt.clearParameters();     
                pstmt.setString(1, textList.get(i));
                pstmt.execute();     
                if (i % 1000 == 0) {         
                    conn.commit();  
                } 
            } 
            conn.commit();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        List<String> fileList = getFiles(args[0]);
        List<String> textList = new ArrayList<String>();
        for(String file:fileList){
            textList.add(String.join("\n",readText(file)));
        }
        String sql;
        if(args[1].equals("function")){
            // Save into Table_Function
            sql = "insert into " + args[2] + " (Function_content, SourceFun_id, Mutation_method, Mutation_times) values (?, 0, 0, 0)";
        }
        else{
            // Save into Table_Testcase
            sql = "insert into " + args[2] + " (Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method, Mutation_times, Interesting_times, Probability) values (?, 0, 0, 0, 0, 0, 0, 0)";
        }
        new SaveFunction().insertManyIntoTable(sql, textList);
        System.out.println("Totally insert "+fileList.size()+" files.");
    }
}
