package com.jvmfuzzing.generator;

public class ConstructorInfo{
    String className;
    String constructorStmt;
    String packageName;

    public ConstructorInfo(String className, String constructorStmt,String packageName){
        this.className = className;
        this.constructorStmt = constructorStmt;
        this.packageName = packageName;
    }

    public String getClassName(){
        return className;
    }

    public String getConstructorStmt(){
        return constructorStmt;
    }

    public String getPackageName(){
        return packageName;
    }
}