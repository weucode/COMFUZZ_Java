package com.jvmfuzzing.mutation_by_all;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.NodeList;

import com.jvmfuzzing.generator.VariableGenerater;
import com.jvmfuzzing.generator.VariableComponent;

import java.util.*;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.sql.*;

public class MutationbyTrueAndInsert {
    static VariableGenerater generator = new VariableGenerater();
    static ClassifyVariable classifier = new ClassifyVariable();
    static CreateJavaFile createJavaFile = new CreateJavaFile();
    static SelectDB s = new SelectDB();


    Random random = new Random();

    public int getStmtLines(Statement visitStmt) {
        return visitStmt.getEnd().get().line - visitStmt.getBegin().get().line + 1;
    }

    public Statement addStmtInStmt(Statement visitStmt, String Stmt_true) {
        visitStmt.getComment().ifPresent(comment -> comment.remove());
        List<String> codeList = new ArrayList<String>();
        codeList.add(Stmt_true);
        Collections.addAll(codeList, visitStmt.toString().split("\n"));
        codeList.add("}\n");
        // Put the mutated code block back.
        System.out.print("------------------------------------------\n");
        Statement True_stmt = StaticJavaParser.parseStatement(String.join("\n", codeList));
        // methodBody.setStatement(1, StaticJavaParser.parseStatement(String.join("\n",
        // codeList)));
        return True_stmt;
    }

    public List<CompilationUnit> getCu(String path) throws IOException {
        Path pathToSource = Paths.get(path);
        SourceRoot sourceRoot = new SourceRoot(pathToSource);
        sourceRoot.tryToParse();
        List<CompilationUnit> compilations = FindJava(sourceRoot);
        return compilations;
    }

    public List Decommon(List list) {
        HashSet set_list_for_var = new HashSet(list);
        list.clear();
        list.addAll(set_list_for_var);
        return list;
    }

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

    public void addImport_forcu(CompilationUnit cu, List<ImportDeclaration> list_ImportDeclarations) {
        for (ImportDeclaration Imp : list_ImportDeclarations) {
            cu.addImport(Imp);
        }
    }

    public void setVarName(Statement visitStmt, String oldName, String newName) {
        visitStmt.findAll(NameExpr.class).forEach(n -> {
            if (n.getNameAsString().equals(oldName)) {
                n.setName(newName);
            }
        });
        // System.out.println("check set name here:\n" + visitStmt.toString());
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

    public CompilationUnit Insert_new(CompilationUnit cu,List<String> bin_true_par,List<String> bin_false_par,List<String> bin_true_expr,List<String> bin_false_expr) throws IOException, SQLException, ClassNotFoundException {
        // System.out.println("get for1");
        List<String> for_stmt = s.select();
        List<HashMap<String, String>> needsetVarName_list = s.getNeedfHashMaps();

        // String name = cu.getStorage().get().getPath().toString();
        // String name_1 = cu.getStorage().get().getFileName();
        // System.out.println(name);
        List<ClassOrInterfaceDeclaration> c1 = cu.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration c : c1) {

            List<MethodDeclaration> methods = c.getMethods();
            int size = methods.size() - 1;
            for (int i = 0; i < size; i++) {
                MethodDeclaration m = methods.get(i);
                if (m.getBody().isPresent()) {
                    BlockStmt methodBody = m.getBody().get();
                    if (methodBody == null)
                        continue;
                    Random random = new Random();
                    Statement test = null;
                    // Generate a random number, num: for inserted fragment; ture_index_1: for index of true expression.
                    int ture_size = bin_true_expr.size();
                    int falsez_size = bin_false_expr.size();
                    int smaller = Math.min(for_stmt.size(), needsetVarName_list.size());
                    int num = 0;
                    HashMap<String, String> needsetVarName = new HashMap<>();
                    HashMap<String, String> needsetVarName_all = new HashMap<>();
                    if(smaller > 0){
                        num = random.nextInt(smaller);
                        test = StaticJavaParser.parseStatement(for_stmt.get(num));
                        needsetVarName = needsetVarName_list.get(num);
                        // System.out.println(needsetVarName);
                    }

                    List<Statement> statementList = m.getBody().get().getStatements();
                    needsetVarName_all.putAll(needsetVarName);
                    List<String> needsetVarName_expr = new ArrayList<>();
                    // System.out.println("---------------------------Start Insert-----------------------------");
                    for (Statement stmt : statementList) {
                        List<Node> nodes = stmt.findAll(Node.class);
                        // nodes.get(0).setLineComment("THe seed form" + name_1);
                        break;
                    }

                    // System.out.println("needsetVarName_all:\t" + needsetVarName_all);
                    List<Statement> stmt_par_gen = new ArrayList<>();

                    stmt_par_gen.addAll(setVarNameInInsertStmt_new(cu, m,test, needsetVarName_all,bin_true_par,bin_false_par,bin_true_expr,bin_false_expr));

                    int stmt_gen_size = stmt_par_gen.size();
                    for (int index_a = 0; index_a < stmt_gen_size; index_a++) {
                        Statement stmt = stmt_par_gen.get(index_a);
                        addStmtBetweenStmt(methodBody, index_a, stmt);
                    }
                    Statement stmt = stmt_par_gen.get(0);
                    List<Node> nodess = stmt.findAll(Node.class);
                    // nodess.get(0).setLineComment(name);
                    nodess.get(1).setLineComment("MutateByTrueAndInsertion");
                    if (test.toString().contains("TestFailure")) {
                        // System.out.println("---------------------------Insert Import-----------------------------");
                        ImportDeclaration importDeclaration = StaticJavaParser
                                .parseImport("import nsk.share.TestFailure;");
                        cu.addImport(importDeclaration);
                    }

                    // System.out.println("---------------------------End Insert-----------------------------");
                }
            }
            // System.out.println("I HERE");
        }
        return cu;
    }
    public List<Statement> setVarNameInInsertStmt_new(CompilationUnit cu, MethodDeclaration visitMethod, Statement insertStmt, HashMap<String,String> needfulParam,List<String> bin_true_par,List<String> bin_false_par,List<String> bin_true_expr,List<String> bin_false_expr)throws NullPointerException{
        List<Statement> newVarsDec = new ArrayList<Statement>();
        Map<String,String> availableVar = classifier.getAvailableVar(cu.findAll(FieldDeclaration.class),visitMethod,insertStmt.getBegin().get().line);

        List<String> list_avar_key = new ArrayList<String>(availableVar.keySet());
        List<String> list_avar_vale = new ArrayList<String>(availableVar.values());


        List<String> list_need_key = new ArrayList<String>(needfulParam.keySet());
        List<String> list_need_vale = new ArrayList<String>(needfulParam.values());
        HashMap<String,String> common = new HashMap<String,String>();
        HashMap<String,String> uncommon = new HashMap<String,String>();
        NodeList<Parameter> Par = visitMethod.getParameters();
        for(Parameter par_m :Par){
            list_avar_key.add(par_m.getName().asString());
            list_avar_vale.add(par_m.getType().asString());
        }

        // Compare between the parameters to be generated and the original parameters.
        // System.out.println("----------------Start setVarNameInInsertStmt---------------------");
        if(availableVar.size()!=0){
            for(int index =0;index<list_avar_vale.size();index++){
                for(int index_need =0;index_need<list_need_key.size();index_need++){
                    // If has the same type.
                    if(list_avar_vale.get(index).equals(list_need_vale.get(index_need))){
                        // If there is one with the same name and type.
                        if(list_avar_key.get(index).equals(list_need_key.get(index_need))){
                            common.put(list_need_key.get(index_need),list_need_key.get(index_need)+"change");
                        }
                        // The name is different with the same type, and the parameter is not in the common list.
                        else if(!common.containsValue(list_avar_key.get(index))){
                            common.put(list_need_key.get(index_need),list_avar_key.get(index));
                        }
                    }
                    else{
                        uncommon.put(list_need_key.get(index_need),list_need_vale.get(index_need));
                    }
                }
            }
        }
        else{
            // System.out.println("uncommon"+needfulParam);
            uncommon = needfulParam;
        }

        // This part is about variable generation and variable renaming.
        if(uncommon.size()!=0){
            for(Map.Entry<String,String> entry:uncommon.entrySet()){
                newVarsDec.addAll(callGenerateDec(cu,entry.getKey(),entry.getValue()));
            }
        }
        // System.out.println("setVarName"+insertStmt);
        for(Map.Entry<String,String> entry_com:common.entrySet()){
            setVarName(insertStmt,entry_com.getKey(),entry_com.getValue());
        }


        int ture_size = bin_true_expr.size();
        int falsez_size = bin_false_expr.size();
        int ture_index_1 = random.nextInt(ture_size);
        int ture_index_2 = random.nextInt(ture_size-1);
        int false_index_1 = random.nextInt(falsez_size);
        int false_index_2 = random.nextInt(falsez_size-1);
        String boolean_expr1 = "";
        String boolean_expr2 = "";
        if(newVarsDec.size()>=2){
            // if((!newVarsDec.get(0).toString().contains("myjvm"))&&(!newVarsDec.get(1).toString().contains("myjvm"))){
                boolean_expr1 = newVarsDec.get(0).toString().substring(0, newVarsDec.get(0).toString().length()-1);
                boolean_expr2 = newVarsDec.get(1).toString().substring(0, newVarsDec.get(1).toString().length()-1);
            // }
        }
        Statement if_stmt = Generate_If(insertStmt,boolean_expr1,boolean_expr2,ture_index_1,ture_index_2,false_index_1,false_index_1,bin_true_expr,bin_false_expr);
        newVarsDec.add(StaticJavaParser.parseStatement(bin_false_par.get(false_index_1)));
        newVarsDec.add(StaticJavaParser.parseStatement(bin_false_par.get(false_index_2)));
        newVarsDec.add(StaticJavaParser.parseStatement(bin_true_par.get(ture_index_1)));
        newVarsDec.add(StaticJavaParser.parseStatement(bin_true_par.get(ture_index_2)));
        newVarsDec.add(if_stmt);
        return newVarsDec;
    }

    public List<Statement> callGenerateDec(CompilationUnit cu, String name, String type) throws NullPointerException {
        // The variable that holds the result.
        List<Statement> varStmt = new ArrayList<Statement>();
        // Call generator
        // System.out.println("---------------start callGenerateDec---------");
        List<VariableComponent> varComp = generator.generateDec(name, type);
        // System.out.println("Ths is varComp:\n"+varComp);
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

    public Statement Generate_If(Statement test,String boolean_expr1,String boolean_expr2,int ture_index_1,int ture_index_2,int false_index_1,int false_index_2,List<String> bin_true_expr,List<String> bin_false_expr){

        String boolean_expr_false = "";
        String boolean_expr_true ="";
        String binnaryString = "";
        if(boolean_expr1.length() != 0&&boolean_expr2.length() != 0){
            boolean_expr_false=GetBooleanFromStmt(boolean_expr1,"false");
            boolean_expr_true=GetBooleanFromStmt(boolean_expr2,"true");
            binnaryString = boolean_expr_false+"||"+bin_false_expr.get(false_index_1) + "||" + bin_true_expr.get(ture_index_1) + "&&"
                + bin_true_expr.get(ture_index_2)+"&&"+boolean_expr_true;
        }else{
            binnaryString = bin_false_expr.get(false_index_1)+"||"+bin_false_expr.get(false_index_2) + "||" + bin_true_expr.get(ture_index_1) + "&&"
                + bin_true_expr.get(ture_index_2);
        }
        
        String stmt_ture_tring1 = "if" + "(" + binnaryString + ")" + "{\n";
        Statement insertStatement = addStmtInStmt(test, stmt_ture_tring1);
        return insertStatement;
    }
    public String GetBooleanFromStmt(String stmt,String booleans){
        Statement statement = StaticJavaParser.parseStatement(stmt+";");
        List<Expression> stmt_expr = statement.findAll(Expression.class);
        VariableDeclarationExpr variableDeclarator = (VariableDeclarationExpr) stmt_expr.get(0);
        variableDeclarator.getVariables().get(0).getName();

        String Name = variableDeclarator.getVariables().get(0).getName().toString();
        String Type = variableDeclarator.getVariables().get(0).getType().toString();
        String Value = variableDeclarator.getVariables().get(0).getInitializer().get().toString();

        String BinnaryString="";
        if (Type.contains("[]")&&!Type.contains("[][]")) {
            BinnaryString = GetVaule(stmt, Name,Type, booleans);
        } else if (Type.contains("[][]")){
            BinnaryString = GetVaule_2d(stmt, Name,Type, booleans);
        }
        else {
            BinnaryString = Generate_Bin_Simple(Name, Value, Type, booleans);
        }
        return BinnaryString;
    }

    /**
     * @version 1.0
     * @apiNote GetVaule
     * @param
     * stmt-Call        the variable declaration statement generated by the
     *                  generation code
     *                  name:var name boolen:true or false
     * @return boolean_expression
     */
    public String GetVaule(String stmt, String name, String Type, String boolens) {
        ArrayInitializerExpr he = (ArrayInitializerExpr) StaticJavaParser.parseVariableDeclarationExpr(stmt)
                .getVariable(0).getInitializer().get();
        int size = he.getValues().size();
        Random random = new Random();
        int num = random.nextInt(size);
        String result = he.getValues().get(num).toString();
        String BinnaryString = "";
        switch (boolens) {
            case "true":
                    BinnaryString = name + ".length" + " <= " + size + "&&" + name + "[" + num + "]" + "==" + result;
                    break;

            case "false":
                    BinnaryString = name + ".length" + " >" + size + "&&" + name + "[" + num + "]" + "==" + result;
                    break;
        }
        // System.out.println(BinnaryString);
        return BinnaryString;
    }

    public String GetVaule_2d(String stmt, String name, String Type, String boolens) {
        ArrayInitializerExpr he = (ArrayInitializerExpr) StaticJavaParser.parseVariableDeclarationExpr(stmt)
                .getVariable(0).getInitializer().get();
        ArrayInitializerExpr re = (ArrayInitializerExpr) he.getValues().get(0);
        int size = 10;
        Random random = new Random();
        int num = random.nextInt(size);
        String result = re.getValues().get(num).toString();
        String BinnaryString = "";
        switch (boolens) {
            case "true":
                    BinnaryString = name + ".length" + " <= " + size + "&&" + name + "[0]"+"[" + num + "]" + "==" + result;
                    break;
            case "false":
                    BinnaryString = name + ".length" + " >" + size + "&&" + name + "[0]"+ "[" + num + "]" + "==" + result;
                    break;
        }
        // System.out.println(BinnaryString);
        return BinnaryString;
    }

    public String Generate_Bin_Simple(String name, String value, String Type, String result) {
        String boolean_expression = "";
        int int_value;
        double double_value;
        float float_value;
        long long_value;
        switch (result) {
            case "true":
                if (Type.equals("int")) {
                    int_value = Integer.valueOf(value).intValue() - 1;
                    boolean_expression = name + " > " + int_value;
                    break;
                } else if (Type.equals("double")) {
                    if (value.contains("NaN")) {
                        boolean_expression = name + " != " + value;
                        break;
                    } else if (value.contains("Double.")) {
                        boolean_expression = name + " == " + value;
                        break;
                    } else {
                        double_value = Double.valueOf(value).doubleValue() - 2;
                        boolean_expression = name + " > " + double_value;
                        break;
                    }

                } else if (Type.equals("float")) {
                    if (value.contains("NaN")) {
                        boolean_expression = name + " != " + value;
                        break;
                    } else if (value.contains("Float.")) {
                        boolean_expression = name + " == " + value;
                        break;
                    } else {
                        float_value = Float.valueOf(value).floatValue() - 3;
                        boolean_expression = name + " > " + float_value;
                        break;
                    }
                } else if (Type.equals("long")) {
                    String longa = value;
                    if (value.contains("L")) {
                        longa = value.substring(0, value.length() - 1);
                    }
                    long_value = Long.valueOf(longa).longValue() - 4;
                    boolean_expression = name + " > " + long_value + "L";
                    break;
                } else {
                    boolean_expression = name + " == " + value;
                    break;
                }
            case "false":
                if (Type.equals("int")) {
                    int_value = Integer.valueOf(value).intValue() + 1;
                    boolean_expression = name + " > " + int_value;
                    break;
                } else if (Type.equals("double")) {
                    if (value.contains("NaN")) {
                        boolean_expression = name + " == " + value;
                        break;
                    } else if (value.contains("Double.")) {
                        boolean_expression = name + " != " + value;
                        break;
                    } else {
                        double_value = Double.valueOf(value).doubleValue() + 2;
                        boolean_expression = name + " >" + double_value;
                        break;
                    }

                } else if (Type.equals("float")) {
                    if (value.contains("NaN")) {
                        boolean_expression = name + " == " + value;
                        break;
                    } else if (value.contains("Float.")) {
                        boolean_expression = name + " != " + value;
                        break;
                    } else {
                        float_value = Float.valueOf(value).floatValue() + 3;
                        boolean_expression = name + " > " + float_value;
                        break;
                    }
                } else if (Type.equals("long")) {
                    String longa = value;
                    if (value.contains("L")) {
                        longa = value.substring(0, value.length() - 1);
                    }
                    long_value = Long.valueOf(longa).longValue() + 4;
                    boolean_expression = name + " > " + long_value + "L";
                    break;
                } else {
                    boolean_expression = name + " != " + value;
                    break;
                }

        }
        return boolean_expression;

    }

}
