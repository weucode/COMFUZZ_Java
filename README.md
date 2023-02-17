# COMFUZZ_Java

If you want to rebuild the JVMs or update to the latest version, here is guidance for building JVMs from the beginning.



**HotSpot**

Download source code from the following website:

```
https://openjdk-sources.osci.io/openjdk8/
https://openjdk-sources.osci.io/openjdk11/
```

Build by the following steps:

```
./configure --with-boot-jdk=[boot-jdk path]
make all
```



**OpenJ9**

Download source code.

```
git clone https://github.com/ibmruntimes/openj9-openjdk-jdk8.git
cd openj9-openjdk-jdk8
bash get_source.sh
```

Build.

```
bash configure --with-boot-jdk=[boot-jdk path]
make all
```



**GraalVM**

Download the installer package.

```
wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.0.0.2/graalvm-ce-java11-linux-amd64-22.0.0.2.tar.gz
```

