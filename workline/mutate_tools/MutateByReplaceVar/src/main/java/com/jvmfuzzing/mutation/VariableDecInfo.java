package com.jvmfuzzing.mutation;

public class VariableDecInfo{
    String varType;
    String varName;
    String varInitializer;
    String varDec;

    public VariableDecInfo(String varType, String varName, String varInitializer, String varDec){
        this.varType = varType;
        this.varName = varName;
        this.varInitializer = varInitializer;
        this.varDec = varDec;
    }

    public String getVarType(){
        return varType;
    }

    public String getVarName(){
        return varName;
    }

    public String getVarInitializer(){
        return varInitializer;
    }

    public String getVarDec(){
        return varDec;
    }
}