package com.jvmfuzzing.generator;

import java.util.*;

import com.google.common.collect.ImmutableMap;

public class GroupGenerator{
    class GroupInfo{
        private String value;
        private String groupType;
        private String implementationClass;

        public GroupInfo(String value, String groupType, String implementationClass){
            this.value = value;
            this.groupType = groupType;
            this.implementationClass = implementationClass;
        }

        public String getValue(){
            return value;
        }

        public String getGroupType(){
            return groupType;
        }

        public String getImplementationClass(){
            return implementationClass;
        }
    }

    static final List<String> groupTypeList = Arrays.asList(
        "List", "ArrayList", "Set", "EnumSet", "TreeSet", "Map", "EnumMap", "Hashtable");

    String getGroupTypeFromStr(String typeStr){
        int index = typeStr.indexOf("<");
        if(index>0)
            typeStr = typeStr.substring(0,index);
        return typeStr;
    }
    
    boolean isGroupType(String typeStr){
        String groupType = getGroupTypeFromStr(typeStr);
        return groupTypeList.contains(groupType);
    }

    List<String> getElementType(String typeStr){
        // System.out.println("typeStr="+typeStr);
        List<String> elementTypeList = new ArrayList<String>();
        String paramStr = typeStr.substring(typeStr.indexOf("<")+1,typeStr.lastIndexOf(">"));
        String[] elementTypeArray = paramStr.split(",");
        if(elementTypeArray.length==1){
            elementTypeList.add(elementTypeArray[0]);
        }
        else{
            for(String elementType:elementTypeArray){
                elementTypeList.add(elementType);
            }
        }
        return elementTypeList;
    }

    // This variable is used to specify the length of the assignment statements.
    static final int length = 3;

    Map<String,String> implementationClassMap = ImmutableMap.<String,String>builder()
        .put("Set","HashSet")
        .put("List","ArrayList")
        .put("Map","HashMap")
        .build();

    GroupInfo generateGroupInit(String variableTypeStr){
        String groupType = getGroupTypeFromStr(variableTypeStr);
        String implementationClass = implementationClassMap.get(groupType);
        if(implementationClass==null){
            implementationClass = groupType;
        }
        // String implementationClass = getImplementationClass(groupType);
        String groupInit = "new "+variableTypeStr.replace(groupType,implementationClass)+"()";
        // return groupInit;
        return new GroupInfo(groupInit,groupType,"java.util."+implementationClass);
    }

    List<VariableComponent> generateGroup(String variableTypeStr,String variableName,String paramPattern){
        // TestcaseGenerater t = new TestcaseGenerater();
        List<VariableComponent> resultList = new ArrayList<VariableComponent>();
        // Add the group declaration first.
        GroupInfo groupInfo = generateGroupInit(variableTypeStr);
        // String groupDec = variableTypeStr+" "+variableName+" = "+groupInfo.getValue()+";\n";
        if(groupInfo.getImplementationClass().length()>0){
            resultList.add(new VariableComponent(variableTypeStr,variableName,groupInfo.getValue(),groupInfo.getImplementationClass()));
        }
        else{        
            resultList.add(new VariableComponent(variableTypeStr,variableName,groupInfo.getValue()));
        }
        // // Get element type,for example,'List<String,String>'->'[String,String]'
        // List<String> elementTypeList = new ArrayList<String>();
        // try{
        //     elementTypeList = getElementType(variableTypeStr);
        // }
        // catch(Exception e){
        //     System.out.println("Error: "+e.getMessage());
        //     return resultList;
        // }
        // // Prepare the set value statement,for example'set.add(' or 'list.add(' or 'map.put('
        // List<String> elementDecList = new ArrayList<String>();
        // if(groupInfo.getGroupType().contains("Map")){
        //     for(int k=0;k<length;k++){
        //         elementDecList.add(variableName+".put(");
        //     }
        // }
        // else{
        //     for(int k=0;k<length;k++){
        //         elementDecList.add(variableName+".add(");
        //     }
        // }
        // // Start to generate parameter,for example'String param0 = xxx;'.
        // for(int i=0;i<elementTypeList.size();i++){
        //     String elementType = elementTypeList.get(i);
        //     String elementName = "";
        //     for(int j=0;j<length;j++){
        //         if(isGroupType(elementType)){
        //             String subElementType = getGroupTypeFromStr(elementType);
        //             String subElementName = "sub"+subElementType+i+"_"+j;
        //             List<String> subElementDec = generateGroup(elementType,subElementName,"sub"+paramPattern+j+"_");
        //             resultList.addAll(subElementDec);
        //             if(i<elementTypeList.size()-1)
        //                 elementDecList.set(j,elementDecList.get(j)+subElementName+", ");
        //             else
        //                 elementDecList.set(j,elementDecList.get(j)+subElementName);
        //         }
        //         else{
        //             // For example 'param0_0','param'0_1','param0_2'...
        //             elementName = paramPattern+String.valueOf(i)+"_"+String.valueOf(j);
        //             resultList.addAll(t.generateDec(elementName, elementType));
        //             if(i<elementTypeList.size()-1)
        //                 elementDecList.set(j,elementDecList.get(j)+elementName+", ");
        //             else
        //                 elementDecList.set(j,elementDecList.get(j)+elementName);
        //         }
        //     }
        // }
        // // Add 'add' or 'put' statement.
        // for(String elementDecStr:elementDecList){
        //     // newClassDec += elementDecStr+");\n";
        //     resultList.add(elementDecStr+");\n");
        // }
        // // The order is 'String Param0 = xxx;List<String> list = new ArrayList<String>();list.add(param0);'
        return resultList;
    }
}