package com.jvmfuzzing.generator;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.expr.Expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

public class VariableComponent{
    String varType;
    String varName;
    Expression varValue;
    String importStmt = "";
    String varDec;

    public VariableComponent(){}

    public VariableComponent(String varType, String varName, String varValue){
        this.varType = varType;
        this.varName = varName;
        this.varDec = varType+" "+varName+" = "+varValue;
        this.varValue = formatVarValue(varDec);
    }

    public VariableComponent(String varType, String varName, String varValue, String importStmt){
        this.varType = varType;
        this.varName = varName;
        this.varDec = varType+" "+varName+" = "+varValue;
        this.varValue = formatVarValue(varDec);
        this.importStmt = importStmt;
    }

    private Expression formatVarValue(String varDec){
        Logger logger = LoggerFactory.getLogger(VariableComponent.class);
        Expression formativeVarValue;
        try{
            formativeVarValue = StaticJavaParser.parseVariableDeclarationExpr(varDec).getVariable(0).getInitializer().get();
        } catch(Exception e){
            logger.error(varDec+"\n\n"+e.getMessage());
            formativeVarValue = StaticJavaParser.parseExpression("null");
        }
        return formativeVarValue;
    }

    public String getVarType(){
        return varType;
    }

    public String getVarName(){
        return varName;
    }

    public Expression getVarValue(){
        return varValue;
    }

    public String getImportStmt(){
        return importStmt;
    }

    public String getVarDec(){
        return varDec+";\n";
    }
}