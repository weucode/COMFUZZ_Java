import os
import jpype
import argparse


currentPath = os.getcwd().replace('\\','/')

def generate_testcase():
    # Repackage.
    task = os.popen("cd ./generate_tools/GenerateTestcases && mvn package")
    task.close()
    # Start JVM.
    jvmPath = jpype.getDefaultJVMPath()
    # Load jar package. 
    jpype.startJVM(jvmPath, "-ea", "-Djava.class.path="+currentPath+"/generate_tools/GenerateTestcases/target/generator_new-1.0-SNAPSHOT-shaded.jar")
    # print("start? "+str(jpype.isJVMStarted()))
    # Specify main class.
    JDClass = jpype.JClass("com.jvmfuzzing.generator.Main")
    jd = JDClass()
    jd.main(None)
    # Close JVM.
    jpype.shutdownJVM()


if __name__ == '__main__':
    generate_testcase()
