package com.jvmfuzzing.generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

public class UnprimitiveGenerator{
    static String[] noNeedChangeSet = {"int","short","long","byte","char","float","double","String"};
    static final String variableNamePattern = "Param";
    static Logger logger = LoggerFactory.getLogger(UnprimitiveGenerator.class);

    public boolean isUnprimitive(String type){
        type = type.replace("[]","");
        if(Arrays.asList(noNeedChangeSet).contains(type)){
            return false;
        }
        else{
            return true;
        }
    }

    public int generateChooseNum(int min,int max) {
        // System.out.println("min:"+min+" max:"+max);
        if (min == max) {
            return min;
        }
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    class NoAvailableConstructorException extends Exception{
        NoAvailableConstructorException(String s){
            super(s);
        }
    }

    // public String getUsefulConstructor(String className) throws NoAvailableConstructorException{
    //     // Find in database.
    //     DBOperation operator = new DBOperation();
    //     // String sql = "SELECT Constructor_Stmt FROM API_Info WHERE Class_Name = '"+className+"';";
    //     ConstructorInfo constructorInfo = operator.getConstructorInfo(className);
    //     // If get it, select one randomly,or throw an exception.
    //     if(constructorInfo.getConstructorStmt().length() != 0){
    //         System.out.println("find all constructor:\n"+constructorStr);
    //         String[] constructorArray = constructorStr.split("\n");
    //         int chooseNum = generateChooseNum(constructorArray.length);
    //         return constructorArray[chooseNum];
    //     }
    //     else{
    //         throw new NoAvailableConstructorException("Do not find available constructor!");
    //     }
    // }

    String[] unaryOperator = {"++","--","+","-","~"};
    String[] binaryOperator = {"+","-","*","/","%","&","|","^",">>","<<"};
    List<String> availableFunctionalInterface = new ArrayList<String>(
        Arrays.asList("UnaryOperator","BinaryOperator","IntUnaryOperator","IntBinaryOperator","LongUnaryOperator","LongBinaryOperator","DoubleUnaryOperator","DoubleBinaryOperator","Runnable"));

    public VariableComponent generateFunctional(String varType,String varName){
        int chooseNum = 0;
        String lambdaStr = "",importStmt = "";
        switch(varType){
            case "UnaryOperator":case "DoubleUnaryOperator":case "LongUnaryOperator":case "IntUnaryOperator":
                chooseNum = generateChooseNum(0,unaryOperator.length);
                lambdaStr = "(jvmVarA) -> "+unaryOperator[chooseNum]+"jvmVarA";
                importStmt += "java.util.function."+varType;
                break;
            case "BinaryOperator":case "DoubleBinaryOperator":case "LongBinaryOperator":case "IntBinaryOperator":
                chooseNum = generateChooseNum(0,binaryOperator.length);
                lambdaStr = "(jvmVarA,jvmVarB) -> jvmVarA"+binaryOperator[chooseNum]+"jvmVarB";
                importStmt += "java.util.function."+varType;
                break;
            default:
                lambdaStr = "() -> {}";
        }
        return new VariableComponent(varType,varName,lambdaStr,importStmt);
    }

    public VariableComponent generateIntAsParam(String varName){
        return new VariableComponent("int",varName,String.valueOf(generateChooseNum(0,1000)),"");
    }

    public List<VariableComponent> generateUnprimitive(String varName, String varType){
        VariableGenerater varGenerater = new VariableGenerater();
        List<VariableComponent> compList = new ArrayList<VariableComponent>();
        String lambdaStr = "",rootImportStmt = "";
        // If type is 'Path',give it a special value.
        if(varType.equals("Path")){
            compList.add(new VariableComponent(varType,varName,"FileSystems.getDefault().getPath(\"logs\", \"access.log\")","import java.nio.file.FileSystems;\n"));
            return compList;
        }
        // If this is a functional interface, generate a lambda expression.
        if(availableFunctionalInterface.contains(varType)){
            compList.add(generateFunctional(varType,varName));
            return compList;
        }
        // Start to generate by using Constructor_Info.
        String implementingClassName = "",oneConstructor = "";
        ConstructorInfo constructorInfo = null;
        try{
            DBOperation operator = new DBOperation();
            constructorInfo = operator.getConstructorInfo(varType);
            String constructorStr = constructorInfo.getConstructorStmt();
            if(constructorStr.length() != 0){
                // System.out.println("find all constructor:\n"+constructorStr);
                // Choose a constructor.
                String[] constructorArray = constructorStr.split("\n");
                int chooseNum = generateChooseNum(0,constructorArray.length);
                oneConstructor = constructorArray[chooseNum];
                // System.out.println("choosed constructor:"+oneConstructor);
                // If this is an abstract class,save impementing class name.
                if(constructorInfo.getClassName().equals(varType) == false){
                    implementingClassName = constructorInfo.getClassName();
                    // System.out.println("here is an abstract class:"+varType+":"+implementingClassName);
                }
                rootImportStmt = constructorInfo.getPackageName()+"."+varType+"\n";
            }
            else{
                throw new NoAvailableConstructorException("Do not find available constructor!");
            }
        }
        catch (Exception e) {
            // System.out.println(e.getMessage());
            // logger.info("cannot generate:"+varType);
            compList.add(new VariableComponent(varType, varName, "null"));
            return compList;
        }
        String newClassValue = "", importStmt = rootImportStmt;
        // Here are four situation:is (or not) an abstract class,has (or not) parameters.
        if(implementingClassName.length() > 0){
            importStmt += constructorInfo.getPackageName()+"."+implementingClassName;
            // System.out.println("need to add import:\n"+importStmt);
            // If this constructor do not need parameter.
            if(oneConstructor.contains("()")){
                newClassValue = "new "+implementingClassName+"()";
                compList.add(new VariableComponent(varType, varName, newClassValue, importStmt));
                return compList;
            }
            else
                newClassValue = "new "+implementingClassName+"(";
        }
        else{
            // If this constructor do not need parameter.
            if(oneConstructor.contains("()")){
                newClassValue = "new "+varType+"()";
                compList.add(new VariableComponent(varType, varName, newClassValue, importStmt));
                return compList;
            }
            else
                newClassValue = "new "+varType+"(";
        }
        // Why not create a new method? Because there are two value changed.
        // Get all parameter, and generate declaration.
        String[] tmpArray = oneConstructor.split("\\(|, |,|\\)");
        for(int i=1;i<tmpArray.length-1;i++){
            String variableType = tmpArray[i];
            // System.out.println("type:"+variableType);
            String variableName = varName+UnprimitiveGenerator.variableNamePattern+String.valueOf(i);
            if(variableType.equals("int")){
                compList.add(generateIntAsParam(variableName));
            }
            else if(variableType.equals("?>")){
                compList.add(new VariableComponent());
            }
            else{
                compList.addAll(varGenerater.generateDec(variableName, variableType));
            }
            newClassValue += variableName;
            if(i<tmpArray.length-2)
                newClassValue += ", ";
        }
        newClassValue += ")";
        // System.out.println("newClassValue(has params):\n"+newClassValue);
        compList.add(new VariableComponent(varType, varName, newClassValue,importStmt));
        return compList;
    }
}