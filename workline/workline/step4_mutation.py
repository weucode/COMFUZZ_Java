import os
import argparse
import subprocess as sp

def run_cmd(cmd: str):
    cp = sp.run(cmd,shell=True,encoding="utf-8")
    if cp.returncode != 0:
        error = f"""Something wrong has happened when running command [{cmd}]:
            {cp.stderr}"""

        raise Exception(error)
    return cp.stdout,cp.stderr


def mutation():
    currentPath = os.getcwd().replace('\\','/')

    # parser = argparse.ArgumentParser()
    # parser.add_argument("--file_path", type = str, default = currentPath.replace("workline","data")+"/assemble")
    # args = parser.parse_args()

    mutation_code_path = './mutate_tools/MutateByAll'
    testcase_path = currentPath.replace("workline","data")+"/assemble"
    cmd = "cd %s &&mvn package&&java -jar target/mutation_by_all-1.0-SNAPSHOT-shaded.jar %s" % (mutation_code_path, testcase_path)
    # print(cmd)
    run_cmd(cmd)
