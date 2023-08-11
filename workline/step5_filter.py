import os
import re
import json
import time
import pymysql
import traceback


faulty_file_name = "faulty.log"
diff_file_name = "diff.log"
analized_file_name = "anal.log"


class ResultItem:
    def __init__(self, testcase_id: int, testbed_id: int, returncode: int, stdout: str, stderr: str):
        self.testcase_id = testcase_id
        self.testbed_id = testbed_id
        self.returncode = returncode
        self.stdout = stdout
        self.stderr = stderr


def analysis_all_fail(stderr: str):
    """ Get the type of error throwing and locate API """

    # Get the type of error throwing
    if "Exception in thread" in stderr:
        regex_for_err = r"Exception in thread \"main\" ([a-zA-Z.]+)"
    elif "Caused by" in stderr:
        regex_for_err = r"Caused by: ([a-zA-Z.]+):"
    else:
        regex_for_err = r"(java\.lang\.[a-zA-Z]+)"
    match_for_error = re.search(regex_for_err, stderr)
    if match_for_error:
        error_type = match_for_error.group(1)
    else:
        error_type = stderr
        with open("can_not_parse.log", "a+") as f:
            f.write("===stderr not match error:\n"+stderr+"\n")
    # Get Error Reporting API
    regex_for_api = r"at MyJVMTest_[0-9]+\.(.*?)\("
    match_for_api = re.search(regex_for_api, stderr)
    if match_for_api:
        located_api = match_for_api.group(1)
    else:
        located_api = stderr
        with open("can_not_parse.log", "a+") as f:
            f.write("===stderr not match api:\n"+stderr+"\n")
    return error_type, located_api


def has_diff_res(error_type_list: list, located_api_list: list):
    """ Determine if the behaviour is consistent based on the error type and the location APIs """
    
    print(str(len(located_api_list)))
    if len(located_api_list) > 1:
        return False
    if len(error_type_list) == 1:
        return False
    elif len(error_type_list) == 2:
        if ("java.lang.NoClassDefFoundError" in error_type_list and "java.lang.ClassNotFoundException")\
        or ("java.lang.ArrayIndexOutOfBoundsException" in error_type_list and "java.lang.IndexOutOfBoundsException" in error_type_list):
            return False
        else:
            return True
    elif len(error_type_list) == 3:
        if ("StringIndexOutOfBoundsException" in error_type_list and "java.lang.ArrayIndexOutOfBoundsException" in error_type_list and "java.lang.IndexOutOfBoundsException" in error_type_list):
            return False
        else:
            return True
    else:
        return True


class DatabaseOp:
    def __init__(self):
        self.sql_config = dict(host='comfuzz_mysql',
                    port=3306,
                    user='root',
                    passwd='mysql123',
                    db='JVMFuzzing',
                    charset='utf8mb4')
        self.mydb = pymysql.connect(**self.sql_config)
        self.mycursor = self.mydb.cursor()
  
    def get_res(self, testcase_id: int):
        """ Get results by id """
        
        res_list = []
        self.mycursor.execute("SELECT * FROM Table_Result WHERE Testcase_id=%s", (testcase_id,))
        result = self.mycursor.fetchone()
        while result is not None:
            res_list.append(ResultItem(result[1], result[2], result[3], result[4], result[5]))
            result = self.mycursor.fetchone()    
        return res_list

    def get_testcase(self, testcase_id: int):
        """ Get test case content by id """

        self.mycursor.execute("SELECT Testcase_context FROM Table_Testcase WHERE id=%s", (testcase_id,))
        result = self.mycursor.fetchone()
        if result:
            return result[0]
        else:
            return ""
  
    def get_res_fail(self):
        """ Get results(not 0) from the Table_Result and rejudge them. """
        
        self.mycursor.execute("SELECT DISTINCT Testcase_id FROM Table_Result")
        for row in self.mycursor.fetchall():
            testcase_id = row[0]
            print("--start to query result--")
            start_time = time.time()
            res_list = self.get_res(testcase_id)
            end_time = time.time()
            print("--finish to query result--"+str(end_time-start_time))
            error_type_list = []
            located_api_list = []
            run_failure = False
            for res in res_list:
                if res.returncode != 1:
                    break
                else:
                    error_type, located_api = analysis_all_fail(res.stderr)
                    print("error_type:", error_type, " located_api:", located_api)
                    error_type_list.append(error_type)
                    located_api_list.append(located_api)
                    run_failure = True
            if run_failure:
                located_api_list = list(set(located_api_list))
                error_type_list = list(set(error_type_list))
                if has_diff_res(error_type_list, located_api_list):
                    print("find one!")
                    with open(diff_file_name, "a+") as f:
                        f.write("\n==="+str(testcase_id))
                        f.write(str(error_type_list))
                        f.write(str(located_api_list))
                else:
                    with open(faulty_file_name, "a+") as f:
                        f.write("\n==="+str(testcase_id))
                        f.write(str(error_type_list))
                        f.write(str(located_api_list))
        self.mycursor.close()
        self.mydb.close()
  
    def has_faulty_keyword(self, testcase_content: str):
        """ Filtering test cases based on keywords for false positives """
        
        faulty_keyword_list = [ "System.getProperty", "System.nanoTime", "System.currentTimeMillis",
                                "System.identityHashCode",
                                "ManagementFactory", 
                                "new Random", "new ServerSocket", "new java.util.Date", "new Date", "mkdirs", 
                                "new byte[1024 * 1024]",
                                "jdk.nashorn.internal.runtime.Undefined", "com.sun.management.VMOption", 
                                "com.sun.management.DiagnosticCommandMBean", "java.time", 
                                "Math.floorMod", "Math.floorDiv", "Math.random()"]
        for keyword in faulty_keyword_list:
            if keyword in testcase_content:
                return True
        return False
    
    def is_analized_vul(self, testcase_content: str):
        """ Filter test cases based on keywords for found bugs """

        with open('analized_vul.json') as f:
            vul_info_dict = json.load(f)
        for vul_info in vul_info_dict.values():
            if vul_info["keywords"] in testcase_content:
                return True
        return False

    def get_res_by_susp(self):
        """ Get results from Table_Suspicious_Result """
        
        faulty_record, diff_record, analized_record = [], [], []
        self.mycursor.execute("SELECT DISTINCT Testcase_id FROM Table_Suspicious_Result")
        # Iterate through each suspicious use case. 
        for row in self.mycursor.fetchall():
            testcase_id = row[0]
            testcase_content = self.get_testcase(testcase_id)
            # If the test case contains a false keyword, record it in the false log. 
            if len(testcase_content) == 0:
                print(str(testcase_id)+" has no content.")
            elif self.has_faulty_keyword(testcase_content):
                faulty_record.append("==="+str(testcase_id)+"\n"+testcase_content)
            elif self.is_analized_vul(testcase_content):
                analized_record.append("==="+str(testcase_id)+"\n"+testcase_content)
            else:
                res_list = self.get_res(testcase_id)
                if len(res_list) == 0:
                    faulty_record.append("==="+str(testcase_id)+"\n"+testcase_content)
                else:
                    diff_record.append("==="+str(testcase_id)+"\n"+testcase_content)
        self.mycursor.close()
        self.mydb.close()
        # Start to write.
        with open(faulty_file_name, "w") as f1:
            f1.write("\n".join(faulty_record))
        with open(diff_file_name, "w") as f2:
            f2.write("\n".join(diff_record))
        with open(analized_file_name, "w") as f3:
            f3.write("\n".join(analized_record))


def filter_by_testcase():
    if os.path.isfile(faulty_file_name):
        os.remove(faulty_file_name)
    if os.path.isfile(diff_file_name):
        os.remove(diff_file_name)
    db_op = DatabaseOp()
    db_op.get_res_by_susp()


def filter_by_result():
    if os.path.isfile(diff_file_name):
      os.remove(diff_file_name)
    if os.path.isfile(faulty_file_name):
      os.remove(faulty_file_name)
    db_op = DatabaseOp()
    db_op.get_res_fail()


if __name__ == "__main__":
    # filter_by_result()
    filter_by_testcase()
