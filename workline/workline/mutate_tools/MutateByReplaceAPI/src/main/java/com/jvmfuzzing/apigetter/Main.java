package com.jvmfuzzing.apigetter;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.*;
import java.util.*;

public class Main{
    private static GetFiles opFile = new GetFiles();
    private static DBOperation opDatabase = new DBOperation();

    public static void main(String[] args) {
        Mutator mutator = new Mutator();
        // List<String> allTestcaeList = new ArrayList<String>();
        List<TestcaseInfo> allTestcaeList = new ArrayList<TestcaseInfo>();
        List<String> seedFiles = opFile.getFiles("/JVMfuzzing/htm/java_code/pass_code/generate/has_main/v5");
        // List<String> seedFiles = opFile.getFiles("/JVMfuzzing/htm/java_code/test_code/20220701");
        // String query = "SELECT * FROM Table_Testcase_v4 WHERE id IN (SELECT DISTINCT Testcase_id FROM Comfort_JVM.Table_javac_Result WHERE Returncode = 0)";
        // List<TestcaseInfo> originTestcaseList = operator.getAllTestcases(query);
        try{
            for(int i=0;i<seedFiles.size();i++){
            // for(int i=0;i<10;i++){
                String seedFileName = seedFiles.get(i);
                // System.out.println("\nNow processing:"+seedFileName);
                FileInputStream seedIn = new FileInputStream(seedFileName);
                CompilationUnit seedCu = StaticJavaParser.parse(seedIn);
                // Start to mutate.
                allTestcaeList.addAll(mutator.mutateByReplaceAPI(seedCu));
                // System.out.println(allTestcaeList.toString());
                if(i%1000 == 0 || i == seedFiles.size()-1){ //  || i == 9
                    // System.out.println("start to insert:"+i);
                    // opDatabase.insertIntoTable(allTestcaeList);
                    String insert = "insert into Table_Testcase_v6_2 (Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method, Mutation_times, Interesting_times, Probability) values (?, 0, ?, 0, ?, ?, 0, 0)";
                    opDatabase.insertManyIntoTable(insert, allTestcaeList);
                    allTestcaeList.clear();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}