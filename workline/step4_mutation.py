import os
import argparse
import subprocess as sp

def run_cmd():
    cp = sp.run(cmd,shell=True,capture_output=True,encoding="utf-8")
    if cp.returncode != 0:
        error = f"""Something wrong has happened when running command [{cmd}]:
            {cp.stderr}"""

        raise Exception(error)
    return cp.stdout,cp.stderr


currentPath = os.getcwd().replace('\\','/')

parser = argparse.ArgumentParser()
parser.add_argument("--file_path", type = str, default = currentPath.replace("workline","data")+"/assemble")
args = parser.parse_args()

path = './mutate_tools/MutateByAll'

cmd = "cd %s &&mvn package&&java -jar target/mutation_by_all-1.0-SNAPSHOT-shaded.jar %s" % (path, args.file_path)
print(cmd)
# os.system(cmd)
run_cmd(cmd)
