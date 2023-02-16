package com.jvmfuzzing.mutation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.comments.LineComment;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// import org.apache.commons.lang3.StringUtils;


public class Mutator {
    static MutateByMethodInfo mutater = new MutateByMethodInfo();
    static int index = 1;
    static int count = 0;


    public List<CompilationUnit> callMutator(CompilationUnit cu){
        List<CompilationUnit> resultCuList = new ArrayList<CompilationUnit>();
        // TODO.Check if there need a real value.
        TestcaseInfo testcase = new TestcaseInfo(0, "", 0);
        for(TestcaseInfo testcaseInfo:mutateByReplaceVar(cu, testcase)){
            resultCuList.add(StaticJavaParser.parse(testcaseInfo.getTestcase()));
        }
        return resultCuList;
    }

    public List<TestcaseInfo> mutateByReplaceVar(CompilationUnit cu, TestcaseInfo testcase){
        List<TestcaseInfo> newTestcaseList = new ArrayList<TestcaseInfo>();
        // Get all global variables.
        List<FieldDeclaration> fieldList = cu.findAll(FieldDeclaration.class);
        List<VariableDeclarator> fieldVarList = new ArrayList<VariableDeclarator>();
        if(fieldList.size()>0){
            fieldList.forEach(field -> {
                fieldVarList.addAll(field.getVariables());
            });
        }
        if(fieldVarList.size()>0){
            newTestcaseList.addAll(mutate(cu,fieldVarList,testcase,true));
        }
        // Get all methods.
        List<MethodDeclaration> methodList = cu.findAll(MethodDeclaration.class);
        for(MethodDeclaration method:methodList){
            List<VariableDeclarator> allVariableDec = method.findAll(VariableDeclarator.class);
            newTestcaseList.addAll(mutate(cu,allVariableDec,testcase,false));
        }
        return newTestcaseList;
    }

    public void checkAndAddImports(CompilationUnit cu,String oneImportStr){
        ImportDeclaration oneImport = StaticJavaParser.parseImport("import "+oneImportStr+";");
        List<ImportDeclaration> allImport = cu.getImports();
        if(allImport.contains(oneImport) == false){
            cu.addImport(oneImport);
        }
    }

    public List<TestcaseInfo> mutate(CompilationUnit cu, List<VariableDeclarator> allVariableDec, TestcaseInfo testcase, boolean processGlobal){
        // Prepare some variables.
        boolean startToAdd = false;
        List<VariableDeclarator> seedVarGroup = new ArrayList<VariableDeclarator>();
        List<VariableDeclarator> targetVarGroup = new ArrayList<VariableDeclarator>();
        List<TestcaseInfo> newTestcaseList = new ArrayList<TestcaseInfo>();
        // Start to get mutant variables.
        for(VariableDeclarator variableDec:allVariableDec){
            String variableType = variableDec.getType().toString();
            if(mutater.isUnprimitive(variableType)){
                startToAdd = true;
                seedVarGroup.add(variableDec);
            }
            else if(startToAdd && (mutater.isUnprimitive(variableType) == false)){
                targetVarGroup.add(variableDec);
            }
        }
        // Start to mutate.
        if(targetVarGroup.size() > 0){
            ClassOrInterfaceDeclaration c = cu.findFirst(ClassOrInterfaceDeclaration.class).get();
            List<MutateByMethodInfo.MutatedBlock> blockList = mutater.muatateByMethodInfo(seedVarGroup,targetVarGroup,processGlobal);
            for(MutateByMethodInfo.MutatedBlock block:blockList){
                // Set the class name.
                // int newNum = count + index;
                // c.setName("MyJVMTest_"+newNum);
                // If the added API use new class,add import statement.
                // System.out.println("check import size in main:"+block.getImportList().size());
                if(block.getImportList().size()>0){
                    for(String importStr:block.getImportList())
                        checkAndAddImports(cu, importStr);
                }
                // If there exist class call, update them.
                // String newTestcase = cu.toString().replace("MyJVMTest_"+testcase.getId(),"MyJVMTest_"+newNum);
                String newTestcase = cu.toString();
                // Replace variable string.
                // System.out.println("target var:\n"+block.getTargetVarStr()+"mutate var:\n"+block.getMutateVarStr());
                newTestcase = newTestcase.replace(block.getTargetVarStr(),block.getMutateVarStr());
                try{
                    // Remove variable from global field to main method.
                    CompilationUnit newCu = StaticJavaParser.parse(newTestcase);
                    ClassOrInterfaceDeclaration newC = newCu.findFirst(ClassOrInterfaceDeclaration.class).get();
                    FieldDeclaration f = newC.getFieldByName(block.getVarName()).get();
                    // System.out.println(f.toString());
                    MethodDeclaration m = newC.findAll(MethodDeclaration.class).stream().reduce((first, second) -> second).get();
                    // m.setComment(new LineComment("MutateByReplaceVar"));
                    // System.out.println("add comment:\n"+m);
                    newCu.setComment(new LineComment("MutateByReplaceVar"));
                    BlockStmt b = m.getBody().get();
                    Statement targetStmt = StaticJavaParser.parseStatement(f.toString().replace("static ", ""));
                    b.addStatement(0,targetStmt);
                    f.remove();
                    // System.out.println("after update the statement:\n"+newCu.toString());
                    // Final result.
                    TestcaseInfo newTestcaseInfo = new TestcaseInfo(newCu.toString(),testcase.getId(),1,testcase.getMutationTimes()+1);
                    newTestcaseList.add(newTestcaseInfo);
                    index++;
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("error content:\n"+newTestcase);
                    continue;
                }
                // break;
            }               
        }
        // else{
        //     System.out.println("No variable found to be mutated.");
        // }
        return newTestcaseList;
    }
}
