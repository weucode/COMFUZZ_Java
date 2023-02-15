package com.jvmfuzzing.mutation_by_all;
import java.io.*;
 
public class ReadTxt {
    public String Read(String filename)  {
        String con="";
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str="";
            while ((str = in.readLine()) != null) {
                // System.out.println(str);
                con = str+"\n";
            }
            // System.out.println(str);
        } catch (IOException e) {
        }
        return con;
    }
}