import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class checkGenerate {
    public static void main(String[] args){
        int j = 0;
        File f = new File(args[0]);
        String fileList[] = f.list();
        for (int i = 0; i < fileList.length; i++) {
            System.out.println("checking:" + (i+1) + "/" + fileList.length);
            System.out.print(f + "/" + fileList[i] + "\n");
            List<String> message = checkGenerate .check(f + "/" + fileList[i]);
            if ( message.size() == 0) {
                j = j + 1;
            }
        }
        System.out.println("pass:"+j+ "/" + fileList.length);
    }

    public static List<String> check(String fileName) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();        
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(fileName));
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        File rawFile = new File(fileName);
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(rawFile));
            reader.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        List<String> options = new ArrayList<String>(Arrays.asList("-classpath", "../Dependencies", "-d", "../TmpClasses"));
        compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
        
        // Record the error message.
        List<String> messages = new ArrayList<String>();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            String line = diagnostic.getKind() + ":\t Line [" + diagnostic.getLineNumber() + "] \t Position [" + diagnostic.getPosition() + "]\t" + diagnostic.getMessage(Locale.ROOT) + "\n";
            messages.add(line);
            // System.out.println(line);
        }
        
        // If this code is passed, move it to another folder. 
        // File passFile = new File("../../../data/pass_data/checkpoint-300000");
        // if(!passFile.exists()){
        //     passFile.mkdir();
        // }
        // if(messages.size() == 0){
        //     File targetFile = new File("../../../data/pass_data/checkpoint-300000/"+fileName.substring(fileName.lastIndexOf("/")+1));
        //     rawFile.renameTo(targetFile); 
        // }
        
        if(messages.size() > 0){
            rawFile.delete();
        }

        return messages;
    }
}
