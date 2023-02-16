package com.jvmfuzzing.mutation_by_all;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.jvmfuzzing.generator.VariableGenerater;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.jvmfuzzing.generator.VariableComponent;

public class GenerateBinnary {
    Random random = new Random();
    // static SaveToDB saveToDB = new SaveToDB();
    static VariableGenerater generator = new VariableGenerater();
    static ClassifyVariable classifier = new ClassifyVariable();
    static final List<List<String>> boolean_par = new ArrayList<>();
    int i = 0;

    // Randomly generated string
    public String Generate_name(int length) {
        String name = RandomStringUtils.randomAlphanumeric(length);
        String name1 = "";
        if (!startWithNumber(name)) {
            name1 = name;
        } else {
            name1 = "myjvm" + i;
            i++;
        }
        return name1;
    }

    // Judging whether the generated string starts with a number.
    public boolean startWithNumber(String str) {
        return Pattern.matches("[0-9].*", str);
    }

    // A Boolean expression of non-array type is created.
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
                    boolean_expression = name + " > " + int_value + ";";
                    break;
                } else if (Type.equals("double")) {
                    if (value.contains("NaN")) {
                        boolean_expression = name + " != " + value + ";";
                        break;
                    } else if (value.contains("Double.")) {
                        boolean_expression = name + " == " + value + ";";
                        break;
                    } else {
                        double_value = Double.valueOf(value).doubleValue() - 2;
                        boolean_expression = name + " > " + double_value + ";";
                        break;
                    }

                } else if (Type.equals("float")) {
                    if (value.contains("NaN")) {
                        boolean_expression = name + " != " + value + ";";
                        break;
                    } else if (value.contains("Float.")) {
                        boolean_expression = name + " == " + value + ";";
                        break;
                    } else {
                        float_value = Float.valueOf(value).floatValue() - 3;
                        boolean_expression = name + " > " + float_value + ";";
                        break;
                    }
                } else if (Type.equals("long")) {
                    String longa = value;
                    if (value.contains("L")) {
                        longa = value.substring(0, value.length() - 1);
                    }
                    long_value = Long.valueOf(longa).longValue() - 4;
                    boolean_expression = name + " > " + long_value + "L" + ";";
                    break;
                } else {
                    boolean_expression = name + " == " + value + ";";
                    break;
                }
            case "false":
                if (Type.equals("int")) {
                    int_value = Integer.valueOf(value).intValue() + 1;
                    boolean_expression = name + " > " + int_value + ";";
                    break;
                } else if (Type.equals("double")) {
                    if (value.contains("NaN")) {
                        boolean_expression = name + " == " + value + ";";
                        break;
                    } else if (value.contains("Double.")) {
                        boolean_expression = name + " != " + value + ";";
                        break;
                    } else {
                        double_value = Double.valueOf(value).doubleValue() + 2;
                        boolean_expression = name + " >" + double_value + ";";
                        break;
                    }

                } else if (Type.equals("float")) {
                    if (value.contains("NaN")) {
                        boolean_expression = name + " == " + value + ";";
                        break;
                    } else if (value.contains("Float.")) {
                        boolean_expression = name + " != " + value + ";";
                        break;
                    } else {
                        float_value = Float.valueOf(value).floatValue() + 3;
                        boolean_expression = name + " > " + float_value + ";";
                        break;
                    }
                } else if (Type.equals("long")) {
                    String longa = value;
                    if (value.contains("L")) {
                        longa = value.substring(0, value.length() - 1);
                    }
                    long_value = Long.valueOf(longa).longValue() + 4;
                    boolean_expression = name + " > " + long_value + "L" + ";";
                    break;
                } else {
                    boolean_expression = name + " != " + value + ";";
                    break;
                }

        }
        return boolean_expression;

    }

    /**
     * @version 1.0
     * @apiNote boolean_expression
     * @param name Type:var type result:true or false
     * @return boolean_expression
     */

    public String Generate_Bin(String name, String Type, String result) {
        List<VariableComponent> varComp = generator.generateDec(name, Type);
        List<String> varComp_toStrings = new ArrayList<>();
        String bin = "";
        for (VariableComponent vc : varComp) {
            String stmt = vc.getVarType() + " " + vc.getVarName() + " = " + vc.getVarValue().toString();
            varComp_toStrings.add(stmt + ";");
            if (vc.getVarType().contains("[]")) {
                bin = GetVaule(stmt, vc.getVarName(), result);
            } else {
                bin = Generate_Bin_Simple(vc.getVarName(), vc.getVarValue().toString(), vc.getVarType(), result);
            }
            // System.out.println(bin);
        }
        boolean_par.add(varComp_toStrings);
        return bin;
    }

    // Get the value of the generated array type.
    /**
     * @version 1.0
     * @apiNote GetVaule
     * @param
     * stmt-Call        the variable declaration statement generated by the
     *                  generation code
     *                  name:var name boolen:true or false
     * @return boolean_expression
     */
    public String GetVaule(String stmt, String name, String boolens) {
        ArrayInitializerExpr he = (ArrayInitializerExpr) StaticJavaParser.parseVariableDeclarationExpr(stmt)
                .getVariable(0).getInitializer().get();
        int size = he.getValues().size();
        Random random = new Random();
        int num = random.nextInt(size);
        String result = he.getValues().get(num).toString();
        String BinnaryString = "";
        switch (boolens) {
            case "true":
                BinnaryString = name + ".length" + " <= " + size + "&&" + name + "[" + num + "]" + ".equals(\"" + result
                        + "\"" + ")";
                break;
            case "false":
                BinnaryString = name + "[" + num + "]" + ".equals(\"" + result + "\"" + ")" + "&&" + name + ".length"
                        + " > " + size;
                break;
        }
        // System.out.println(BinnaryString);
        return BinnaryString;
    }

    // public void SavaBinTodb() throws SQLSyntaxErrorException, ClassNotFoundException, SQLException {
    //     List<String> booList = new ArrayList<>();
    //     for (int i = 0; i < 500; i++) {
    //         String name = Generate_name(3);
    //         // List<String> boolenList = Arrays.asList("true","false");
    //         Random random = new Random();
    //         // int num = random.nextInt(2);
    //         List<String> typeList = Arrays.asList("float", "long", "int", "char", "byte", "double", "float[]", "long[]",
    //                 "int[]", "char[]", "byte[]", "double[]");
    //         int type_index = random.nextInt(12);
    //         // String boolen = boolenList.get(num);
    //         String boolean_exp = Generate_Bin(name, typeList.get(type_index), "false");
    //         booList.add(boolean_exp);
    //     }
    //     SaveToDB.SaveBinary(booList, boolean_par);
    // }
}