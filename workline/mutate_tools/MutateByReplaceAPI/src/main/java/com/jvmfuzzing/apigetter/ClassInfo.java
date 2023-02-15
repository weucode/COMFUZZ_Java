package com.jvmfuzzing.apigetter;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.util.List;

import com.jvmfuzzing.apigetter.MethodInfo;

public class ClassInfo{
    private String className;
    private String packageName;
    private List<ImportDeclaration> importList;
    private List<VariableDeclarator> globalVarList;
    private List<MethodInfo> methodInfoList;

    // public ClassInfo(List<ImportDeclaration> importList, List<VariableDeclarator> globalVarList, List<MethodInfo> methodInfoList){
    //     this.importList = importList;
    //     this.globalVarList = globalVarList;
    //     this.methodInfoList = methodInfoList;
    // }

    public ClassInfo(String className, String packageName, List<ImportDeclaration> importList, List<VariableDeclarator> globalVarList, List<MethodInfo> methodInfoList){
        this.className = className;
        this.packageName = packageName;
        this.importList = importList;
        this.globalVarList = globalVarList;
        this.methodInfoList = methodInfoList;
    }

    public String getClassName(){
        return this.className;
    }

    public String getPackageName(){
        return this.packageName;
    }

    public List<ImportDeclaration> getImportList(){
        return this.importList;
    }

    public List<VariableDeclarator> getGlobalVarList(){
        return this.globalVarList;
    }

    public List<MethodInfo> getMethodList(){
        return this.methodInfoList;
    }
}