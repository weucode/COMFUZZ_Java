import os
import subprocess as sp

currentPath = os.getcwd().replace('\\','/')

mvn_project = [ "/generate_tools/GenerateTestcases", "/mutate_tools/MutateByReplaceAPI", "/mutate_tools/MutateByReplaceVar",
                "/mutate_tools/MutateByAll"]

def run_cmd(cmd: str, work_dir: str):
    print("cmd:"+cmd)
    cp = sp.run(cmd,shell=True,encoding="utf-8",cwd=work_dir)
    if cp.returncode != 0:
        error = f"""Something wrong has happened when running command [{cmd}]:
            {cp.stderr}"""

        raise Exception(error)
    return cp.stdout,cp.stderr

def update_java_version():
    for project in mvn_project:
        with open(currentPath+project+"/pom.xml", "r") as f1:
            content = f1.read()
        with open(currentPath+project+"/pom.xml", "w") as f2:
            f2.write(content.replace("301", "361"))

def unzip_model():
    # run_cmd("unzip -q checkpoint-400000.zip", currentPath.replace("workline", "data/model"))
    # cmd = "unzip -q -d "+currentPath.replace("workline", "data/model")+" checkpoint-60000.zip"
    cmd = "tar zxf checkpoint-60000.tar.gz -C "+currentPath.replace("workline", "data/model")
    run_cmd(cmd, "/root")

def unzip_dependencies():
    run_cmd("unzip -q Dependencies.zip", currentPath+"/generate_tools")

def unzip_jvms():
    # run_cmd("unzip -q jvm_20230216.zip", "/root")
    run_cmd("tar zxf jvm.tar.gz", "/root")

def install_mvn_project():
    for i in range(3):
        run_cmd("mvn install", currentPath+mvn_project[i])

def create_data_file():
    os.makedirs(currentPath.replace("workline", "data"))
  os.makedirs(currentPath.replace("workline", "data/model"))

def main():
    create_data_file()
    unzip_model()
    unzip_dependencies()
    unzip_jvms()
    # update_java_version()
    install_mvn_project()


if __name__ == '__main__':
    create_data_file()
    unzip_model()
    unzip_dependencies()
    unzip_jvms()
    # update_java_version()
    install_mvn_project()
