package com.jvmfuzzing.apigetter;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.File;
import java.util.*;

public class GetFiles{
    public List<String> getFiles(String path) {              
        List<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        // System.out.println("----here are "+tempList.length+" files.");
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                files.add(tempList[i].toString());
            }
            if (tempList[i].isDirectory()) {
                String fileName = tempList[i].getName();
                String newPath = path+"/"+fileName;
                List<String> more_files = new ArrayList<String>();
                more_files = getFiles(newPath);
                for(int j=0;j<more_files.size();j++)
                {
                    files.add(more_files.get(j));
                }
            }
        }
        return files;
    }

    public List<String> readText(String filename){
        BufferedReader reader;
        List<String> files = new ArrayList<String>();
        try {
		reader = new BufferedReader(new FileReader(filename));
		String line = reader.readLine();
		files.add(line);
		while (line != null) {
			// System.out.println(line);
			// read next line
			line = reader.readLine();
            if(line != null && !line.contains("$"))
                files.add(line);
		}
		reader.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return files;
    }

    public void writeFiles(String content,String filepath){
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath))) { 
            bufferedWriter.write(content); 
            // System.out.println("sucess!");
        } catch (IOException e) { 
            // System.out.println("fail!");
            e.printStackTrace(); 
        }
    }
    
    public void addToWrite(String content,String filepath){
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
