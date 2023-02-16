package com.jvmfuzzing.mutation_by_all;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.Node;


import java.util.*;
import java.io.IOException;
import java.util.List;

import java.sql.*;

public class MutationbyTrue {
    public int getStmtLines(Statement visitStmt){
        return visitStmt.getEnd().get().line - visitStmt.getBegin().get().line + 1;
    }
    
    public void Insert_do(CompilationUnit cu)throws IOException,SQLException,ClassNotFoundException{
        // String name = cu.getStorage().get().getPath().toString();
        // String name_1 = cu.getStorage().get().getFileName();
        // System.out.println(name);
        List<ClassOrInterfaceDeclaration> c1 = cu.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration c : c1) {

            List<MethodDeclaration> methods = c.getMethods();
            int size = methods.size()-1;
            for(int i =0;i<size;i++) {
                MethodDeclaration m=methods.get(i);
                if (m.getBody().isPresent()) {
                    BlockStmt methodBody = m.getBody().get();
                    if(methodBody == null)continue;
                    List<Statement> statementList = m.getBody().get().getStatements();
                    String stmt_ture_tring1 ="do {\n" ;
                    int ii = 0;
                    for (Statement stmt : statementList) {
                        ii++;
                        if (stmt.isForStmt()) {
                            addStmtInStmt_do(cu, m, methodBody,stmt,stmt_ture_tring1,ii);
                            List<Node> nodes = stmt.findAll(Node.class);
                            nodes.get(0).setLineComment("mutate by true");
                            // nodes.get(0).setLineComment("The seed from:"+name);
                            m.walk(node -> {
                                if (node instanceof ForStmt) {
                                    ForStmt forstmt = (ForStmt) node;
                                    if(stmt.equals(forstmt)){
                                        node.remove();
                                    }
                                }
                            });
                            break;
                        }
                    }
                    System.out.println("---------------------------End Insert-----------------------------");

                }
            }
            // System.out.println("I HERE");
        }
    }

    public void addStmtInStmt_do(CompilationUnit cu,MethodDeclaration visitMethod,BlockStmt methodBody,Statement visitStmt,String Stmt_true,int i){
        // Delete comments
        visitStmt.getComment().ifPresent(comment -> comment.remove());
        List<String> codeList = new ArrayList<String>();
        codeList.add(Stmt_true);
        Collections.addAll(codeList,visitStmt.toString().split("\n"));
        Random random = new Random();
        int num = random.nextInt(100);
        codeList.add("} while(my++<"+num+");\n");
        // Put the mutated code block back.
        // System.out.print("------------------------------------------\n");
        // System.out.print("This is String.join\n"+String.join("\n", codeList)+"\n");
        methodBody.addStatement(0,StaticJavaParser.parseStatement("int my = 0;"));
        methodBody.setStatement(i, StaticJavaParser.parseStatement(String.join("\n", codeList)));
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

    public void Insert_if(CompilationUnit cu,String par_name,String Par_type,List<String> boolen_false,List<String> boolen_True)throws IOException,SQLException,ClassNotFoundException{
        // String name = cu.getStorage().get().getPath().toString();
        // String name_1 = cu.getStorage().get().getFileName();
        // System.out.println(name);
        List<ClassOrInterfaceDeclaration> c1 = cu.findAll(ClassOrInterfaceDeclaration.class);
        
        String boolean_String =""; 
        for (ClassOrInterfaceDeclaration c : c1) {

            List<MethodDeclaration> methods = c.getMethods();
            int size = methods.size()-1;
            for(int i =0;i<size;i++) {
                MethodDeclaration m=methods.get(i);
                if (m.getBody().isPresent()) {
                    BlockStmt methodBody = m.getBody().get();
                    if(methodBody == null)continue;
                    List<Statement> statementList = m.getBody().get().getStatements();
                    String stmt_ture_tring1 ="if"+"("+boolean_String+")"+" {\n" ;
                    int ii = 0;
                    for (Statement stmt : statementList) {
                        ii++;
                        if (stmt.isForStmt()) {
                            addStmtInStmt_if(cu, m, methodBody,stmt,stmt_ture_tring1,ii);
                            List<Node> nodes = stmt.findAll(Node.class);
                            nodes.get(0).setLineComment("mutate by true");
                            // nodes.get(0).setLineComment("The seed from:"+name);
                            m.walk(node -> {
                                if (node instanceof ForStmt) {
                                    ForStmt forstmt = (ForStmt) node;
                                    if(stmt.equals(forstmt)){
                                        node.remove();
                                    }
                                }
                            });
                            break;
                        }
                    }
                    // System.out.println("---------------------------End Insert-----------------------------");

                }
            }
            // System.out.println("I HERE");
        }
    }

    public void addStmtInStmt_if(CompilationUnit cu,MethodDeclaration visitMethod,BlockStmt methodBody,Statement visitStmt,String Stmt_true,int i){
        visitStmt.getComment().ifPresent(comment -> comment.remove());
        List<String> codeList = new ArrayList<String>();
        codeList.add(Stmt_true);
        Collections.addAll(codeList,visitStmt.toString().split("\n"));
        Random random = new Random();
        int num = random.nextInt(100);
        codeList.add("} while(my++<"+num+");\n");
        // Put the mutated code block back.
        // System.out.print("------------------------------------------\n");
        methodBody.addStatement(0,StaticJavaParser.parseStatement("int my = 0;"));
        methodBody.setStatement(i, StaticJavaParser.parseStatement(String.join("\n", codeList)));
    }
}
