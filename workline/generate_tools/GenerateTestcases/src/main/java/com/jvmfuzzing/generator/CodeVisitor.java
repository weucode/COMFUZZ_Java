package com.jvmfuzzing.generator;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static com.github.javaparser.ast.Modifier.Keyword.STATIC;
import static com.github.javaparser.ast.Modifier.Keyword.DEFAULT;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

public class CodeVisitor{
    int failNum = 0, hasParaNum = 0, noParaNum = 0, canNotGenerateNum = 0, callItselfNum = 0;
    static private VariableGenerater varGenerater = new VariableGenerater();
    static private final Logger logger = LoggerFactory.getLogger(CodeVisitor.class);
    
    public void setAllMethodStatic(List<MethodDeclaration> allMethod){
        for(MethodDeclaration oneMethod:allMethod){
            oneMethod.setStatic(true);
        }
    }

    // public ClassOrInterfaceDeclaration addThrowExceptionToMain(ClassOrInterfaceDeclaration classNode,MethodDeclaration mainMethod){
    //     List<MethodDeclaration> methodList = classNode.getMethods();
    //     MethodDeclaration extractedMethod = classNode.findAll(MethodDeclaration.class).get(methodList.size()-2);
    //     // MethodDeclaration mainMethod = classNode.getMethodsByName("main").get(0);
    //     extractedMethod.getThrownExceptions().forEach(throwStmt -> {
    //         mainMethod.addThrownException(throwStmt);
    //     });
    //     // System.out.println("after add thrown expr:\n"+classNode.toString());
    //     return classNode;
    // }

    public void addThrowExceptionToMain(MethodDeclaration mainMethod){
        mainMethod.addThrownException(new ClassOrInterfaceType("Exception"));
    }

    public String getNameWithMaxCount(Map<String, List<Object>> map){
        int maxValue = 0;
        String maxName = "";
        for(Map.Entry<String, List<Object>> entry:map.entrySet()){
            int tmpValue = (int)entry.getValue().get(1);
            if(tmpValue>=maxValue){
                maxValue = tmpValue;
                maxName = entry.getKey();
            }
        }
        return maxName;
    }

    public void setMethodReturnType(MethodDeclaration n){
        // Collect all variables.
        // List<CountOfVarUsage> countOfVarUsageList = new ArrayList<CountOfVarUsage>();
        Map<String,List<Object>> varMap = new HashMap<String,List<Object>>();
        for(Parameter para:n.getParameters()){
            String paraName = para.getNameAsString();
            String paraType = para.getType().toString();
            varMap.put(paraName,new ArrayList<Object>(Arrays.asList(paraType,0)));
            // System.out.println("put param:"+paraName);
        }
        for(VariableDeclarator varDeclarator:n.getBody().get().findAll(VariableDeclarator.class)){
            if(varDeclarator.findAncestor(IfStmt.class).isPresent()||
            varDeclarator.findAncestor(ForStmt.class).isPresent()||
            varDeclarator.findAncestor(WhileStmt.class).isPresent()||
            varDeclarator.findAncestor(TryStmt.class).isPresent()){
                continue;
            }
            String varName = varDeclarator.getNameAsString();
            String varType = varDeclarator.getType().toString();
            if(!varName.equals("i")){
                varMap.put(varName,new ArrayList<Object>(Arrays.asList(varType,0)));
                // System.out.println("put var:"+varName);
            }
        }
        // Start to count.
        List<NameExpr> expressionList = n.getBody().get().findAll(NameExpr.class);
        expressionList.forEach(e -> {
            // System.out.println(e.toString());
            String varName = e.getNameAsString();
            if(varMap.containsKey(varName)){
                List<Object> varInfo = varMap.get(varName);
                varInfo.set(1,(int)varInfo.get(1)+1);
            }
        });
        // Get the max value.
        String maxName = getNameWithMaxCount(varMap);
        // System.out.println("maxName: "+maxName);
        // Add the return statement and set the return type.
        n.getBody().get().addStatement(new ReturnStmt(new NameExpr(maxName)));
        // n.addStatement("return "+maxName+";");
        String returnType = varMap.get(maxName).get(0).toString();
        n.setType(new ClassOrInterfaceType(returnType));
        // System.out.println("after set return type:\n"+n.toString());
    }
    
    public boolean hasCallItself(MethodDeclaration n){
        List<MethodCallExpr> allMethodCall = n.findAll(MethodCallExpr.class);
        for(MethodCallExpr oneMethodCall:allMethodCall){
            if(oneMethodCall.getNameAsString().equals(n.getNameAsString())){
                return true;
            }
        }
        return false;
    }
    
    public MainMethodComponent visitMethod(List<String> globalVarList,MethodDeclaration n, String newClassName) {

        String allParaName="";
        List<VariableComponent> needfulArgList = new ArrayList<VariableComponent>();
        // Get all variables,and generate declaration for them.
        NodeList<Parameter> paraList = n.getParameters();
        if(paraList.size() == 0){
            // System.out.println("No need to generate parameters!");
            this.noParaNum++;
        }
        else{
            // System.out.println("argument:"+paraList);
            this.hasParaNum++;
            for(int i=0;i<paraList.size();i++){
                Parameter para = paraList.get(i);
                String paraName = para.getNameAsString();
                // Check if the parameter has a same name with a global variable.
                if(globalVarList.contains(paraName)){
                    // System.out.println("\nglobal var:"+paraName);
                }
                else{
                    // System.out.println("\nlocal var:"+paraName);
                    String paraType = para.getTypeAsString().replace("java.lang.","").replace("java.io.","").replace("java.util.","");
                    // System.out.println("parameter:"+paraName+"-"+paraType+"-");
                    // Generate the declaration of parameter.
                    needfulArgList.addAll(varGenerater.generateDec(paraName,paraType));
                }
                // Generate the parameter expression in call statement.
                allParaName += paraName;
                if(i<paraList.size()-1 && paraList.size()>1)
                    allParaName += ",";
            }  
        }
        // Start to add the call statement.There are 3 special cases:(1)void;(2)array;(3)map.
        String runStmt = "";
        String methodType = n.getTypeAsString();
        String methodName = n.getNameAsString();
        // First consider the situation when the method type is void.
        if(methodType.equals("void") && paraList.size()>0){
            setMethodReturnType(n);
        }
        // If the modification is not successful,just call it and do not print.
        if(methodType.equals("void")){
            runStmt = "new "+newClassName+"()."+methodName+"("+allParaName+");\n";
        }
        else if(methodType.contains("[]")){
            int dimension = varGenerater.strCount(methodType,"[]");
            if(dimension >= 2){
                for(int i=0;i<dimension;i++)
                    runStmt += "\tSystem.out.println(Arrays.asList(new "+newClassName+"()."+methodName+"("+allParaName+")["+String.valueOf(i)+"]));\n";
                    // System.out.println("runStmt:"+runStmt);
            }
            else
                runStmt = "\tSystem.out.println(Arrays.asList(new "+newClassName+"()."+methodName+"("+allParaName+")));\n";
        }
        else if(methodType.contains("Map")){
            runStmt = "\tnew "+newClassName+"()."+methodName+"("+allParaName+").forEach((a,b)->System.out.println(a+b));";
        }
        else
            runStmt = "\tSystem.out.println(new "+newClassName+"()."+methodName+"("+allParaName+"));\n";
        // Instance all results.
        MainMethodComponent mainMethod = new MainMethodComponent(needfulArgList,runStmt,VariableGenerater.implementationClassList);
        VariableGenerater.implementationClassList.clear();
        return mainMethod;
    }

    public boolean alreadyIn(NodeList<ImportDeclaration> importList,String importName){
        for(ImportDeclaration oneImport:importList){
            String tmpImportName = oneImport.getNameAsString();
            // String tmpImportName = oneImport.getNameAsString().substring(oneImport.getNameAsString().lastIndexOf("."));
            // System.out.println("tmpImportName:"+tmpImportName+" importName:"+importName);
            if(tmpImportName.equals(importName))
                return true;
        }
        return false;
    }

    public void checkAndAddImports(CompilationUnit cu,String oneImportStr){
        String[] realImportArray = oneImportStr.split("\n");
        for(String oneImport:realImportArray){
            if(!alreadyIn(cu.getImports(),oneImport))
                cu.addImport(oneImport);
        }
    }
    
    public void visitGloablVar(ClassOrInterfaceDeclaration classNode){
        // Set all fields static,and if there are some fields not initialized,generate value for them.
        for(FieldDeclaration field : classNode.getFields()){
            field.setStatic(true);
            field.getVariables().forEach(v -> {
                if(!v.getInitializer().isPresent()){
                    String fieldName = v.getNameAsString();
                    String fieldType = v.getTypeAsString().replace("java.lang.","").replace("java.io.","").replace("java.util.","");
                    v.setInitializer(varGenerater.generateValueOfDec(fieldType));
                    // System.out.println("impl class:"+varGenerater.implementationClassList);
                    if(varGenerater.implementationClassList.size()>0){
                        varGenerater.implementationClassList.forEach(imp -> checkAndAddImports(v.findCompilationUnit().get(),imp));
                    }
                }
            });
        }
    }
    
    public String createTestcase(CompilationUnit oldCUnit, int id){
        String newClassName = "MyJVMTest_"+id;
        List<MethodDeclaration> allMethod = oldCUnit.findAll(MethodDeclaration.class);
        // Make all methods STATIC.
        // setAllMethodStatic(allMethod);
        // Get the extracted method.
        MethodDeclaration extractedMethod = allMethod.get(allMethod.size()-1);
        if(hasCallItself(extractedMethod)){
            // System.out.println("The extracted method has call itself!");
            callItselfNum++;
            return "";
        }
        // Visit the extracted method.
        List<String> globalVarList = oldCUnit.findAll(FieldDeclaration.class).stream().map(f -> f.getVariable(0).getNameAsString()).collect(Collectors.toList());
        // globalVarList.forEach(System.out::println);
        MainMethodComponent mainMethodComponent = visitMethod(globalVarList,extractedMethod,newClassName);
        // Prepare a new compilation unit.
        CompilationUnit newCUnit = new CompilationUnit();
        // Visit global variables in old class.
        ClassOrInterfaceDeclaration oldClass = oldCUnit.findAll(ClassOrInterfaceDeclaration.class).get(0);
        visitGloablVar(oldClass);
        // Add needful import.
        NodeList<ImportDeclaration> importList = oldCUnit.getImports();
        importList.forEach(importNode -> {
            newCUnit.addImport(importNode);
        });
        for(String implementationClass : mainMethodComponent.getImplementationClassList()){
            if(alreadyIn(importList,implementationClass))
                continue;
            else
                // newCUnit.addImport(implementationClass);
                checkAndAddImports(newCUnit,implementationClass);
        }
        // Create a new class node.
        ClassOrInterfaceDeclaration newClass = newCUnit.addClass(newClassName);
        // Add needful arguments as gloabl variables.
        List<VariableComponent> needfulArgList = mainMethodComponent.getParaList();
        try{
            for(int i=0;i<needfulArgList.size();i++){
                VariableComponent oneComponent = needfulArgList.get(i);
                // System.out.println("add field:"+oneComponent.getVarType()+oneComponent.getVarName()+oneComponent.getVarValue());
                if(oneComponent.getVarType()==null || oneComponent.getVarName()==null || oneComponent.getVarValue()==null)
                    return "";
                newClass.addFieldWithInitializer(oneComponent.getVarType(),oneComponent.getVarName(),oneComponent.getVarValue(),STATIC);
                // newClass.addFieldWithInitializer(oneComponent.getVarType(),oneComponent.getVarName(),oneComponent.getVarValue(),DEFAULT);
                if(oneComponent.getImportStmt().length() > 0){
                    // newCUnit.addImport(oneComponent.getImportStmt());
                    checkAndAddImports(newCUnit,oneComponent.getImportStmt());
                }
            }
        } catch (Exception e){
            logger.error("Error when add needful arguments as gloabl variables!\n"+e.getMessage());
            return "";
        }       
        // Add all gloable variables and methods in the old class node.
        for(BodyDeclaration<?> member:oldClass.getMembers()){
            newClass.addMember(member);
        }
        // Add the main method.
        MethodDeclaration mainMethod = newClass.addMethod("main",PUBLIC,STATIC);
        BlockStmt methodBody = mainMethod.createBody();
        String callStmtStr = mainMethodComponent.getCallStatement();
        String[] callStmtArray = callStmtStr.split("\n");
        for(String callStmt:callStmtArray)
            methodBody.addStatement(callStmt);
        mainMethod.addParameter(String[].class,"args");
        // newClass = addThrowExceptionToMain(newClass,mainMethod);
        addThrowExceptionToMain(mainMethod);
        // If return type is array,add related import statement.
        if(callStmtStr.contains("Arrays"))
            checkAndAddImports(newCUnit,"java.util.Arrays");
        // Try to parse new compilation unit.
        String code = "";
        try{
            code = newCUnit.toString();
            // System.out.println("code:\n"+code);
        }
        catch(Exception e){
            this.failNum++;
            logger.error("Content:\n{}Error message:\n{}",StringUtils.join(needfulArgList,"\n"),e.getMessage()); 
            return "";  
        }
        return code;        
    }

    class MainMethodComponent{
        private List<VariableComponent> paraList;
        private String callStatement;
        private List<String> implementationClassList;

        public MainMethodComponent(List<VariableComponent> paraList, String callStatement, List<String> implementationClassList){
            this.paraList = paraList;
            this.callStatement = callStatement;
            this.implementationClassList = implementationClassList;
        }

        public List<VariableComponent> getParaList(){
            return paraList;
        }

        public String getCallStatement(){
            return callStatement;
        }

        public List<String> getImplementationClassList(){
            return implementationClassList;
        }
    }
}