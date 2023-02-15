package com.jvmfuzzing.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.*;

public class GetFiles{
    public List<String> getFiles(String path) {              
        List<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tmpList = file.listFiles();
        // System.out.println("here are "+tmpList.length+" files.");
        for (int i = 0; i < tmpList.length; i++) {
            if (tmpList[i].isFile()) {
                files.add(tmpList[i].toString());
            }
            else if (tmpList[i].isDirectory()) {
                String newPath = path+"/"+tmpList[i].getName();
                List<String> more_files = getFiles(newPath);
                files.addAll(more_files);
            }
            else {
                System.out.println("This is neither a file nor a directory.\n"+tmpList[i].toString());
            }
        }
        // System.out.println("test:"+files.size());
        return files;
    }
    
    public void overWriteFile(String content,String filepath) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath))) { 
            bufferedWriter.write(content); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addWriteFile(String content,String filepath){
        FileWriter fw = null;
        try {
            File f=new File(filepath);
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(content);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

