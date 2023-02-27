import collections
import json
import math
import tempfile
import logging
import pathlib
import subprocess
import gc
import re
from threading import Thread
from typing import List
import time as t
import os
from src.studyMysql.Table_Operation import Table_Testbed, Table_Result, Table_Suspicious_Result
from src.utils import labdate

currentPath = os.getcwd().replace('\\','/')

Majority = collections.namedtuple('Majority', [
    'majority_outcome', 'outcome_majority_size',
    'majority_stdout', 'stdout_majority_size'
])
javac_classpath = ["-classpath","/root/jvm/openjdk8/jdk8u322-b04/build/linux-x86_64-normal-server-release/images/j2sdk-image/lib:"
                                "/root/jvm/openjdk8/jdk8u322-b04/build/linux-x86_64-normal-server-release/images/lib:"
                                "/root/jvm/openjdk11/jdk-11.0.14-ga/build/linux-x86_64-normal-server-release/images/jdk/lib:"
                                "/root/jvm/openj9-jdk8/openj9-jdk8_0723/openj9-openjdk-jdk8/build/linux-x86_64-normal-server-release/images/lib:"
                                "/root/jvm/openj9-jdk8/openj9-jdk8_0723/openj9-openjdk-jdk8/build/linux-x86_64-normal-server-release/images/j2sdk-image/lib:"
                                "/root/jvm/openj9-jdk11/openj9-jdk11_0723/openj9-openjdk-jdk11/build/linux-x86_64-normal-server-release/images/jdk/lib:"
                                "/root/jvm/graal-jdk11/graalvm-ce-java11-22.0.0.2/lib:"
                                +currentPath+"/generate_tools/Dependencies"
]
code_files = "/tmp/comfuzz_jvm/"

class DifferentialTestResult:
    def __init__(self, function_id: int, testcase_id: int, error_type: str, testbed_id: int, testbed_location: str):
        self.function_id = function_id
        self.testcase_id = testcase_id
        self.error_type = error_type
        self.testbed_id = testbed_id
        self.testbed_location = testbed_location
        self.classify_result = None
        self.classify_id = None
        self.remark = None

    def serialize(self):
        return {"Differential Test Result": {"testcase_id": self.testcase_id,
                                             "error_type": self.error_type,
                                             "testbed_id": self.testbed_id,
                                             "function_id": self.function_id,
                                             "inconsistent_testbed": self.testbed_location,
                                             "classify_result": self.classify_result,
                                             "classify_id": self.classify_id
                                             }}

    def __str__(self):
        return json.dumps(self.serialize(), indent=4)

    def save_to_table_suspicious_Result(self):
        """
        Save the result to the database
        :return:
        """
        table_suspicious_Result = Table_Suspicious_Result()
        table_suspicious_Result.insertDataToTableSuspiciousResult(self.error_type, self.testcase_id, self.function_id, self.testbed_id,
                                                                  self.remark)


class HarnessResult:
    """
    This is the result type of the differential test, as opposed to ResultClass,
    which is the type that holds the results of the execution at runtime.
    """

    def __init__(self, function_id: int, testcase_id: int, testcase_context: str):
        self.function_id = function_id
        self.testcase_id = testcase_id
        self.testcase_context = testcase_context
        self.outputs: list[Output] = []

    def __str__(self):
        return json.dumps({"Harness_Result": {"testcase_id": self.testcase_id,
                                              "testcase_context": self.testcase_context,
                                              "outputs": [e.serialize() for e in self.outputs]
                                              }
                           }, indent=4)

    def get_majority_output(self) -> Majority:
        """Majority vote on testcase outcomes and outputs."""
        # print(result)

        majority_outcome, outcome_majority_size = collections.Counter([
            output.output_class for output in self.outputs
        ]).most_common(1)[0]
        majority_stdout, stdout_majority_size = collections.Counter([
            output.stdout for output in self.outputs
        ]).most_common(1)[0]
        return Majority(majority_outcome, outcome_majority_size,
                        majority_stdout, stdout_majority_size)

    def differential_test(self) -> List[DifferentialTestResult]:
        if self.outputs is None:
            return []
        ratio = 2 / 3
        majority = self.get_majority_output()
        # print(majority)
        testbed_num = len(self.outputs)
        # print(testbed_num)

        bugs_info = []
        for output in self.outputs:
            if output.output_class == "crash":
                bugs_info.append(
                    DifferentialTestResult(self.function_id, self.testcase_id, "crash", output.testbed_id,
                                           output.testbed_location))
                pass
            elif majority.majority_outcome != output.output_class and majority.outcome_majority_size >= math.ceil(
                    ratio * testbed_num):
                if majority.majority_outcome == "pass":
                    bugs_info.append(
                        DifferentialTestResult(self.function_id, self.testcase_id, "Most Java engines pass",
                                               output.testbed_id,
                                               output.testbed_location))
                elif majority.majority_outcome == "timeout":
                    # Usually, this is not a bug
                    pass
                elif majority.majority_outcome == "crash":
                    bugs_info.append(
                        DifferentialTestResult(self.function_id, self.testcase_id, "Most Java engines crash",
                                               output.testbed_id,
                                               output.testbed_location))
                elif majority.majority_outcome == "script_error":
                    bugs_info.append(
                        DifferentialTestResult(self.function_id, self.testcase_id,
                                               "Majority Java engines throw runtime error/exception",
                                               output.testbed_id,
                                               output.testbed_location))
            elif output.output_class == "pass" and majority.majority_outcome == output.output_class and \
                    output.stdout != majority.majority_stdout and \
                    majority.stdout_majority_size >= math.ceil(ratio * majority.outcome_majority_size):
                if majority.outcome_majority_size >= math.ceil(ratio * testbed_num):
                    bugs_info.append(
                        DifferentialTestResult(self.function_id,self.testcase_id, "Pass value *** run error", output.testbed_id,
                                               output.testbed_location))
        return bugs_info

    def save_to_table_result(self):
        """
        Save the result to the database.
        :return:
        """
        table_result = Table_Result()
        for output in self.outputs:
            table_result.insertDataToTableResult(self.testcase_id, output.testbed_id, output.returncode, output.stdout,
                                                 output.stderr, output.duration_ms, 0, 0,
                                                 None)


class Output:
    def __init__(self,
                 testbed_id: int,
                 testbed_location: str,
                 returncode: int,
                 stdout: str,
                 stderr: str,
                 duration_ms: int,
                 event_start_epoch_ms: int):
        self.testbed_id = testbed_id
        self.testbed_location = testbed_location
        self.returncode = returncode
        self.stdout = stdout
        self.stderr = stderr
        self.duration_ms = duration_ms
        self.event_start_epoch_ms = event_start_epoch_ms
        self.output_class = self.get_output_class()

    def get_output_class(self) -> str:
        """
        The order in which branches are judged cannot be reversed，
        because Whether the test case has a syntax error or not, chakraCore's returnCode is equal to 0
        """
        if self.returncode == -9 and self.duration_ms > 60 * 1000:
            return "timeout"
        elif self.returncode < 0:
            return "crash"
        elif self.returncode > 0 or not self.stderr == "":
            return "script_error"
        else:
            return "pass"

    def serialize(self):
        return {"testbed_id": self.testbed_id,
                "testbed_location": self.testbed_location,
                "returncode": self.returncode,
                "stdout": self.stdout,
                "stderr": self.stderr,
                "duration_ms": self.duration_ms,
                "event_start_epoch_ms": self.event_start_epoch_ms
                }

    def __str__(self):
        return json.dumps({"testbed_id": self.testbed_id,
                           "testbed_location": self.testbed_location,
                           "returncode": self.returncode,
                           "stdout": self.stdout,
                           "stderr": self.stderr,
                           "duration_ms": self.duration_ms,
                           "event_start_epoch_ms": self.event_start_epoch_ms},
                          indent=4)


class ThreadLock(Thread):
    def __init__(self, testbed_location, testcase_path, testbed_id):
        super().__init__()
        self.testbed_id = testbed_id
        self.output = None
        self.testbed_location = testbed_location
        self.testcase_path = testcase_path
        self.returnInfo = None

    def run(self):
        try:
            #添加javac编译的步骤
            self.output = self.run_test_case(self.testbed_location, self.testcase_path, self.testbed_id)
        except BaseException as e:
            self.returnInfo = 1

    def run_test_case(self, testbed_location: str, testcase_path1: pathlib.Path, testbed_id, time: str = "60"):
        try:
            testcase_path = str(testcase_path1)
            print("start java...")
            print(testcase_path)
            if os.path.exists(testcase_path):
                print("class file exists.")
            else:
                print("class file doesn't exist.")
            execute_path = testcase_path[:testcase_path.rindex("/")]
            print("execute_path:"+execute_path)
            
            # comfuzz argmuments
            cmd = ["timeout", "-s9", time]

            # for ob in testbed_location.split():
            #     cmd.append(ob)
            cmd.append(testbed_location)
 
            if "openj9" in testbed_location or "Openj9" in testbed_location:
                cmd.append("-XX:+CompactStrings")
                cmd.append("-Xnuma:none")
                cmd.append("-XX:-HandleSIGABRT")
                if "jdk8" in testbed_location:
                    cmd.append("-Djava.lang.string.substring.nocopy=true")
            
            cmd.append("-cp")
            jvm_name = testbed_location.split("/")[3]
            cmd.append(code_files + jvm_name + "/out:"+javac_classpath[1])

            # classming-fop arguments
            # cmd = [testbed_location]
            # cmd.append("-Xbootclasspath/a:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/xmlgraphics-commons-1.3.1.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/commons-logging.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/avalon-framework-4.2.0.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/batik-all.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/commons-io-1.3.1.jar:/root/oracle/jdk1.8.0_341/jre/lib/rt.jar")
            # cmd.append("-classpath")
            # cmd.append("/JVMfuzzing/zjy/zjy/classming/test1/sootOutput/fop/")
            # classming-jython arguments
            # cmd = [testbed_location]
            # cmd.append("-Xbootclasspath/a:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/guava-r07.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/constantine.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/jnr-posix.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/jaffl.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/jline-0.9.95-SNAPSHOT.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/antlr-3.1.3.jar:/JVMfuzzing/zjy/zjy/classming/test1/dependencies/asm-3.1.jar:/root/oracle/jdk1.8.0_341/jre/lib/rt.jar")
            # cmd.append("-classpath")
            # cmd.append("/JVMfuzzing/zjy/zjy/classming/test1/sootOutput/jython/")

            # cmd.append("-noverify")
            
            classfile_path = testcase_path[testcase_path.rindex("/")+1:].replace(".class", "")
            # cmd += str(classfile_path)
            # if "/fop/" in testcase_path:
            #     classfile_path = "org.apache.fop.cli.Main"
            # elif "/pmd/" in testcase_path:
            #     classfile_path = "net.sourceforge.pmd.PMD"
            # elif "/jython" in testcase_path:
            #     classfile_path = "org.python.util.jython"
            # else:
            #     classfile_path = ""
            cmd.append(str(classfile_path))

            start_time = labdate.GetUtcMillisecondsNow()
            print("java cmd:",cmd)
            pro = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                               stderr=subprocess.PIPE, universal_newlines=True, cwd=execute_path)
            stdout, stderr = pro.communicate()
            print("java stdout:",stdout)
            print("java stderr:",stderr)
        except Exception as e:
            print("java failed: ",e)
        end_time = labdate.GetUtcMillisecondsNow()
        duration_ms = int(round(
            (end_time - start_time).total_seconds() * 1000))
        event_start_epoch_ms = labdate.MillisecondsTimestamp(start_time)
        output = Output(testbed_id=testbed_id, testbed_location=testbed_location, returncode=pro.returncode,
                            stdout=stdout,
                            stderr=stderr,
                            duration_ms=duration_ms, event_start_epoch_ms=event_start_epoch_ms)
        return output
class Harness:

    @staticmethod
    def get_engines():
        table_testbed = Table_Testbed()
        testbed_list = table_testbed.selectAllIdAndLocateFromTableTestbed()
        return testbed_list

    def __init__(self):
        """
        initialize harness
        :param engines: engines to be test
        """
        self.engines = self.get_engines()
        self.name = ""

    # 取出第一个类的类名
    def get_class_name(self, Testcase_context):
        pattern = r"(public class )(MyJVMTest_[\d]*?)(?=(\s{)|{)"
        name = re.search(pattern, Testcase_context).group(2)
        self.name = name

    def  run_testcase(self, function_id: int, testcase_id: int, testcase_context: str) -> HarnessResult:
        """
        Execute test cases with multiple engines and return test results after execution of all engines.
        :param function_id: executed function Id
        :param testcase_id:  executed Testcases Id
        :param testcase_context: Testcases to be executed
        :return: test results
        """
        result = HarnessResult(function_id=function_id, testcase_id=testcase_id, testcase_context=testcase_context)
        result.outputs = self.multi_thread()
        #result.outputs = self.single_thread(testcase_path)
        return result

    def multi_thread(self) -> List[Output]:
        """
        Multithreading test execution test cases
        :param testcase_path: path of the test case
        :return: execution results of all engines
        """
        outputs = []
        threads_pool = []
        for engine in self.engines:
            testbed_id = engine[0]
            testbed_location = engine[1]
            jvm_name = testbed_location.split("/")[3]
            print(jvm_name)
            new_file_loc = code_files + jvm_name + "/out/" + self.name + ".class"
            testcase_path = pathlib.Path(new_file_loc)
            tmp = ThreadLock(testbed_location=testbed_location, testcase_path=testcase_path, testbed_id=testbed_id)
            threads_pool.append(tmp)
            tmp.start()
        for thread in threads_pool:
            thread.join()
            if thread.returnInfo:
                gc.collect()
            elif thread.output is not None:
                outputs.append(thread.output)
        return outputs



def single_thread(testcase_classname: str, testcase_context: str,engines) -> List[Output]:
    outputs = []
    counter = 0

    for engine in engines:
        testbed_id = engine[0]
        testbed_location = engine[1]
        jvm_name = testbed_location.split("/")[3]
        print(jvm_name)
        # new_classname = testcase_classname + "_" + jvm_name
        with open(code_files + jvm_name + "/" + testcase_classname + ".java", "w", encoding="utf-8")as f:
            testcase_path = pathlib.Path(code_files + jvm_name + "/" + testcase_classname + ".java")
            print(testcase_path)
            f.write(testcase_context)
            # testcase_context = testcase_context.replace(testcase_classname,new_classname,1)
            # print(testcase_context)
            output = run(testbed_location=testbed_location, testcase_path=testcase_path, testbed_id=testbed_id)
            outputs.append(output)
            counter += 1
            if counter >= 2:
                break

    return outputs

def run(testbed_location, testcase_path, testbed_id):
    try:
        output = run_test_case(testbed_location, testcase_path, testbed_id)
        return output
    except BaseException as e:
        print(e)
        returnInfo = 1


def run_test_case(testbed_location: str, testcase_path: pathlib.Path, testbed_id, time: str = "60"):
    cmd = "timeout -s9 "+time+" "
    #cmd = []
    testbed_location_javac = testbed_location + "c"
    #for ob in testbed_location_javac.split():
    #    cmd.append(ob)
    #print(type(testcase_path))
    testcase_path = testcase_path.as_posix()
    #cmd.append(str(testcase_path))
    #cmd.append("-d")
    jvm_name = testbed_location_javac.split("/")[3]
    #cmd.append("/tmp/" + jvm_name + "/out")
    cmd += testbed_location_javac+" "
    cmd += str(testcase_path)+" "
    cmd += "-d "+code_files + jvm_name + "/out "
    print(cmd)
    start_time = labdate.GetUtcMillisecondsNow()
    '''
    try:
        print("javac cmd:", cmd)
        p = subprocess.Popen(cmd, shell=False)
        
        stdout = None
        stderr = None
        end_time = labdate.GetUtcMillisecondsNow()
        duration_ms = int(round(
            (end_time - start_time).total_seconds() * 1000))
        event_start_epoch_ms = labdate.MillisecondsTimestamp(start_time)
        if stderr is not None or stdout is not None:
            # len(stderr) >= 0 or len(stdout) >= 0:
            output = Output(testbed_id=testbed_id, testbed_location=testbed_location, returncode=pro.returncode,
                            stdout=stdout,
                            stderr=stderr,
                            duration_ms=duration_ms, event_start_epoch_ms=event_start_epoch_ms)
        else:
            output = None

        return output
    
    except Exception as e:
        print("javac failed: ", e)
    '''
    output = None
    if output is not None:
        return output
    else:
        #t.sleep(10)
        print("start java...")
        testcase_path=pathlib.Path(testcase_path)
        classfilePath=code_files + testbed_location.split("/")[3] + "/out/" + testcase_path.name.replace(".java", ".class")
        print(classfilePath)
        if os.path.exists(classfilePath):
                print("class file exists.")
        else:
            print("class file not exists.")
        cmd +="&& timeout -s9 "+time+" "
        #cmd.append("&&")
        #cmd.append("timeout")
        #cmd.append("-s9")
        #cmd.append(time)
        cmd += testbed_location+" "
        cmd += "-cp "
        #for ob in testbed_location.split():
        #    cmd.append(ob)
        #cmd.append("-cp")
        jvm_name = testbed_location.split("/")[3]
        #cmd.append("/tmp/" + jvm_name + "/out")
        classfile_path = testcase_path.name.replace(".java", "")
        #cmd.append(str(classfile_path))
        cmd += code_files + jvm_name + "/out "
        cmd += classfile_path
        #cmd_str = " ".join(str(i) for i in cmd)
        start_time = labdate.GetUtcMillisecondsNow()
        pro = subprocess.Popen(cmd,shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE, universal_newlines=True)
        print("java cmd:", cmd)
        stdout, stderr = pro.communicate()
        print("java stdout:", stdout)
        print("java stderr:", stderr)
        end_time = labdate.GetUtcMillisecondsNow()
        duration_ms = int(round(
                (end_time - start_time).total_seconds() * 1000))
        event_start_epoch_ms = labdate.MillisecondsTimestamp(start_time)
        output = Output(testbed_id=testbed_id, testbed_location=testbed_location, returncode=pro.returncode,
                            stdout=stdout,
                            stderr=stderr,
                            duration_ms=duration_ms, event_start_epoch_ms=event_start_epoch_ms)
        return output
