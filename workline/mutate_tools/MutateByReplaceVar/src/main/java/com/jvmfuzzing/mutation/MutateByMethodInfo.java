package com.jvmfuzzing.mutation;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.jvmfuzzing.generator.VariableGenerater;
import com.jvmfuzzing.generator.VariableComponent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.*;

import org.apache.commons.lang3.StringUtils;

public class MutateByMethodInfo{
    static List<String> primitiveTypeSet = new ArrayList<String>(Arrays.asList("int","short","long","byte","char","float","double","boolean"));
    static DBOperation operator = new DBOperation();
    
    class MutatedBlock{
        private String varName;
        private String targetVarStr;
        private String mutateVarStr;
        private List<String> importList;

        public MutatedBlock(String varName,String targetVarStr, String mutateVarStr,List<String> importList){
            this.varName = varName;
            this.targetVarStr = targetVarStr;
            this.mutateVarStr = mutateVarStr;
            this.importList = importList;
        }

        public String getVarName() {
            return varName;
        }

        public String getTargetVarStr(){
            return targetVarStr;
        }

        public String getMutateVarStr(){
            return mutateVarStr;
        }

        public List<String> getImportList(){
            return importList;
        }

        // public void addimportList(List<String> importList){
        //     this.importList.addAll(importList);
        // }
    }

    public boolean isUnprimitive(String type){
        type = type.replace("[]","");
        if(primitiveTypeSet.contains(type)){
            return false;
        }
        else{
            return true;
        }
    }

    public int generateChooseNum(int sizeNum){
        Random random = new Random();
        return random.nextInt(sizeNum);
    }

    public VariableDecInfo processVariableIntoInfo(VariableDeclarator varDec){
        String varName = varDec.getNameAsString();
        String varType = varDec.getTypeAsString();
        // String varDecStr = "    "+varType + " " + varDec.toString()+";\n";
        String varDecStr = varType + " " + varDec.toString()+";\n";
        if(varDec.getInitializer().isPresent()){
            VariableDecInfo varDecInfo = new VariableDecInfo(varType, varName, varDec.getInitializer().get().toString(),varDecStr);
            return varDecInfo;
        }
        else{
            VariableDecInfo varDecInfo = new VariableDecInfo(varType, varName, "",varDecStr);
            return varDecInfo;
        }
    }

    public String generateBoundaryValue(String varName, String varType,String boundaryValueStr){
        final String regex = "[0-9.-]+";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(boundaryValueStr);
        
        try{
            String boundaryValue = matcher.group(0);
            String newDec = varType+" "+varName+" = "+boundaryValue+";\n";
            return newDec;
        }
        catch(Exception e){
            return "";
        }
    }

    public MutatedBlock generateMutantDec(String blockNum, VariableDecInfo seedVarInfo, VariableDecInfo targetVarInfo, UsefulMethodInfo methodInfo,boolean processGlobal){
        String mutatedDec = "";
        String mutateArgStr = "";
        List<String> importList = new ArrayList<String>();
        VariableGenerater t = new VariableGenerater();
        // Save some frequently-used variables.
        String methodName = methodInfo.getMethodName();
        Map<String, String> paramInfoMap = methodInfo.parseDictInfo(methodInfo.getParamInfoStr());
        Map<String, String> boundaryValueMap = methodInfo.parseDictInfo(methodInfo.getBoundaryValueStr());
        String seedVarName = seedVarInfo.getVarName();
        String targetVarType = targetVarInfo.getVarType();
        String targetVarName = targetVarInfo.getVarName();
        // System.out.println("seedVarName:"+seedVarName);
        // System.out.println("targetVarName:"+targetVarName);
        if(paramInfoMap.size()>0){
            // If this method has boundary value,try to use.
            if(boundaryValueMap.size()>0){
                for(Map.Entry<String,String> entry:boundaryValueMap.entrySet()){
                    String paramOriginName = entry.getKey();
                    String paramNewName = entry.getKey()+"MutateArg"+blockNum;
                    String boundaryDec = generateBoundaryValue(paramNewName,paramInfoMap.get(paramOriginName),entry.getValue());
                    if(boundaryDec.length()>0){
                        mutatedDec += boundaryDec;
                        // System.out.println("mutatedDec(use boundary value):\n"+mutatedDec);
                        mutateArgStr += paramNewName+",";
                        paramInfoMap.remove(paramOriginName);
                    }
                }
            }
            for(Map.Entry<String, String> entry:paramInfoMap.entrySet()){
                String paramNewName = entry.getKey()+"MutateArg"+blockNum;
                String paramType = entry.getValue();
                List<VariableComponent> compList = t.generateDec(paramNewName,paramType);
                for(VariableComponent comp:compList){
                    mutatedDec += "static "+comp.getVarType()+" "+comp.getVarName()+" = "+comp.getVarValue()+";\n";
                    if(comp.getImportStmt().length() > 0){
                        importList.addAll(Arrays.asList(comp.getImportStmt().split("\n")));
                    }
                }
                // System.out.println("mutatedDec:\n"+mutatedDec);
                mutateArgStr += paramNewName+",";
            }
            mutateArgStr = mutateArgStr.substring(0,mutateArgStr.length()-1);
            String tmp = "";
            if(processGlobal)
                tmp = "static "+targetVarType+" "+targetVarName+" = "+seedVarName+"."+methodName+"("+mutateArgStr+");\n";
            else
                tmp = targetVarType+" "+targetVarName+" = "+seedVarName+"."+methodName+"("+mutateArgStr+");\n";
                // System.out.println("tmp:"+tmp);
            mutatedDec += tmp;
        }
        else{
            if(processGlobal)
                mutatedDec = "static "+targetVarType+" "+targetVarName+" = "+seedVarName+"."+methodName+"();\n";
            else
                mutatedDec = targetVarType+" "+targetVarName+" = "+seedVarName+"."+methodName+"();\n";
        }
        // System.out.println("check import size in mutate:"+importList.size());
        if(processGlobal)
            return new MutatedBlock(targetVarInfo.getVarName(),"static "+targetVarInfo.getVarDec(),mutatedDec,importList);
        else
            return new MutatedBlock(targetVarInfo.getVarName(),targetVarInfo.getVarDec(),mutatedDec,importList);
    }

    public List<MutatedBlock> muatateByMethodInfo(List<VariableDeclarator> seedVarGroup,List<VariableDeclarator> targetVarGroup,boolean processGlobal){
        String finalDec = "";
        String targetVarStr = "";
        List<MutatedBlock> blockList = new ArrayList<MutatedBlock>();
        Statement stmt = operator.createStatement(operator.createConnection());
        for(int j=0;j<seedVarGroup.size();j++){
            // Process seed variable.
            VariableDeclarator seedVarDec = seedVarGroup.get(j);
            VariableDecInfo seedVarInfo = processVariableIntoInfo(seedVarDec);
            // Start to try to find a list of variable declarations to mutate.
            String mutatedDec = "";
            for(int i=0,k=0;i<targetVarGroup.size();i++,k++){
                // Process target variable.
                VariableDeclarator targetVarDec = targetVarGroup.get(i);
                VariableDecInfo targetVarInfo = processVariableIntoInfo(targetVarDec);
                // Select in table Method_Info.
                String sql = "";
                if(seedVarInfo.getVarType().equals("AtomicIntegerArray"))
                    sql = "SELECT method_name,param_info,boundary_value FROM Quick_Method_Info WHERE class_name = '"+seedVarInfo.getVarType()+"' and return_type = '"+targetVarInfo.getVarType()+"'";
                else
                    sql = "SELECT method_name,param_info,boundary_value FROM Method_Info WHERE class_name = '"+seedVarInfo.getVarType()+"' and return_type = '"+targetVarInfo.getVarType()+"'";
                // System.out.println(sql);
                List<UsefulMethodInfo> methodInfoList = operator.getMethodInfo(stmt, sql); 
                // System.out.println("useful method size:"+methodInfoList.size()); 
                if(methodInfoList.size()>0){
                    int chooseNum = generateChooseNum(methodInfoList.size());
                    blockList.add(generateMutantDec(String.valueOf(i),seedVarInfo,targetVarInfo,methodInfoList.get(chooseNum),processGlobal));
                    targetVarGroup.remove(i);
                    i--;
                    // System.out.println("check remove:"+targetVarGroup.size());
                }
                // else{
                //     System.out.println("No useful method to mutate.");
                // }
            }
        }
        try{
            stmt.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return blockList;
    }
}