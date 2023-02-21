import os
import re
import time
#export LC_ALL=C
from multiprocessing.dummy import Pool as ThreadPool
import sys
from pathlib import Path
BASE_DIR = str(Path(__file__).resolve().parent.parent)
sys.path.append(BASE_DIR)
from tqdm import tqdm

from src.studyMysql.Table_Operation import Table_Testcase,Table_Result

from workline.table_to_class.Table_Testcase_Class import Testcase_Object


table_Testcases = Table_Testcase()


def muti_javac_compile(testcase_object):
    # testcase_object = Testcase_Object(testcase)
    tqdm.write('*' * 25 + f'compile{testcase_object.Id}' + '*' * 25)
    start_time = time.time()
    Compiled_result = testcase_object.engine_compile_testcase()
    if Compiled_result != None:
        # print(testcase_object.Id," Start insert results...",len(Compiled_result.outputs))
        Compiled_result.save_to_table_result()
        # Execute vote mechanism.
        different_result_list = Compiled_result.differential_test()
        # print(different_result_list)
        if not len(different_result_list):
            tqdm.write("nothing happened.")
        else:
            tqdm.write("{}error has been occured by java compiler.".format(len(different_result_list)))

            # Store suspicious results in Table_javac_Suspicious_Result.
            for interesting_test_result in different_result_list:
                # print(interesting_test_result)
                interesting_test_result.save_to_table_suspicious_Result()

        tqdm.write(f'Spending time:{int(time.time() - start_time)}s')
    else:
        tqdm.write('*' * 25 + f'{testcase_object.Id} has no content.' + '*' * 25)

def check(testcase_object):
    counter = testcase_object.check_classfile()
    tqdm.write(f'{testcase_object.Id} classfiles has been made: ' + str(counter))
    return counter

def muti_harness(testcase_object):
    # Check if the class file exists.
    counter = check(testcase_object)
    if counter == 0:
        tqdm.write(f"{testcase_object.Id} no classfile found.")
        return
    # print('*' * 25 + f'running{testcase_object.Id}' + '*' * 25)
    start_time = time.time()
    # Obtain each engine outputs
    harness_result = testcase_object.engine_run_testcase()
    if harness_result != None:
        # Save result.
        try:
            harness_result.save_to_table_result()
        except:
            pass
        
        # Execute vote mechanism.
        different_result_list = harness_result.differential_test()

        if not len(different_result_list):
            tqdm.write("nothing happened.")
        else:
            tqdm.write("{}error has been occured by JVM.".format(len(different_result_list)))
            # Set property.
            testcase_object.add_interesting_times(1)

            # Store suspicious results in Table_Suspicious_Result.
            for interesting_test_result in different_result_list:
                # print(interesting_test_result)
                interesting_test_result.save_to_table_suspicious_Result()

    tqdm.write(f'Spending time:{int(time.time() - start_time)}s')
    # Update fuzzing time and interesting time in Table_Testcase.
    testcase_object.updateFuzzingTimesInterestintTimes()


def get_each_coverage():
    """ Get coverage information """

    objs_path = "/root/jvm/openjdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs"
    # Generate coverage file
    os.system(
        "cd "+objs_path+"&&lcov -b ./ -d ./ --rc lcov_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 --gcov-tool /usr/bin/gcov -c -o output.info&&genhtml --rc genhtml_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 -o ../HtmlFile output.info|tee result.txt")
    # Read and get the coverage of line, function and branch.
    getcov_float_cmd = "tail -n -3 "+objs_path+"/result.txt"
    cov_result_list = get_coverage_value(getcov_float_cmd)
    # Delete gcda file to prevent accumulation.
    # delect_gcda = os.system("cd "+objs_path+"&&find . -name '*.gcda' | xargs -i rm -f {}")
    return cov_result_list


def get_coverage_value(cmd):
    ''' Get the last three lines of result.txt and write the value into csv file. '''

    cev_result = os.popen(cmd).read()
    print(cev_result)
    cov_result_list = re.findall('\d+\.?\d*%', cev_result)
    print("the result of cov:",cov_result_list)
    cov_folat = []
    for i in cov_result_list:
        folat_result = float(i.strip('%'))
        folat_result = folat_result / 100.0
        folat_result = round(folat_result, 3)
        print(folat_result)
        cov_folat.append(folat_result)
    return cov_folat


def harness(testcase_type):
    start_time = time.time()
    if testcase_type == 0:
        # Obtain the untested test cases by Fuzzing_times=0
        list_unfuzzing = table_Testcases.selectFuzzingTimeFromTableTestcase(0)
    elif testcase_type == 1:
        # Obtain the mutated test cases(interesting)
        list_unfuzzing = table_Testcases.selectInterestingTestcase()
    elif testcase_type == 2:
        # Obtain the mutated test cases(not interesting)
        list_unfuzzing = table_Testcases.selectNotInterestingTestcase()
    else:
        raise RuntimeError("Invalid value for argument 'testcase_type'!")

    old_cov_result_list = [0,0,0]
    pbar = tqdm(total=len(list_unfuzzing))
    print("list_unfuzzing size:",len(list_unfuzzing))
    # for index in range(0):
    for index in range(len(list_unfuzzing)):
        testcase = list_unfuzzing[index]
        if testcase[1] == "":
            pbar.update(1)
            continue
        testcase_object = Testcase_Object(testcase)
        origin_fuzzing_time = testcase_object.Interesting_times

        # Execute the test case.
        muti_javac_compile(testcase_object)
        muti_harness(testcase_object)

        # If do not trigger different behaviors, obtain the code coverage.
        print(origin_fuzzing_time," ",testcase_object.Interesting_times)
        if testcase_object.Interesting_times == origin_fuzzing_time:
            cov_result_list = get_each_coverage()
            for i in range(3):
                if cov_result_list[i] > old_cov_result_list[i]:
                    testcase_object.updateProbabilityToOne()
                    old_cov_result_list = cov_result_list
                    break
        pbar.update(1)

    end_time =time.time()

    tqdm.write(f'take {int(end_time-start_time)}s')

if __name__ == '__main__':
    print('''Select the part of testcase to apply differential test:
        0-fuzzing origin testcases
        1-fuzzing mutated testcases(interesting)
        2- fuzzing mutated testcases(not interesting)
    ''')
    choice = input()
    if choice in [0,1,2]:
        harness(choice)
    else:
        print("Invalid input!")
