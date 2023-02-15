package com.jvmfuzzing.mutation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.util.*;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main{
    static DBOperation operator = new DBOperation();
    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws FileNotFoundException {
        Mutator mutator = new Mutator();
        // Get all origin testcase.
        // String query = "SELECT * FROM Table_Testcase_v4 WHERE id < 1000";
        String query = "SELECT * FROM Table_Testcase_v4 WHERE id IN (SELECT DISTINCT Testcase_id FROM Comfort_JVM.Table_javac_Result WHERE Returncode = 0)";
        List<TestcaseInfo> originTestcaseList = operator.getAllTestcases(query);
        List<TestcaseInfo> newTestcaseList = new ArrayList<TestcaseInfo>();
        for(TestcaseInfo testcase:originTestcaseList){
            // System.out.println("\nProcessing:"+testcase.getId());
            // System.out.println("Testcase:"+testcase.getTestcase());
            try{
                CompilationUnit cu = StaticJavaParser.parse(testcase.getTestcase());
                newTestcaseList.addAll(mutator.mutateByReplaceVar(cu, testcase));
            }
            catch (Exception e){
                // e.printStackTrace();
                logger.error("Error when processing testcase:{}\n{}\n",testcase.getId(),e);
                continue;
            }
        }
        // String insert = "insert into Table_Testcase_v4 (Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method, Mutation_times, Interesting_times, Probability) values (?, 0, ?, 0, ?, ?, 0, 0)";
        String insert = "insert into Table_Mutate_Testcase (Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method, Mutation_times, Interesting_times, Probability) values (?, 0, ?, 0, ?, ?, 0, 0)";
        operator.insertManyIntoTable(insert, newTestcaseList);
    }
}