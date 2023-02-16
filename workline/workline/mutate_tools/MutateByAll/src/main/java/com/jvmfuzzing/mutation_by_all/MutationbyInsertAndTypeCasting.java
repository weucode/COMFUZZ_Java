package com.jvmfuzzing.mutation_by_all;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;

import com.jvmfuzzing.generator.VariableGenerater;
import com.jvmfuzzing.generator.VariableComponent;

import java.util.*;

import java.util.List;
public class MutationbyInsertAndTypeCasting {
    static VariableGenerater generator = new VariableGenerater();
    static ClassifyVariable classifier = new ClassifyVariable();

    public int getStmtLines(Statement visitStmt){
        return visitStmt.getEnd().get().line - visitStmt.getBegin().get().line + 1;
    }

    public void addStmtBetweenStmt(BlockStmt methodBody, int stmtIndex, Statement insertStmt){
        methodBody.addStatement(stmtIndex,insertStmt);
    }

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

    public CompilationUnit setVarNameInInsertStmt(CompilationUnit cu, MethodDeclaration visitMethod, Statement insertStmt, HashMap<String,String> needfulParam,String name)throws NullPointerException{
        List<Statement> newVarsDec = new ArrayList<Statement>();
        List<String> typeList = Arrays.asList("float", "long", "int","char","short","byte");
        List<String> typeCast = Arrays.asList("double","float", "long", "int","char","short");
        Map<String, String> commonType_differName = new HashMap<String,String>();
        Map<String,String> availableVar = classifier.getAvailableVar(cu.findAll(FieldDeclaration.class),visitMethod,insertStmt.getBegin().get().line);


        String name_changed = "";
        List<String> list_avar_key = new ArrayList<String>(availableVar.keySet());
        List<String> list_avar_vale = new ArrayList<String>(availableVar.values());
        NodeList<Parameter> Par = visitMethod.getParameters();
        for(Parameter par_m :Par){
            list_avar_key.add(par_m.getName().asString());
            list_avar_vale.add(par_m.getType().asString());
        }

        List<String> list_need_key = new ArrayList<String>(needfulParam.keySet());
        List<String> list_need_vale = new ArrayList<String>(needfulParam.values());
        HashMap<String,String> common = new HashMap<String,String>();
        HashMap<String,String> uncommon = new HashMap<String,String>();
        // System.out.println("Start setVarNameInInsertStmt");
        int numms = 0;
        if(availableVar.size()!=0){
            for(int index =0;index<list_avar_vale.size();index++){
                for(int index_need =0;index_need<list_need_key.size();index_need++){
                    if(list_avar_vale.get(index).equals(list_need_vale.get(index_need))){
                        if(!list_avar_key.get(index).equals(list_need_key.get(index_need))&&(numms==0)){
                            numms++;
                            common.put(list_need_key.get(index_need),list_avar_key.get(index));
                        }
                        
                    }
                    else{
                        if(list_avar_key.get(index).equals(list_need_key.get(index_need))){
                            setVarName(insertStmt,list_need_key.get(index_need),list_need_key.get(index_need)+"_change");
                            uncommon.put(list_need_key.get(index_need)+"_change",list_need_vale.get(index_need));
                        }
                        uncommon.put(list_need_key.get(index_need),list_need_vale.get(index_need));
                    }

                }
            }
        }
        else{
            // System.out.println("uncommon"+needfulParam);
            uncommon = needfulParam;
        }
        // System.out.println("common:\t"+common);
        int common_num =0;
        for(Map.Entry<String,String> entry_com:common.entrySet()){
            setVarName(insertStmt,entry_com.getKey(),entry_com.getValue());
            name_changed = entry_com.getKey();
            // System.out.println("name_changed:\t"+name_changed);
            common_num++;
            break;
        }
        String delete = "";
        uncommon.remove(name_changed);
        if(uncommon.size()!=0){
            // System.out.println("uncommon:\t"+uncommon);
            int num =0;
            for(Map.Entry<String,String> entry:uncommon.entrySet()){
                for(int i =0;i<6;i++){
                    // if(!(entry.getValue().equals(typeList.get(i))&&(!entry.getKey().equals(name_changed)))){
                    if(!entry.getValue().equals(typeList.get(i))){
                        commonType_differName.put(entry.getKey(),entry.getValue());
                    }
                    else {
                        newVarsDec.addAll(callGenerateDec(cu,"myjvm"+num,typeCast.get(i)));
                        newVarsDec.add(StaticJavaParser.parseStatement(entry.getValue()+" "+entry.getKey()+" = "+"("+typeList.get(i)+")"+"myjvm"+num+";\n"));
                        delete = entry.getKey();
                        num++;
                    }     
                    commonType_differName.remove(delete);
                }
                
                
            }
            commonType_differName.remove(name_changed);
            // System.out.println("commonType_differName"+commonType_differName);
            for(Map.Entry<String,String> entry1:commonType_differName.entrySet()){
                newVarsDec.addAll(callGenerateDec(cu,entry1.getKey(),entry1.getValue()));
            }
        }
        newVarsDec.add(insertStmt);
        Statement stmt = newVarsDec.get(0);
        List<Node> nodes = stmt.findAll(Node.class);
        nodes.get(0).setLineComment("The Seed of:\t"+name);
        nodes.get(1).setLineComment("MutationbyInsertAndTypeCasting");
        // return newVarsDec;

        for (int index = 0; index < newVarsDec.size(); index++) {
            Statement tmpStmt = newVarsDec.get(index);
            addStmtBetweenStmt(visitMethod.getBody().get(), index, tmpStmt);
        }
        return cu;
    }
    
    public Map<String,String> getAvailableVar(List<FieldDeclaration> fieldList,MethodDeclaration visitMethod,int lineIndex){
        Map<String,String> varMap = new HashMap<String,String>();
        for(FieldDeclaration field:fieldList){
            for(VariableDeclarator var:field.getVariables()){
                varMap.put(var.getNameAsString(),var.getTypeAsString());
            }
        }
        List<VariableDeclarator> varList = visitMethod.findAll(VariableDeclarator.class);
        for(VariableDeclarator var:varList){
            if(var.getBegin().get().line < lineIndex){
                varMap.put(var.getNameAsString(),var.getTypeAsString());
            }
        }
        return varMap;
    }
}
