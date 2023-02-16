package com.jvmfuzzing.mutation_by_all;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.utils.SourceRoot;
import static java.util.Arrays.asList;

import java.util.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.util.List;

import java.sql.*;

public class mutationbyOperator {

    static CreateJavaFile create = new CreateJavaFile();

    // public static void getOperator(List<CompilationUnit> Seed_compilations,String
    // NewPath,StringBuffer stringBuffer) throws IOException,NullPointerException {
    // public static void getOperator(List<CompilationUnit> Seed_compilations)
    // throws IOException {
    public static CompilationUnit getOperator(CompilationUnit cu) throws IOException, NullPointerException {
        // String name = cu.getStorage().get().getPath().toString();
        // System.out.println(name);

        List<UnaryExpr.Operator> unarylist = asList(com.github.javaparser.ast.expr.UnaryExpr.Operator.MINUS,
                com.github.javaparser.ast.expr.UnaryExpr.Operator.PLUS,
                com.github.javaparser.ast.expr.UnaryExpr.Operator.POSTFIX_DECREMENT,
                com.github.javaparser.ast.expr.UnaryExpr.Operator.POSTFIX_INCREMENT,
                com.github.javaparser.ast.expr.UnaryExpr.Operator.PREFIX_DECREMENT,
                com.github.javaparser.ast.expr.UnaryExpr.Operator.PREFIX_INCREMENT);
        List<BinaryExpr.Operator> binaryList = asList(com.github.javaparser.ast.expr.BinaryExpr.Operator.DIVIDE,
                com.github.javaparser.ast.expr.BinaryExpr.Operator.PLUS,
                com.github.javaparser.ast.expr.BinaryExpr.Operator.MINUS,
                com.github.javaparser.ast.expr.BinaryExpr.Operator.MULTIPLY);
        List<AssignExpr.Operator> assignlist = asList(com.github.javaparser.ast.expr.AssignExpr.Operator.DIVIDE,
                com.github.javaparser.ast.expr.AssignExpr.Operator.MINUS,
                com.github.javaparser.ast.expr.AssignExpr.Operator.MULTIPLY,
                com.github.javaparser.ast.expr.AssignExpr.Operator.PLUS,
                com.github.javaparser.ast.expr.AssignExpr.Operator.REMAINDER,
                com.github.javaparser.ast.expr.AssignExpr.Operator.XOR);
        List<ClassOrInterfaceDeclaration> c1 = cu.findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration c : c1) {

            List<MethodDeclaration> methods = c.getMethods();
            int size = methods.size() - 1;

            for (int i = 0; i < size; i++) {

                MethodDeclaration m = methods.get(i);
                Random random = new Random();
                int num_ue = random.nextInt(unarylist.size());
                int num_ae = random.nextInt(assignlist.size());
                int num_be = random.nextInt(binaryList.size());

                m.walk(node -> {
                    if (node instanceof UnaryExpr) {
                        UnaryExpr ue = (UnaryExpr) node;
                        ue.setOperator(unarylist.get(num_ue));
                    } else if (node instanceof AssignExpr) {
                        AssignExpr ae = (AssignExpr) node;
                        ae.setOperator(assignlist.get(num_ae));
                    } else if (node instanceof BinaryExpr) {
                        BinaryExpr be = (BinaryExpr) node;
                        if (be.getOperator().asString().equals("LESS")) {
                            List<BinaryExpr.Operator> OP = asList(
                                    com.github.javaparser.ast.expr.BinaryExpr.Operator.LESS_EQUALS,
                                    com.github.javaparser.ast.expr.BinaryExpr.Operator.GREATER,
                                    com.github.javaparser.ast.expr.BinaryExpr.Operator.GREATER_EQUALS);
                            be.setOperator((BinaryExpr.Operator) OP.get(random.nextInt(3)));
                        } else if (be.getOperator().asString().equals("GREATER")) {
                            List<BinaryExpr.Operator> OP = asList(
                                    com.github.javaparser.ast.expr.BinaryExpr.Operator.LESS_EQUALS,
                                    com.github.javaparser.ast.expr.BinaryExpr.Operator.LESS,
                                    com.github.javaparser.ast.expr.BinaryExpr.Operator.GREATER_EQUALS);
                            be.setOperator((BinaryExpr.Operator) OP.get(random.nextInt(3)));
                        } else if (be.getOperator().asString().equals("BINARY_AND")) {
                            be.setOperator(com.github.javaparser.ast.expr.BinaryExpr.Operator.BINARY_OR);
                        } else if (be.getOperator().asString().equals("BINARY_OR")) {
                            be.setOperator(com.github.javaparser.ast.expr.BinaryExpr.Operator.BINARY_AND);
                        } else if (be.getOperator().asString().equals("OR")) {
                            be.setOperator(com.github.javaparser.ast.expr.BinaryExpr.Operator.AND);
                        } else if (be.getOperator().asString().equals("AND")) {
                            be.setOperator(com.github.javaparser.ast.expr.BinaryExpr.Operator.OR);
                        } else {
                            be.setOperator((BinaryExpr.Operator) binaryList.get(num_be));
                        }
                    }
                });

                BlockStmt methodBody = m.getBody().get();
                List<Statement> stmt_list = methodBody.getStatements();
                if(stmt_list.size()>0){
                    List<Node> nodes = stmt_list.get(0).findAll(Node.class);
                    nodes.get(0).setLineComment("MutationbyReplaceOpr");
                }
            }
        }
        return cu;
    }

    public static List<CompilationUnit> getCu(String path) throws IOException {
        Path pathToSource = Paths.get(path);
        SourceRoot sourceRoot = new SourceRoot(pathToSource);
        sourceRoot.tryToParse();
        List<CompilationUnit> compilations = FindJava(sourceRoot);
        return compilations;
    }

    public static List<CompilationUnit> FindJava(SourceRoot sourceRoot) throws IOException {
        return sourceRoot.getCompilationUnits();
    }
}
