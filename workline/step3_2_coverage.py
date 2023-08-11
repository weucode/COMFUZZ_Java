import os
import re
import sys  
sys.path.append("/root/COMFUZZ/COMFUZZ_Java")
from src.studyMysql.db_operation_base import DataBaseHandle


handler = DataBaseHandle()

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
    delect_gcda = os.system("cd "+objs_path+"&&find . -name '*.gcda' | xargs -i rm -f {}")
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

def update_probability(start_id: int, end_id: int):
    ''' Update the test cases from start_id to end_id '''

    for i in range(start_id, end_id+1):
        param = (1, i)
        handler.update("update Table_Testcase set Probability=%s where id=%s", param)

def main():
    ''' Obtain code coverages '''

    start_id = 1
    old_cov_result_list, cov_result_list = [0,0,0], [0,0,0]
    # Get sum of test cases.
    sql = "select count(id) from Table_Testcase"
    res = handler.selectOne(sql, ())
    testcase_num = res[0]
    print("totally:",testcase_num)
    # Get the test cases finished javac.
    sql = "select max(Testcase_id) from Table_javac_Result;"
    res = handler.selectOne(sql, ())
    javac_num = res[0]
    print("javac number:",testcase_num)
    # Start to obtain the code coverages.
    while True:
        sql = "select max(id) from Table_Testcase where Fuzzing_times=1"
        res = handler.selectOne(sql, ())
        if res:
            end_id = res[0]
        else:
            end_id = 1
        print("max id:",end_id)
        cov_result_list = get_each_coverage()
        for i in range(3):
            if cov_result_list[i] > old_cov_result_list[i]:
                print("Higher coverage, update.")
                update_probability(start_id, end_id)
                old_cov_result_list = cov_result_list
                break
        print("im here")
        if end_id == testcase_num or javac_num >= testcase_num:
            break
        print("im here1")
        start_id = end_id


if __name__ == "__main__":
    main()