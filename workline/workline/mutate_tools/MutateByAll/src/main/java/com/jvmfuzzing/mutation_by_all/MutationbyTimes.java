package com.jvmfuzzing.mutation_by_all;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;

import com.jvmfuzzing.generator.VariableGenerater;
import com.jvmfuzzing.generator.VariableComponent;

import java.util.*;
import java.io.IOException;

import java.util.List;

import java.sql.*;

public class MutationbyTimes {

    static VariableGenerater generator = new VariableGenerater();
    static ClassifyVariable classifier = new ClassifyVariable();

    public void setVarName(Statement visitStmt,String oldName,String newName){
        visitStmt.findAll(NameExpr.class).forEach(n -> {
            if(n.getNameAsString().equals(oldName)){
                n.setName(newName);
            }
        });
        // System.out.println("check set name here:\n"+visitStmt.toString());
    }

    public List<Statement> callGenerateDec(CompilationUnit cu,String name,String type)throws NullPointerException {
        // The variable that holds the result.
        List<Statement> varStmt = new ArrayList<Statement>();
        // Call generator
        // System.out.println("---------------start callGenerateDec---------");
        List<VariableComponent> varComp = generator.generateDec(name,type);
        // System.out.println("Ths is varComp:\n"+varComp);
        for(VariableComponent vc : varComp){
            // Get the components of variables and assemble, such as:int a = 1;\n
            if(StaticJavaParser.parseStatement(vc.getVarType()+" "+vc.getVarName()+" = "+vc.getVarValue().toString()+";\n")==null) continue;
            varStmt.add(StaticJavaParser.parseStatement(vc.getVarType()+" "+vc.getVarName()+" = "+vc.getVarValue().toString()+";\n"));
            // If the generated parameters introduce new types, check and add them in cu.
            if(vc.getImportStmt().length()>0){
                Arrays.asList(vc.getImportStmt().split("\n")).forEach(s -> {
                    if(!cu.getImports().stream().anyMatch(i -> i.toString().equals(s))){
                        cu.addImport(s);
                    }
                });
            }
        }
        return varStmt;
    }

    public void addStmtBetweenStmt(BlockStmt methodBody, int stmtIndex, Statement insertStmt){
        methodBody.addStatement(stmtIndex,insertStmt);
    }

    public CompilationUnit setVarNameInInsertStmt(CompilationUnit cu, MethodDeclaration visitMethod, Statement insertStmt, HashMap<String,String> needfulParam,String name)throws NullPointerException{
        List<Statement> newVarsDec = new ArrayList<Statement>();
        Map<String,String> availableVar = classifier.getAvailableVar(cu.findAll(FieldDeclaration.class),visitMethod,insertStmt.getBegin().get().line);

        List<String> list_avar_key = new ArrayList<String>(availableVar.keySet());
        List<String> list_avar_vale = new ArrayList<String>(availableVar.values());


        // This part is to modify the number of variable cycles.
        List<BinaryExpr> binaryExprs = insertStmt.findAll(BinaryExpr.class);;
        // System.out.println("bin:\n"+binaryExprs);
        Random random1 = new Random();
        int nums = random1.nextInt(10000);
        String num_str = String.valueOf(nums);
        if(binaryExprs.size()>0){
            binaryExprs.get(0).setRight(StaticJavaParser.parseExpression(num_str));
        }
        

        List<String> list_need_key = new ArrayList<String>(needfulParam.keySet());
        List<String> list_need_vale = new ArrayList<String>(needfulParam.values());
        HashMap<String,String> common = new HashMap<String,String>();
        HashMap<String,String> uncommon = new HashMap<String,String>();
        NodeList<Parameter> Par = visitMethod.getParameters();
        for(Parameter par_m :Par){
            list_avar_key.add(par_m.getName().asString());
            list_avar_vale.add(par_m.getType().asString());
        }


        // System.out.println("----------------Start setVarNameInInsertStmt---------------------");
        if(availableVar.size()!=0){
            for(int index =0;index<list_avar_vale.size();index++){
                for(int index_need =0;index_need<list_need_key.size();index_need++){
                    if(list_avar_vale.get(index).equals(list_need_vale.get(index_need))){
                        if(list_avar_key.get(index).equals(list_need_key.get(index_need))){
                            common.put(list_need_key.get(index_need),list_need_key.get(index_need)+"change");
                        }
                        else if(!common.containsValue(list_avar_key.get(index))){
                            common.put(list_need_key.get(index_need),list_avar_key.get(index));
                        }
                    }
                    else{
                        uncommon.put(list_need_key.get(index_need),list_need_vale.get(index_need));
                    }
                }
            }
        }
        else{
            // System.out.println("uncommon"+needfulParam);
            uncommon = needfulParam;
        }


        if(uncommon.size()!=0){
            for(Map.Entry<String,String> entry:uncommon.entrySet()){
                newVarsDec.addAll(callGenerateDec(cu,entry.getKey(),entry.getValue()));
            }
        }
        // System.out.println("setVarName"+insertStmt);
        for(Map.Entry<String,String> entry_com:common.entrySet()){
            setVarName(insertStmt,entry_com.getKey(),entry_com.getValue());
        }

        newVarsDec.add(insertStmt);
        Statement stmt = newVarsDec.get(0);
        List<Node> nodes = stmt.findAll(Node.class);
        nodes.get(0).setLineComment("The Seed of:\t"+name);
        nodes.get(1).setLineComment("MutateByInsertionAndTimes");


        for (int index = 0; index < newVarsDec.size(); index++) {
            Statement tmpStmt = newVarsDec.get(index);
            addStmtBetweenStmt(visitMethod.getBody().get(), index, tmpStmt);
        }
        return cu;
    }
}
