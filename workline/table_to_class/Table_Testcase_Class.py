import re
import subprocess
import sys
import tempfile

from src.studyMysql.Table_Operation import Table_Testcase, Table_Function
from workline.harness_tools.harness_class import Harness as H_java
from workline.harness_tools.harness_class_javac import Harness as H_javac


class Testcase_Object(object):
    def __init__(self, Testcase_item):
        self.Id = Testcase_item[0]
        self.Testcase_context: str = Testcase_item[1]
        self.SourceFun_id = Testcase_item[2]
        self.SourceTestcase_id = Testcase_item[3]
        self.Fuzzing_times = Testcase_item[4]
        self.Mutation_method = Testcase_item[5]
        self.Mutation_times = Testcase_item[6]
        self.Interesting_times = Testcase_item[7]
        self.Probability = Testcase_item[8]
        self.Remark = Testcase_item[9]

    def check_classfile(self) -> int:
        harness = H_javac()
        if self.Testcase_context != "":
            harness.get_class_name(self.Testcase_context)
            # print("start check class file...")
            counter = harness.check_classfile(harness.name)
            return counter
        # else:
        #     print(f"{self.Id} has no content.")

    def engine_compile_testcase(self):
        harness = H_javac()
        # print("Compiling the ",self.Id,"testcase...")
        if self.Testcase_context != "":
            harness.get_class_name(self.Testcase_context)
            name = harness.name
            compiled_result = harness.run_testcase(self.SourceFun_id, self.Id, self.Testcase_context)
            return compiled_result
        else:
            return None

    def engine_run_testcase(self):
        harness = H_java()
        # print("Running the ", self.Id, "testcase...")
        if self.Testcase_context != "":
            # print(f'Apllying {len(harness.get_engines())} engines to test jvm.')
            harness.get_class_name(self.Testcase_context)
            harness_result = harness.run_testcase(self.SourceFun_id, self.Id, self.Testcase_context)
            # add fuzzing time
            self.Fuzzing_times += 1
            return harness_result
        else:
            # print(f"{self.Id} has no content.")
            return None

    def add_interesting_times(self, interesting_number):
        self.Interesting_times += interesting_number

    def add_mutation_times(self, mutation_times):
        self.Mutation_times += mutation_times
    
    def updateFuzzingTimesInterestintTimes(self):
        table_Testcases = Table_Testcase()
        table_Testcases.updateFuzzingTimesInterestintTimes(self.Fuzzing_times,
                                                           self.Interesting_times, self.Id)
    
    def updateProbabilityToOne(self):
        table_Testcases = Table_Testcase()
        table_Testcases.updateProbability(1, self.Id)
