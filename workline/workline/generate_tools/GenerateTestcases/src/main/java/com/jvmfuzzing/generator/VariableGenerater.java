package com.jvmfuzzing.generator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

public class VariableGenerater 
{
    static CodeVisitor codeVisitor = new CodeVisitor();
    static PrimitiveGenerator primitiveGenerator = new PrimitiveGenerator();
    static GroupGenerator groupGenerator = new GroupGenerator();
    static UnprimitiveGenerator unprimitiveGenerator = new UnprimitiveGenerator();
    static List<String> implementationClassList = new ArrayList<String>();
    static Logger logger = LoggerFactory.getLogger(VariableGenerater.class);
        
    public int strCount(String string,String subString){
        int count = 0 ;
        while (string.indexOf(subString) != -1){
            count ++ ;
            int index = string.indexOf(subString);
            string = string.substring(index+1);
        }
        return count;
    }

    public String generateValueOfDec(String type){
        // System.out.println("generate value of type:"+type);
        // 1.Array 2.boolean 3.Integral 4.FloatingPoint 5.String 6.Set/List/Map 7.API_Info
        String value="",elemType="";
        if(primitiveGenerator.isArray(type)){
            // Gets the dimension of the array.
            int count = strCount(type,"[]");
            // Gets the type.
            elemType = type.substring(0,type.length()-2*count);
            // System.out.println("type:"+elemType);
            // Start to generate.
            value = primitiveGenerator.generateArray(count,elemType);            
        }
        else if(primitiveGenerator.isBoolean(type)){
            value = primitiveGenerator.generateBoolean();
        }
        else if(primitiveGenerator.isIntegral(type)){
            value = primitiveGenerator.generateIntegral(type);
        }
        else if(primitiveGenerator.isFloatingPoint(type)){
            value = primitiveGenerator.generateFloatingPoint(type);
        }
        else if(primitiveGenerator.isString(type)){
            value = primitiveGenerator.generateString();
        }
        else if(groupGenerator.isGroupType(type)){
            GroupGenerator.GroupInfo groupInfo = groupGenerator.generateGroupInit(type);
            value = groupInfo.getValue();
            String implementationClass = groupInfo.getImplementationClass();
            // System.out.println("implementationClass:"+implementationClass);
            if(!implementationClassList.contains(implementationClass)){
                implementationClassList.add(groupInfo.getImplementationClass());
            }
        }
        else{
            value = "null";
        }
        // System.out.println("value:"+value);
        return value;
    }

    public List<VariableComponent> generateDec(String name,String type){
        // System.out.println("start to generate dec:"+name+"-"+type);
        // Prepare the needful variable.
        List<VariableComponent> decList = new ArrayList<VariableComponent>();
        // 1.Set/List/Map 2.Array/Integral/FloatingPoint/String/boolean 3.API_Info
        if(groupGenerator.isGroupType(type)){
            decList.addAll(groupGenerator.generateGroup(type,name,"param"));
        }
        else if(primitiveGenerator.isArray(type)||primitiveGenerator.isIntegral(type)||primitiveGenerator.isFloatingPoint(type)||primitiveGenerator.isString(type)||primitiveGenerator.isBoolean(type)){
            String value = generateValueOfDec(type);
            // decList.add("\t"+type+" "+name+" = "+value+";\n");
            decList.add(new VariableComponent(type,name,value));
        }
        else{
            decList.addAll(unprimitiveGenerator.generateUnprimitive(name,type));
        }
        // Process special type T.
        // boolean isMatch;
        // if(elemType.length() == 0)
        //     isMatch = Pattern.matches("[A-Z]", type);
        // else
        //     isMatch = Pattern.matches("[A-Z]", elemType);
        // if(isMatch){
        //     dec = "\tObject"+type.substring(1,type.length())+" "+name+" = "+value+";\n";
        // }else{
        //     dec = "\t"+type+" "+name+" = "+value+";\n";
        // }       
        // System.out.println("generate declaration:\n"+dec);
        return decList;
    }
}