package com.jvmfuzzing.apigetter;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;

import java.util.*;

public class MethodInfo{
    private String name;
    private String returnType;
    private Map<String, String> parameters;
    private List<ImportDeclaration> imports;

    public MethodInfo(String name, String returnType, Map<String, String> parameters, List<ImportDeclaration> importList){
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.imports = importList;
    }

    public MethodInfo(String name, String returnType, String parameterStr, String importStr){
        this.name = name;
        this.returnType = returnType;
        this.parameters = parseDictInfo(parameterStr);
        this.imports = parseImportStr(importStr);
    }

    public List<ImportDeclaration> parseImportStr(String importStr){
        List<ImportDeclaration> importList = new ArrayList<ImportDeclaration>();
        for(String oneImport:importStr.split("\n")){
            if(oneImport.length()>0)
                importList.add(StaticJavaParser.parseImport(oneImport));
        }
        return importList;
    }

    public Map<String, String> parseDictInfo(String dictInfoStr){
        Map<String, String> dictInfoMap = new LinkedHashMap<String, String>();
        // System.out.println("dictInfoStr:"+dictInfoStr);
        if(dictInfoStr.equals("{\"\": \"\"}") || dictInfoStr.equals("{}")){
            // System.out.println("No content in this column.");
        }
        else{
            dictInfoStr = dictInfoStr.substring(1,dictInfoStr.length()-1);
            String[] dictInfoArray = dictInfoStr.split(", ");
            for(String oneDictInfo : dictInfoArray){
                String[] dictInfoPair = oneDictInfo.split(": ");
                dictInfoMap.put(dictInfoPair[0].replace("\"",""), dictInfoPair[1].replace("\"",""));
                // System.out.println(dictInfoPair[0]+" "+dictInfoPair[1]);
            }
        }
        return dictInfoMap;
    }

    public String getMethodName(){
        return this.name;
    }

    public String getReturnType(){
        return this.returnType;
    }

    public Map<String, String> getMethodParam(){
        return this.parameters;
    }

    public List<ImportDeclaration> getImportList(){
        return this.imports;
    }
}