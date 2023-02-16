package com.jvmfuzzing.mutation;

import com.github.javaparser.ast.body.VariableDeclarator;

import java.util.*;

public class UsefulMethodInfo{
    String methodName;
    String paramInfoStr;
    String boundaryValueStr;

    public UsefulMethodInfo(String methodName, String paramInfoStr, String boundaryValueStr){
        this.methodName = methodName;
        this.paramInfoStr = paramInfoStr;
        this.boundaryValueStr = boundaryValueStr;
    }

    public String getMethodName(){
        return methodName;
    }

    public String getParamInfoStr(){
        return paramInfoStr;
    }

    public String getBoundaryValueStr(){
        return boundaryValueStr;
    }

    public Map<String, String> parseDictInfo(String dictInfoStr){
        Map<String, String> dictInfoMap = new LinkedHashMap<String, String>();
        Map<String, String> emptyMap = new LinkedHashMap<String, String>();
        // System.out.println("dictInfoStr:"+dictInfoStr);
        if(dictInfoStr.equals("{\"\": \"\"}") || dictInfoStr.equals("{}")){
            // System.out.println("No content in this column.");
        }
        else{
            dictInfoStr = dictInfoStr.substring(1,dictInfoStr.length()-1);
            String[] dictInfoArray = dictInfoStr.split(", ");
            for(String oneDictInfo : dictInfoArray){
                try{
                    String[] dictInfoPair = oneDictInfo.split(": ");
                    dictInfoMap.put(dictInfoPair[0].replace("\"",""), dictInfoPair[1].replace("\"",""));
                    // System.out.println(dictInfoPair[0]+" "+dictInfoPair[1]);                   
                } catch (Exception e){
                    e.printStackTrace();
                    return emptyMap;
                }
            }
        }
        return dictInfoMap;
    }
}