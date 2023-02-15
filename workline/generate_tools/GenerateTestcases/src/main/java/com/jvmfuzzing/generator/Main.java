package com.jvmfuzzing.generator;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.apache.commons.lang3.StringUtils;

public class Main{
    // static final Logger logger = LoggerFactory.getLogger(TestcaseGenerater.class);

    public static void main( String[] args ) throws Exception 
    {
        // Get all files from path.
        GetFiles fileGetter = new GetFiles();

        // Get all files from database.
        List<String> testcases = new ArrayList<String>();
        CodeVisitor codeVisitor = new CodeVisitor();
        DBOperation dbOperation = new DBOperation();
        List<String> files = dbOperation.getAllMethods();

        // Start to assemble.
        int succeesNum = 0;
        for(int i=0;i<files.size();i++)
        {
            // Get one of files.
            // CompilationUnit cu = null;
            // String filename = files.get(i);
            // System.out.println("\n------------------");
            // System.out.println("here is the "+ i + " " + "filename:\n"+filename);
            // Start to parse.
            // FileInputStream in = new FileInputStream(filename);
            try{
                // cu = StaticJavaParser.parse(in);
                // System.out.println("now process:"+i);
                CompilationUnit cu = StaticJavaParser.parse(files.get(i));

                // Set all methods static.
                // List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
                // t.setAllMethodStatic(methods);
                
                // Visit the last method (which is extracted) and generate needful parameters.
                // List<String> needfulArgList = t.visitMethod(methods.get(methods.size()-1));
                
                // If all the parameter can be generated, add them.
                // ClassOrInterfaceDeclaration c = cu.findFirst(ClassOrInterfaceDeclaration.class).get();
                
                // Change the class name.
                // int newNum = i+1;
                // c.setName("MyJVMTest_"+newNum);
                
                // Add main method.
                String allContent = "";
                int newNum = i+1;
                String testcaseContent = codeVisitor.createTestcase(cu, newNum);
                testcases.add(testcaseContent);

                // Write out.
                String workDir = System.getProperty("user.dir");
                String saveDir = workDir.substring(0, workDir.indexOf("/workline"))+"/data/assemble";
                // System.out.println("Save dir:"+saveDir);
                File file =new File(saveDir);
                if(!file.exists()){
                    System.out.println("Save path dose not exist, create it.");
                    file.mkdir();
                }
                if(testcaseContent!=""){
                    fileGetter.overWriteFile(testcaseContent, saveDir+"/MyJVMTest_"+newNum+".java");
                    succeesNum++;
                }

                // Save result into database.
                if(i%1000 == 0 || i == files.size()-1){
                    DBOperation operator = new DBOperation();
                    operator.insertIntoTable(testcases);
                    testcases.clear();
                }  
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
        System.out.println("Totally assemble "+succeesNum+" files(except self-calling methods).");
    }
}