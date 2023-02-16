package com.jvmfuzzing.mutation_by_all;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ContinueStmt;

import java.io.IOException;

import java.util.List;

public class MutationbyJump {
    public static CompilationUnit getOperator(CompilationUnit cu) throws IOException {
        // String name = cu.getStorage().get().getPath().toString();
        List<ClassOrInterfaceDeclaration> c1 = cu.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration c : c1) {
            List<MethodDeclaration> methods = c.getMethods();
            int size = methods.size() - 1;
            for (int i = 0; i < size; i++) {
                MethodDeclaration m = methods.get(i);
                m.walk(node -> {
                    if (node instanceof BreakStmt) {
                        if(!node.toString().contains("switch")){
                            node.setLineComment("MutateByJump_Break");
                            // node.setLineComment("The seed from:\t"+name);
                            BreakStmt bre = (BreakStmt)node;
                            bre.toContinueStmt();
                        }
                    }
                    else if (node instanceof ContinueStmt) {
                        if(!node.toString().contains("while")){
                            node.setLineComment("MutateByJump_continue");
                            // node.setLineComment("The seed from:\t"+name);
                            ContinueStmt continueStmt = (ContinueStmt) node;
                            continueStmt.toBreakStmt();
                        }                        
                    }
                });
            }
        }
        return cu;
    }
}
