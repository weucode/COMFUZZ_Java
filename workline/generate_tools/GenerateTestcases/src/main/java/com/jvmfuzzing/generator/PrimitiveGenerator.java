package com.jvmfuzzing.generator;

import java.util.Random;
import java.util.List;
import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;

public class PrimitiveGenerator{
    Random random = new Random();

    String[] basicType = {"int","double","float","long","boolean","byte","char","short"};

    public  String generateInt(){
        int intNumber = random.nextInt();
        return String.valueOf(intNumber);
    }

    public  String generateInt(int max){
        int intNumber = random.nextInt(max);
        return String.valueOf(intNumber);
    }

    public String generateShort(){
        int shortNumber=(random.nextInt(65536)-32768);
        return String.valueOf(shortNumber);
    }

    public  String generateFloat(){
        float floatNumber =random.nextFloat();
        return String.valueOf(floatNumber)+"f";
    }

    public  String generateLong(){
        long longNumber=random.nextLong();
        return String.valueOf(longNumber)+"L";
    }
        
    public  String generateDouble(){
        double doubleNumber =random.nextDouble();
        return String.valueOf(doubleNumber);
    }

    public  String generateBoolean(){
        boolean booleanNumber =random.nextBoolean();
        return String.valueOf(booleanNumber);
    }

    public  String generateByte(){
        byte[] bytes = {1};
        random.nextBytes(bytes);
        return String.valueOf(bytes[0]);
    }

    public  String generateChar(){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-={}|:\"<>?[]\\;',./";
        int intNumber = random.nextInt(92);
        String charStr = str.substring(intNumber,intNumber+1);
        // System.out.println("generate char:"+char);
        if(charStr.equals("\\")){
            return "'\\\\'";
        }
        else if(charStr.equals("'")){
            return "'\\\''";
        }    
        else{
            return "'"+charStr+"'";
        }
    }

    public  String generateBasic(String type){
        // System.out.println("type in generatebasic:"+type);
        String value = "null";
        if(isIntegral(type)){
            value = generateIntegral(type);
        }
        else if(isFloatingPoint(type)){
            value = generateFloatingPoint(type);
        }
        else if(isBoolean(type)){
            value = generateBoolean();
        }
        else if(isString(type)){
            value = generateString();
        }
        return value;
    }

    public int generateLength(){
        int len = random.nextInt();
		if(len>0 && len<100){
            // System.out.println("generate array length:"+len);
            return len;
        }
		else if(len<0&&len>-100){
            // System.out.println("generate array length:"+len);
            return -len;
        }
		else{
            // System.out.println("generate array length:"+10);
            return 10;
        }
    }

    public String generateBytes(){
        byte[] nbyte = new byte[generateLength()];
		random.nextBytes(nbyte);
        String value = "{";
        for(int i = 0;i<nbyte.length;i++){
			value += String.valueOf(nbyte[i]);
			if(i<nbyte.length-1)
				value += ",";
		}
        return value+"}";
    }

    public String linearArray(int len,String type){
        // If type is byteArray,use API.
        if(type.equals("byte")){
            return generateBytes();
        }
        // If other type,use String splicing.
        String arrStr = "{";
        for(int i=0;i<len;i++){
            // if(type.equals("String")){
            //     arrStr += "\""+generateString()+"\"";
            // }
            // else{
                arrStr += generateBasic(type);
            // }
            if(i < len-1)
                arrStr += ",";
        }            
        arrStr += "}";
        // System.out.println("Array:"+arrStr);
        return arrStr;
    }

    public String generateArray(int count,String type){
        String rsArray;
        int len = generateLength();

        if(type.equals("T")){
            int n = random.nextInt(7);
            type = basicType[n];
            // System.out.println("select a basic type:"+type);
        }        

        if(count == 1){
            rsArray = linearArray(len, type);
        }
        else{
            rsArray = "{";
            for(int i=0;i<count;i++){
                rsArray += linearArray(len, type);
                if(i < count-1)
                    rsArray += ",";
            }

            rsArray += "}";
        }
        return rsArray;          
    }
    
    public String generateString(){
        int len = generateLength();
        String randomStr = RandomStringUtils.randomAscii(len);
        String resultStr = "";
        for(int i=0;i<len;i++){
            char ch = randomStr.charAt(i);
            if(ch == '"' || ch == '\\'){
                resultStr += "\\";
            }    
            resultStr += String.valueOf(ch);
        }
        // System.out.println("generate string here:"+resultStr+" size:"+resultStr.length());
        return "\""+resultStr+"\"";
    }

    public String generateStrongString(){
        String[] specialCharacter = {"\\\b","\\\f","\\\\","\\\'","\\\\*"};
        String value = "";
        int len = generateLength();
        for(int i=0;i<len;i++){
            int index = random.nextInt(5);
            value += specialCharacter[index];
        }
        return value;
    }

    public String generateRandom(String type){
        switch(type){
            case "int":
            case "Integer":
            case "Object":
                return generateInt();
            case "short":
            case "Short":
                return generateShort();
            case "float":
            case "Float":
                return generateFloat();
            case "double":
            case "Double":
                return generateDouble();
            case "long":
            case "Long":
                return generateLong();           
            case "boolean":
            case "Boolean":
                return generateBoolean();
            case "byte":
            case "Byte":
                return generateByte();
            case "char":
            case "Character":
                return generateChar();
            default:
                return "";
        }
    }

    public String generateMAX(String type){
        switch(type){
            case "int":
            case "Integer":
            case "Object":
                return String.valueOf(generateInt(10));
            case "short":
            case "Short":
                return Short.toString(Short.MAX_VALUE);
            case "long":
            case "Long":
                return Long.toString(Long.MAX_VALUE)+"L";
            case "byte":
            case "Byte":
                return Byte.toString(Byte.MAX_VALUE);
            case "char":
            case "Character":
                return "Character.MAX_VALUE";
            case "float":
            case "Float":
                return "Float.MAX_VALUE";
            case "double":
            case "Double":
                return "Double.MAX_VALUE";
            default:
                return "";
        }
    }

    public String generateMIN(String type){
        switch(type){
            case "int":
            case "Integer":
            case "Object":
                return String.valueOf(generateInt(10));
            case "short":
            case "Short":
                return Short.toString(Short.MIN_VALUE);
            case "long":
            case "Long":
                return Long.toString(Long.MIN_VALUE)+"L";
            case "byte":
            case "Byte":
                return Byte.toString(Byte.MIN_VALUE);
            case "float":
            case "Float":
                return "Float.MIN_VALUE";
            case "double":
            case "Double":
                return "Double.MIN_VALUE";
            case "char":
            case "Character":
                return "Character.MIN_VALUE";
            default:
                return "";
        }
    }

    public String generatePositiveInfinity(String type){
        switch(type){
            case "float":
            case "Float":
                return "Float.POSITIVE_INFINITY";
            case "double":
            case "Double":
                return "Double.POSITIVE_INFINITY";
            default:
                return "";
        }
    }

    public String generateNegativeInfinity(String type){
        switch(type){
            case "float":
            case "Float":
                return "Float.NEGATIVE_INFINITY";
            case "double":
            case "Double":
                return "Double.NEGATIVE_INFINITY";
            default:   
                return "";
        }
    }

    public String generateNaN(String type){
        switch(type){
            case "float":
            case "Float":
                return "Float.NaN";
            case "double":
            case "Double":
                return "Double.NaN";
            default:
                return "";
        }
   }

    public String generateZero(String type){
        if(type.equals("int")||type.equals("long")||type.equals("short")||type.equals("byte")||type.equals("Object")||type.equals("Integer")||type.equals("Long")||type.equals("Short")||type.equals("Byte"))
            return "0";
        else if(type.equals("char")||type.equals("Character"))
            return "'0'";
        else if(type.equals("double")||type.equals("Double"))
            return "0d";
        else if(type.equals("float")||type.equals("Float"))
            return "0f";
        else
            return "";
    }

    public String generateIntegral(String type){
        String value = "";
        int choose = random.nextInt(4);
        // System.out.println("choose:"+choose);
        switch(choose){
            case 0:
                value = generateRandom(type);break;
            case 1:
                value = generateMAX(type);break;
            case 2:
                value = generateMIN(type);break;
            case 3:
                value = generateZero(type);break;
        }
        return value;
    }

    public String generateFloatingPoint(String type){
        String value = "";
        int choose = random.nextInt(7);
        // System.out.println("choose:"+choose);
        switch(choose){
            case 0:
                value = generateRandom(type);break;
            case 1:
                value = generateMAX(type);break;
            case 2:
                value = generateMIN(type);break;
            case 3:
                value = generateNaN(type);break;
            case 4:
                value = generatePositiveInfinity(type);break;
            case 5:
                value = generateNegativeInfinity(type);break;
            case 6:
                value = generateZero(type);break;
        }
        return value;
    }

    public boolean isIntegral(String type){
        if(type.equals("int") || type.equals("long") || type.equals("short") || type.equals("byte") || type.equals("char") || type.equals("Object")){
            return true;
        }
        else if(type.equals("Integer") || type.equals("Long") || type.equals("Short") || type.equals("Byte") || type.equals("Character") || type.equals("Object")){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isFloatingPoint(String type){
        if(type.equals("float") || type.equals("double")){
            return true;
        }
        else if(type.equals("Float") || type.equals("Double")){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isString(String type){
        if(type.equals("String")){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isBoolean(String type){
        if(type.equals("boolean")){
            return true;
        }
        else if(type.equals("Boolean")){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isArray(String type){
        if(type.contains("[]") && !type.contains("<")){
            return true;
        }
        else{
            return false;
        }
    }
}
