package com.jvmfuzzing.mutation_by_all;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreateJavaFile {
    public static void createFile(String filePath1,String fileName,String stringBuffer) throws IOException {
        String filePath = filePath1;
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File checkFile = new File(filePath + "/"+fileName);
        if(!checkFile.exists()){
            // System.out.println("CREAT:\t"+checkFile.toString());
            FileWriter writer = null;
            try {
                if (!checkFile.exists()) {
                    checkFile.createNewFile();
                }
                writer = new FileWriter(checkFile, true);
                writer.append(stringBuffer);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != writer)
                    writer.close();
            }
        }
        
    }

}