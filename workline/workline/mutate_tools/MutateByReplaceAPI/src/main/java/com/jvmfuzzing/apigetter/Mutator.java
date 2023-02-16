package com.jvmfuzzing.apigetter;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.comments.LineComment;

import com.jvmfuzzing.generator.VariableGenerater;
import com.jvmfuzzing.generator.VariableComponent;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// import com.jvmfuzzing.apigetter.GetFiles;
// import com.jvmfuzzing.apigetter.MethodInfo;
// import com.jvmfuzzing.apigetter.ClassInfo;

public class Mutator {
    private static GetFiles opFile = new GetFiles();
    private static DBOperation opDatabase = new DBOperation();
    private static VariableGenerater generator = new VariableGenerater();

    // private static List<String> referFiles = opFile.readText("/JVMfuzzing/wy/JavaTailor-CFSynthesis-81ef055/02Benchmarks/HotspotTests-Java/testcases.txt");
    

    public List<CompilationUnit> callMutator(CompilationUnit seedCu){
        List<CompilationUnit> resultCuList = new ArrayList<CompilationUnit>();
        // Get the extracted method.
        List<MethodDeclaration> seedMethodList = seedCu.findAll(MethodDeclaration.class);
        for(int i=0;i<seedMethodList.size()-1;i++){
            MethodDeclaration seedMethod = seedMethodList.get(i);
            List<TestcaseInfo> testcaseInfoList = mutateInEachMethod(seedCu, i);
            for(TestcaseInfo testcaseInfo:testcaseInfoList){
                resultCuList.add(StaticJavaParser.parse(testcaseInfo.getTestcase()));
            }
        }
        return resultCuList;
    }

    public List<TestcaseInfo> mutateByReplaceAPI(CompilationUnit seedCu){
        List<TestcaseInfo> testcaseList = new ArrayList<TestcaseInfo>();
        // Get the extracted method.
        List<MethodDeclaration> seedMethodList = seedCu.findAll(MethodDeclaration.class);
        for(int i=0;i<seedMethodList.size()-1;i++){
            MethodDeclaration seedMethod = seedMethodList.get(i);
            // testcaseList.addAll(mutateInEachMethod(seedMethod, seedClassName, seedVarList, seedCu.toString()));
            testcaseList.addAll(mutateInEachMethod(seedCu, i));
        }
        return testcaseList;
    }

    private int classNameCount = 1;
    private String needfulImportStmt = "";
    private String regex = "new ([A-Za-z]+)\\(.*?\\)";
    private Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

    public List<TestcaseInfo> mutateInEachMethod(CompilationUnit seedCu, int methodIndex){
        List<TestcaseInfo> testcaseList = new ArrayList<TestcaseInfo>();
        CompilationUnit seedCuCopy = seedCu.clone();
        MethodDeclaration seedMethodCopy = seedCuCopy.findAll(MethodDeclaration.class).get(methodIndex);
        // Get file(class) name.
        String seedClassName = seedCu.findFirst(ClassOrInterfaceDeclaration.class).get().getNameAsString();
        // Get all variable declarations.
        List<VariableDeclarator> seedVarList = seedCu.findAll(VariableDeclarator.class);
        // Get all method call expression.
        List<MethodCallExpr> seedCallExprs = seedMethodCopy.findAll(MethodCallExpr.class);
        Collections.reverse(seedCallExprs);
        for(MethodCallExpr oneSeedCall:seedCallExprs){
            // TODO.Save information longer time.
            // Search useful class info for each expression.
            List<ClassInfo> allClassInfoList = new ArrayList<ClassInfo>();
            // System.out.println("MethodCallExpr:"+oneSeedCall);
            oneSeedCall.getScope().ifPresent(scope -> {
                String realType = scope.toString();
                Matcher matcher = pattern.matcher(realType);
                if(matcher.find()) {
                    realType = matcher.group(1);
                }
                else{
                    for(VariableDeclarator oneSeedVar:seedVarList){
                        if(oneSeedVar.getNameAsString().equals(realType)){
                            realType = oneSeedVar.getTypeAsString();
                        }
                    }
                }
                List<ClassInfo> classInfoList = opDatabase.existInTable(realType, oneSeedCall.getNameAsString());
                if(classInfoList.size()>0){
                    // TODO.If here are more than one class,how to check it?
                    if(classInfoList.get(0).getMethodList().size()>0){
                        // allClassInfoList.addAll(getInfoFromDatabase(oneSeedCall.getNameAsString()));
                        allClassInfoList.addAll(classInfoList);
                        // System.out.println("Get from database.");
                    }
                    // else{
                    //     allClassInfoList.addAll(getInfoFromTestsuits(oneSeedCall.getNameAsString(), referFiles));
                    //     // System.out.println("Get from Testsuits.");
                    // }
                }
            });
            // Use each useful methodinfo in classinfo to replace this method call expression.
            String oldMethodStr = seedMethodCopy.toString();
            // System.out.println("ClassInfo num:"+allClassInfoList.size());
            for(ClassInfo oneClassInfo:allClassInfoList){
                List<MethodInfo> methodInfoList = oneClassInfo.getMethodList();
                // System.out.println("MethodInfo num:"+methodInfoList.size());
                for(MethodInfo oneMethodInfo:methodInfoList){
                    if(oneMethodInfo.getMethodName().length() <= 0){
                        continue;
                    }
                    // Get clear copy of import, method and cu.
                    needfulImportStmt = "";
                    seedCuCopy = seedCu.clone();
                    seedMethodCopy = seedCuCopy.findAll(MethodDeclaration.class).get(methodIndex);            
                    // Get new method which method call expression has been replaced.
                    // System.out.println("MethodInfo:"+oneMethodInfo.getMethodName()+" param:"+oneMethodInfo.getMethodParam());
                    String newMethodStr = replaceMethodCall(oneSeedCall.clone(), oneMethodInfo, seedVarList, seedMethodCopy.clone());
                    if(newMethodStr.length() <= 0 || oldMethodStr.equals(newMethodStr)){
                        continue;
                    }
                    // System.out.println("Start to change:"+oldMethodStr+" -> "+newMethodStr);
                    // Get needful import statement in methodinfo.
                    for(ImportDeclaration oneImportStmt:oneMethodInfo.getImportList()){
                        needfulImportStmt += oneImportStmt.toString();
                    }
                    // Start to replace.
                    // System.out.println("Start to replace(Method):\n"+oldMethodStr+"->\n"+newMethodStr);
                    // System.out.println("Start to replace(Class):\n"+rawCode);
                    MethodDeclaration newMethodDec = (MethodDeclaration) StaticJavaParser.parseBodyDeclaration(newMethodStr);
                    // System.out.println(m1.toString());
                    seedMethodCopy.replace(seedMethodCopy.getBody().get(), newMethodDec.getBody().get());
                    // System.out.println("Success!\n"+seedMethodCopy.toString());
                    String rawCode = seedCuCopy.toString();
                    // String newCode = needfulImportStmt+rawCode.replace(seedClassName,"MyJVMTest_"+classNameCount);
                    String newCode = "// MutateByReplaceAPI\n" + needfulImportStmt+rawCode;
                    int classNum = Integer.valueOf(seedClassName.replace("MyJVMTest_", ""));
                    testcaseList.add(new TestcaseInfo(newCode, classNum, 1, 1));
                    // System.out.println("After change:\n"+newCode+"\n");
                    classNameCount++;
                    // break;
                }
            }
        }
        return testcaseList;
    }

    public List<ClassInfo> getInfoFromTestsuits(String seedCallerType, List<String> referFiles){
        List<ClassInfo> allClassInfoList = new ArrayList<ClassInfo>();
        for(String tmpFileName:referFiles){
            // Find methods in same class.
            if(tmpFileName.contains("."+seedCallerType+"\n")){
                String referFileName = "/JavaTailor-CFSynthesis-81ef055/02Benchmarks/HotspotTests-Java/src/"+tmpFileName.replace(".","\\");
                try{
                    FileInputStream referIn = new FileInputStream(referFileName);
                    CompilationUnit referCu = StaticJavaParser.parse(referIn);
                    List<ClassOrInterfaceDeclaration> allClasses = referCu.findAll(ClassOrInterfaceDeclaration.class);
                    for(ClassOrInterfaceDeclaration oneClass:allClasses){
                        List<MethodDeclaration> allMethods = oneClass.findAll(MethodDeclaration.class);
                        List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
                        for(MethodDeclaration oneMethod:allMethods){
                            methodInfoList.add(processMethodInfo(oneMethod, referCu.getImports()));
                        }
                        List<VariableDeclarator> globalVarList = new ArrayList<VariableDeclarator>();
                        oneClass.getFields().forEach(f -> {
                            globalVarList.addAll(f.getVariables());
                        });
                        ClassInfo classInfo = new ClassInfo(seedCallerType, tmpFileName, referCu.getImports(), globalVarList, methodInfoList);
                        allClassInfoList.add(classInfo);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
            }
            // TODO.Find methods in same package.
        }
        return allClassInfoList;
    }

    public MethodInfo processMethodInfo(MethodDeclaration oneMethod, List<ImportDeclaration> importList){
        Map<String, String> paramMap = new HashMap<String, String>();
        NodeList<Parameter> paraList = oneMethod.getParameters();
        if(paraList.size() > 0){
            // System.out.println("argument:"+paraList);
            for(int i=0;i<paraList.size();i++){
                Parameter para = paraList.get(i);
                paramMap.put(para.getNameAsString(), para.getTypeAsString());
            }
        }
        MethodInfo methodInfo = new MethodInfo(oneMethod.getNameAsString(), oneMethod.getTypeAsString(), paramMap, importList);
        return methodInfo;
    }

    Random random = new Random();
    
    public String replaceMethodCall(MethodCallExpr oneSeedCall, MethodInfo oneReferMethod, List<VariableDeclarator> seedVarList, MethodDeclaration visitMethod){
        String oldStmt = oneSeedCall.toString();
        // Replace the method name.
        oneSeedCall.setName(oneReferMethod.getMethodName());
        // If target method has its parameters,check first, if nothing can reuse, try to add.
        Map<String, String> targetParamMap = oneReferMethod.getMethodParam();
        Map<String, String> availableVarMap = getAvailableVar(seedVarList, oneSeedCall.getBegin().get().line);
        if(targetParamMap.size() > 0){
            NodeList<Expression> arguments = new NodeList<>();
            for(Map.Entry<String, String> oneParam:targetParamMap.entrySet()){
                // Check if there are available variables can reuse.
                List<String> sameTypeVar = getSameTypeVar(availableVarMap, oneParam.getValue());
                if(sameTypeVar.size() > 0){
                    String selectedVar = sameTypeVar.get(random.nextInt(sameTypeVar.size()));
                    // System.out.println("var:"+selectedVar);
                    arguments.add(new NameExpr(selectedVar));
                }
                else{
                    String thisVarName = oneParam.getKey();
                    // Check if there has variables with same name but different value.
                    String sameNameVar = availableVarMap.get(thisVarName);
                    if(thisVarName != null){
                        thisVarName += "a";
                    }
                    List<VariableComponent> varCompList = generator.generateDec(thisVarName, oneParam.getValue());
                    for(int i = 0;i<varCompList.size();i++){
                        VariableComponent varComp = varCompList.get(i);
                        String tmpVarDec = varComp.getVarDec();
                        // System.out.println("check var dec:"+tmpVarDec);
                        if(tmpVarDec.contains("null")){
                            return "";
                        }
                        String[] thisImportStmt = varComp.getImportStmt().split("\n");
                        for(String oneImportStmt:thisImportStmt){
                            if(!needfulImportStmt.contains(oneImportStmt))
                                needfulImportStmt += "import "+oneImportStmt+";\n";
                        }
                        try{
                            visitMethod.getBody().get().addStatement(i, StaticJavaParser.parseStatement(tmpVarDec));
                            // visitMethod.setComment(new LineComment("MutateByReplaceAPI"));
                            // System.out.println("add comment:\n"+visitMethod);
                        } catch (Exception e){
                            e.printStackTrace();
                            return "";
                        }
                    }
                    arguments.add(new NameExpr(thisVarName));
                }
            }
            oneSeedCall.setArguments(arguments);
        }
        else{
            oneSeedCall.getArguments().clear();
        }
        return visitMethod.toString().replace(oldStmt, oneSeedCall.toString());
    }

    public Map<String,String> getAvailableVar(List<VariableDeclarator> varList,int lineIndex){
        Map<String,String> varMap = new HashMap<String,String>();
        for(VariableDeclarator var:varList){
            if(var.getBegin().get().line < lineIndex){
                varMap.put(var.getNameAsString(),var.getTypeAsString());
            }
        }
        return varMap;
    }

    public List<String> getSameTypeVar(Map<String,String> availableVarMap, String type){
        return getKeysByStream(availableVarMap, type);
    }

    public <K, V>  List<K> getKeysByStream(Map<K, V> map, V value) {
        Set<K> result = map.entrySet()
            .stream()
            .filter(kvEntry -> Objects.equals(kvEntry.getValue(), value))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        return new ArrayList<>(result);
    }
}
