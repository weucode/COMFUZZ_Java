package com.jvmfuzzing.mutation_by_all;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;

import com.jvmfuzzing.generator.VariableGenerater;
import com.jvmfuzzing.generator.VariableComponent;
import com.jvmfuzzing.apigetter.*;
import com.jvmfuzzing.mutation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.sql.*;

public class Main {
    static VariableGenerater generator = new VariableGenerater();
    static ClassifyVariable classifier = new ClassifyVariable();
    static SelectDB s = new SelectDB();
    static MutationbyJump mutationbyJump = new MutationbyJump();
    static MutationbyInsertAndTypeCasting mutationbyInsertAndTypeCasting = new MutationbyInsertAndTypeCasting();
    static MutationbyTrue mutationbyTrue = new MutationbyTrue();
    static CreateJavaFile createFile = new CreateJavaFile();
    static MutationbyTimes mutationbyTimes = new MutationbyTimes();
    static MutationbyTrueAndInsert mutationbyTrueAndInsert = new MutationbyTrueAndInsert();
    static GenerateBinnary generateBinnary = new GenerateBinnary();
    static SaveToDB_new saveToDB_new1 = new SaveToDB_new();
    static mutationbyOperator mutationbyOperator = new mutationbyOperator();
    Object lock = new Object();

    com.jvmfuzzing.apigetter.Mutator apiMutator = new com.jvmfuzzing.apigetter.Mutator();
    com.jvmfuzzing.mutation.Mutator varMutator = new com.jvmfuzzing.mutation.Mutator();
    ReadTxt read = new ReadTxt();
    private static int number = 100;

    private static String filePath;

    public int getStmtLines(Statement visitStmt) {
        return visitStmt.getEnd().get().line - visitStmt.getBegin().get().line + 1;
    }

    public List<TestcaseInfo> Insert_Cov(List<TestcaseInfo> testcases) throws Exception {
        // prepare materials for mutation
        s.select_boolean_true();
        s.select_boolean_false();
        HashMap<String, String> bin_false = s.GetFalse();
        HashMap<String, String> bin_true = s.GetTrue();
        List<String> bin_true_par = new ArrayList<String>(bin_true.keySet());
        List<String> bin_false_par = new ArrayList<String>(bin_false.keySet());
        List<String> bin_true_expr = new ArrayList<String>(bin_true.values());
        List<String> bin_false_expr = new ArrayList<String>(bin_false.values());
        // start to mutate
        int file_num = 0;
        List<TestcaseInfo> mutatedTestcase = new ArrayList<TestcaseInfo>();
        for(TestcaseInfo testcase:testcases){
            mutatedTestcase.addAll(Insert(testcase, bin_true_par, bin_false_par, bin_true_expr, bin_false_expr));
            System.out.println("===mutatedTestcase(outer):"+mutatedTestcase.size());
            file_num++;
        }
        System.out.println("Total process " + file_num + " testcases.");
        return mutatedTestcase;
    }

    public Boolean hasOp(String content){
        String[] opList = new String[]{"+", "-", "/", "*", "%", "^", "||", "&&", "!", ">", "<", "~", "="};
        for(String op:opList){
            if(content.contains(op)){
                return true;
            }
        } 
        return false;
    }

    public String findRealNum(String content){
        String regex = "MyJVMTest_([0-9]+)";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
	matcher.find();
        return matcher.group(1);
    }

    public List<TestcaseInfo> Insert(TestcaseInfo testcase, List<String> bin_true_par, 
                            List<String> bin_false_par, List<String> bin_true_expr, List<String> bin_false_expr)
            throws IOException, SQLException, ClassNotFoundException, InterruptedException {

        Random random = new Random();
        List<String> for_stmt = s.select();
        List<HashMap<String, String>> needsetVarName_list = s.getNeedfHashMaps();
        int numss = 0;
        int for_num = 0;
        List<CompilationUnit> apiMutator_resultList = new ArrayList<>();

	    // System.out.println("check:\n"+testcase.getTestcaseContent());
        String realNum = findRealNum(testcase.getTestcaseContent());
        String filePath = this.filePath + "/MyJVMTest_" + realNum + ".java";
        FileInputStream in = new FileInputStream(filePath);
        System.out.println("Corresponding file:"+filePath);
        CompilationUnit cu = StaticJavaParser.parse(in);
        CompilationUnit cuCopy = cu.clone();
        
        // String name = cu.getStorage().get().getPath().toString();
        // String name_1 = cu.getStorage().get().getFileName();
        String name = filePath;

        List<TestcaseInfo> mutatedTestcase = new ArrayList<TestcaseInfo>();
        List<ClassOrInterfaceDeclaration> c1 = cu.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration c : c1) {
            List<MethodDeclaration> methods = c.getMethods();
            int size = methods.size() - 1;
            label1: for (int i = 0; i < size; i++) {
                MethodDeclaration m = methods.get(i);
                if (m.getBody().isPresent()) {
                    List<String> seed_content = new ArrayList<>();
                    seed_content.add(cu.toString());
                    // createFile.createFile(Seed_file, name_1, seed_content.get(0));
                    BlockStmt methodBody = m.getBody().get();
                    if (methodBody == null)
                        continue;
                    int num = random.nextInt(93);
                    HashMap<String, String> needsetVarName = new HashMap<>();
                    // System.out.println("---------------------Start ---------------------");
                    Statement test = StaticJavaParser.parseStatement(for_stmt.get(num));
                    if (test.toString().contains("TestFailure")) {
                        // System.out.println("------Insert Import-----");
                        ImportDeclaration importDeclaration = StaticJavaParser
                                .parseImport("import nsk.share.TestFailure;");
                        cu.addImport(importDeclaration);

                    }
                    if(needsetVarName_list.size()-1>=num)
                        needsetVarName = needsetVarName_list.get(num);
                    // System.out.println("needsetVarName" + needsetVarName);
                    List<Statement> stmt_gen = new ArrayList<Statement>();
                        
                    if(testcase.getProbability() == 0 && testcase.getInterestingTimes() == 0){
                        // Select universal mutation method
                        // 1-mutationbyJump 2-mutationbyTrue 3-mutatebyOp
                        List<Integer> availableMutants = new ArrayList<Integer>();
                        if (methodBody.toString().contains("break") || methodBody.toString().contains("continue")) {
                            availableMutants.add(1);
                        }
                        availableMutants.add(2);
                        if(hasOp(methodBody.toString())){                        
                            availableMutants.add(3);
                        }
                        // System.out.println("===:availableMutants:"+availableMutants.toString());
                        // Collections.shuffle(availableMutants);
                        // Integer chosenMutant = availableMutants.get(0);
                        if(availableMutants.contains(1)){
                            // System.out.println("-----mutationbyJump------");
                            CompilationUnit tmpCu = mutationbyJump.getOperator(cu);
                            TestcaseInfo tmpInfo = new TestcaseInfo(0, tmpCu.toString(), testcase.getId(), testcase.getMutationTimes()+1, 0, 0);
                            tmpInfo.setMutationMethod(1);
                            mutatedTestcase.add(tmpInfo);
                            cu = cuCopy;
                        }
                        if(availableMutants.contains(2)){
                            // System.out.println("-----mutationbyTrue------");
                            CompilationUnit tmpCu = mutationbyTrueAndInsert.Insert_new(cu, bin_true_par, bin_false_par, bin_true_expr,
                                    bin_false_expr);
                            TestcaseInfo tmpInfo = new TestcaseInfo(0, tmpCu.toString(), testcase.getId(), testcase.getMutationTimes()+1, 0, 0);
                            tmpInfo.setMutationMethod(2);
                            mutatedTestcase.add(tmpInfo);
                            cu = cuCopy;
                        }
                        if(availableMutants.contains(3)){
                            // System.out.println("-----mutationbyOp------");
                            CompilationUnit tmpCu = mutationbyOperator.getOperator(cu);
                            // System.out.print("===3-mutatebyop:\n"+cu.toString());
                            TestcaseInfo tmpInfo = new TestcaseInfo(0, tmpCu.toString(), testcase.getId(), testcase.getMutationTimes()+1, 0, 0);
                            tmpInfo.setMutationMethod(3);
                            mutatedTestcase.add(tmpInfo);
                            cu = cuCopy;
                        }
                    }
                    else{
                        // Select oriented mutation method
                        // 4-mutationbyTimes  5-mutationbyVarMutator 6-mutationbyApiMutator 7-mutationbyInsertAndTypeCasting
                        List<Integer> availableMutants = new ArrayList<Integer>();
                        List<CompilationUnit> varMutator_resultList = varMutator.callMutator(cu);
                        if (!varMutator_resultList.isEmpty()) {
                            availableMutants.add(5);
                        } else {
                            try{
                                apiMutator_resultList = apiMutator.callMutator(cu);
                            }
                            catch (Exception e){
                            }
                            if (!apiMutator_resultList.isEmpty()) {
                                availableMutants.add(6);
                            } else {
                                availableMutants.add(7);
                            }
                        }
                        if (methodBody.toString().contains("for")) {
                            availableMutants.add(4);
                        }
                        // Collections.shuffle(availableMutants);
                        // Integer chosenMutant = availableMutants.get(0);
                        if(availableMutants.contains(4)){
                            // System.out.println("-----mutationbyTimes------");
                            CompilationUnit tmpCu = mutationbyTimes.setVarNameInInsertStmt(cu, m, test, needsetVarName, name);
                            TestcaseInfo tmpInfo = new TestcaseInfo(0, tmpCu.toString(), testcase.getId(), testcase.getMutationTimes()+1, 0, 0);
                            tmpInfo.setMutationMethod(4);
                            mutatedTestcase.add(tmpInfo);
                            cu = cuCopy;
                        }
                        if(availableMutants.contains(5)){
                            // System.out.println("-----mutationbyVarMutator------");
                            // System.out.println("varMutator_resultList:\t" + varMutator_resultList.size());
                            CompilationUnit tmpCu = varMutator_resultList.get(random.nextInt(varMutator_resultList.size()));
                            TestcaseInfo tmpInfo = new TestcaseInfo(0, tmpCu.toString(), testcase.getId(), testcase.getMutationTimes()+1, 0, 0);
                            tmpInfo.setMutationMethod(5);
                            mutatedTestcase.add(tmpInfo);
                            cu = cuCopy;
                        }
                        if(availableMutants.contains(6)){
                            // System.out.println("-----mutationbyApiMutator------");
                            CompilationUnit tmpCu = apiMutator_resultList.get(random.nextInt(apiMutator_resultList.size()));
                            TestcaseInfo tmpInfo = new TestcaseInfo(0, tmpCu.toString(), testcase.getId(), testcase.getMutationTimes()+1, 0, 0);
                            tmpInfo.setMutationMethod(6);
                            mutatedTestcase.add(tmpInfo);
                            cu = cuCopy;
                        }
                        if(availableMutants.contains(7)){
                            // System.out.println("-----mutationbyInsertAndTypeCasting------");
                            CompilationUnit tmpCu = mutationbyInsertAndTypeCasting.setVarNameInInsertStmt(cu, m, test,
                                    needsetVarName, name);
                            TestcaseInfo tmpInfo = new TestcaseInfo(0, tmpCu.toString(), testcase.getId(), testcase.getMutationTimes()+1, 0, 0);
                            tmpInfo.setMutationMethod(7);
                            mutatedTestcase.add(tmpInfo);
                            cu = cuCopy;
                        }
                    }
                    // System.out.println("-------------End-------------");
                    break label1;
                }
                // System.out.println("I HERE");
            }
            // System.out.println("---------------------" + numss + "-----------------------------");
            numss++;
        }
        // System.out.println("===mutatedTestcase:"+mutatedTestcase.size());
        return mutatedTestcase;
    }

    public List<CompilationUnit> getCu(String path) throws IOException {
        Path pathToSource = Paths.get(path);
        SourceRoot sourceRoot = new SourceRoot(pathToSource);
        sourceRoot.tryToParse();
        List<CompilationUnit> compilations = FindJava(sourceRoot);
        return compilations;
    }

    // public List Decommon(List list) {
    //     HashSet set_list_for_var = new HashSet(list);
    //     list.clear();
    //     list.addAll(set_list_for_var);
    //     return list;
    // }

    public int generateChooseNum(int min, int max, boolean isLine) {
        // System.out.println("min:" + min + " max:" + max);
        if ((min >= max) && isLine) {
            return 1;
        } else if ((min >= max) && !isLine) {
            return 0;
        }
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public void addStmtBetweenStmt(BlockStmt methodBody, int stmtIndex, Statement insertStmt) {
        methodBody.addStatement(stmtIndex, insertStmt);
    }

    // public void addImport_forcu(CompilationUnit cu, List<ImportDeclaration> list_ImportDeclarations) {
    //     for (ImportDeclaration Imp : list_ImportDeclarations) {
    //         cu.addImport(Imp);
    //     }
    // }

    public void setVarName(Statement visitStmt, String oldName, String newName) {
        visitStmt.findAll(NameExpr.class).forEach(n -> {
            if (n.getNameAsString().equals(oldName)) {
                n.setName(newName);
            }
        });
    }

    public List<Statement> callGenerateDec(CompilationUnit cu, String name, String type) throws NullPointerException {
        // The variable that holds the result.
        List<Statement> varStmt = new ArrayList<Statement>();
        // Call generator_new.
        // System.out.println("---------------start callGenerateDec---------");
        List<VariableComponent> varComp = generator.generateDec(name, type);
        for (VariableComponent vc : varComp) {
            // Get the components of variables and assemble, such as:int a = 1;\n
            if (StaticJavaParser.parseStatement(
                    vc.getVarType() + " " + vc.getVarName() + " = " + vc.getVarValue().toString() + ";\n") == null)
                continue;
            varStmt.add(StaticJavaParser.parseStatement(
                    vc.getVarType() + " " + vc.getVarName() + " = " + vc.getVarValue().toString() + ";\n"));
            // If the generated parameters introduce new types, check and add them in cu.
            if (vc.getImportStmt().length() > 0) {
                Arrays.asList(vc.getImportStmt().split("\n")).forEach(s -> {
                    if (!cu.getImports().stream().anyMatch(i -> i.toString().equals(s))) {
                        cu.addImport(s);
                    }
                });
            }
        }
        return varStmt;
    }

    public List<Statement> setVarNameInInsertStmt(CompilationUnit cu, MethodDeclaration visitMethod,
            Statement insertStmt, HashMap<String, String> needfulParam) throws NullPointerException {
        List<Statement> newVarsDec = new ArrayList<Statement>();
        List<String> typeList = Arrays.asList("float", "long", "int", "char", "short", "byte");
        List<String> typeCast = Arrays.asList("double", "float", "long", "int", "char", "short");
        Map<String, String> commonType_differName = new HashMap<String, String>();
        Map<String, String> availableVar = classifier.getAvailableVar(cu.findAll(FieldDeclaration.class), visitMethod,
                insertStmt.getBegin().get().line);

        String name_changed = "";
        List<String> list_avar_key = new ArrayList<String>(availableVar.keySet());
        List<String> list_avar_vale = new ArrayList<String>(availableVar.values());
        NodeList<Parameter> Par = visitMethod.getParameters();
        for (Parameter par_m : Par) {
            list_avar_key.add(par_m.getName().asString());
            list_avar_vale.add(par_m.getType().asString());
        }
        // {key:value} {var_name:var_type}
        List<String> list_need_key = new ArrayList<String>(needfulParam.keySet());
        List<String> list_need_vale = new ArrayList<String>(needfulParam.values());
        HashMap<String, String> common = new HashMap<String, String>();
        HashMap<String, String> uncommon = new HashMap<String, String>();
        // System.out.println("Start setVarNameInInsertStmt");
        int numms = 0;
        if (availableVar.size() != 0) {
            for (int index = 0; index < list_avar_vale.size(); index++) {
                for (int index_need = 0; index_need < list_need_key.size(); index_need++) {
                    // Judge if the name of the known type is not the same, put it in common, but only one can be modified.
                    if (list_avar_vale.get(index).equals(list_need_vale.get(index_need))) {
                        if (!list_avar_key.get(index).equals(list_need_key.get(index_need)) && (numms == 0)) {
                            numms++;
                            common.put(list_need_key.get(index_need), list_avar_key.get(index));
                        }

                    } else {
                        if (list_avar_key.get(index).equals(list_need_key.get(index_need))) {
                            // set(old, new)
                            setVarName(insertStmt, list_need_key.get(index_need),
                                    list_need_key.get(index_need) + "_change");
                            uncommon.put(list_need_key.get(index_need) + "_change", list_need_vale.get(index_need));
                        }
                        uncommon.put(list_need_key.get(index_need), list_need_vale.get(index_need));
                    }

                }
            }
        } else {
            // System.out.println("uncommon" + needfulParam);
            uncommon = needfulParam;
        }
        // System.out.println("common:" + common);
        int common_num = 0;
        for (Map.Entry<String, String> entry_com : common.entrySet()) {
            setVarName(insertStmt, entry_com.getKey(), entry_com.getValue());
            name_changed = entry_com.getKey();
            common_num++;
            break;
        }
        String delete = "";
        uncommon.remove(name_changed);
        if (uncommon.size() != 0) {
            // System.out.println("uncommon:\t" + uncommon);
            int num = 0;
            for (Map.Entry<String, String> entry : uncommon.entrySet()) {
                for (int i = 0; i < 6; i++) {
                    // if(!(entry.getValue().equals(typeList.get(i))&&(!entry.getKey().equals(name_changed)))){
                    if (!entry.getValue().equals(typeList.get(i))) {
                        commonType_differName.put(entry.getKey(), entry.getValue());
                    } else {
                        newVarsDec.addAll(callGenerateDec(cu, "myjvm" + num, typeCast.get(i)));
                        newVarsDec.add(StaticJavaParser.parseStatement(entry.getValue() + " " + entry.getKey() + " = "
                                + "(" + typeList.get(i) + ")" + "myjvm" + num + ";\n"));
                        delete = entry.getKey();
                        num++;
                    }
                    commonType_differName.remove(delete);
                }

            }
            commonType_differName.remove(name_changed);
            // System.out.println("commonType_differName" + commonType_differName);
            for (Map.Entry<String, String> entry1 : commonType_differName.entrySet()) {
                newVarsDec.addAll(callGenerateDec(cu, entry1.getKey(), entry1.getValue()));
            }
        }

        // System.out.println("setVarName" + insertStmt);
        newVarsDec.add(insertStmt);
        return newVarsDec;
    }

    public List<CompilationUnit> FindJava(SourceRoot sourceRoot) throws IOException {
        return sourceRoot.getCompilationUnits();
    }

    public Map<String, String> getAvailableVar(List<FieldDeclaration> fieldList, MethodDeclaration visitMethod,
            int lineIndex) {
        Map<String, String> varMap = new HashMap<String, String>();
        for (FieldDeclaration field : fieldList) {
            for (VariableDeclarator var : field.getVariables()) {
                varMap.put(var.getNameAsString(), var.getTypeAsString());
            }
        }
        List<VariableDeclarator> varList = visitMethod.findAll(VariableDeclarator.class);
        for (VariableDeclarator var : varList) {
            if (var.getBegin().get().line < lineIndex) {
                varMap.put(var.getNameAsString(), var.getTypeAsString());
            }
        }
        return varMap;
    }

    public HashMap<Statement, MethodDeclaration> GetForAndMethodList(String path)
            throws IOException, IllegalArgumentException, SQLException, ClassNotFoundException {
        Path pathToSource = Paths.get(path);
        SourceRoot sourceRoot = new SourceRoot(pathToSource);
        sourceRoot.tryToParse();
        List<CompilationUnit> compilations = FindJava(sourceRoot);
        HashMap<Statement, MethodDeclaration> for_1 = new HashMap<>();
        List<Statement> list = new ArrayList<>();
        List<MethodDeclaration> Method = new ArrayList<>();

        for (CompilationUnit cu : compilations) {
            // String name = cu.getStorage().get().getPath().toString();
            // String name_1 = cu.getStorage().get().getFileName();
            // System.out.println(name);
            List<ClassOrInterfaceDeclaration> c1 = cu.findAll(ClassOrInterfaceDeclaration.class);

            for (ClassOrInterfaceDeclaration c : c1) {

                List<MethodDeclaration> methods = c.getMethods();
                for (MethodDeclaration m : methods) {
                    if (m.getBody().isPresent()) {
                        List<Statement> statementList = m.getBody().get().getStatements();
                        for (Statement stmt : statementList) {
                            // Judge whether it is a for statement and store it by the format {for, [variable type, variable]}.
                            if (stmt.isForStmt()) {
                                list.add(stmt);
                                Method.add(m);
                                for_1.put(stmt, m);
                            }
                        }
                    }
                }
            }
        }
        return for_1;
    }

    public static void main(String[] args)throws Exception, IllegalArgumentException, FileNotFoundException, NullPointerException, IOException,SQLException, ClassNotFoundException, SQLException, SQLSyntaxErrorException {
        Main mainner = new Main();
        mainner.filePath = args[0];
        String query = "SELECT * FROM Table_Testcase where Fuzzing_times=1;";
        List<TestcaseInfo> testcases = s.getAllTestcases(query);
        System.out.println("Totoally get "+testcases.size()+" origin testcases.");
        List<TestcaseInfo> mutatedTestcase = mainner.Insert_Cov(testcases);
        System.out.println("Totoally get "+mutatedTestcase.size()+" mutated testcases.");
        saveToDB_new1.insertIntoTable(mutatedTestcase);
        System.out.println("Finish to insert!");
    }
}
