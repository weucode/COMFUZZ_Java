package com.jvmfuzzing.mutation_by_all;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.FieldDeclaration;

import java.util.*;

public class ClassifyVariable{
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