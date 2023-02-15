# !/bin/bash

# 测试种子程序的覆盖率
cd /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov_temp
/JVMfuzzing/zjy/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/jdk/bin/javac *.java 
#这一部分对整个文件夹下的java文件进行编译
traverse_dir()
{
    filepath=$1
    
    for file in `ls -a $filepath`
    do
        if [ -d ${filepath}/$file ]
        then
            if [[ $file != '.' && $file != '..' ]]
            then
                #递归
                traverse_dir ${filepath}/$file
            fi
        else
            #调用查找指定后缀文件
            check_suffix ${filepath}/$file
        fi
    done
}
##
check_suffix()
{
    file=$1
    if [ "${file##*.}"x = "class"x ];then
        echo $file
        timeout 10s /JVMfuzzing/zjy/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/jdk/bin/java $(basename $file .class)
        
    fi    
}

## 测试的路径
traverse_dir /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov_temp
# 进行覆盖率统计
echo "--------------Start cov----------"
cd /JVMfuzzing/zjy/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs
pwd
# lcov -b ./ -d ./ --rc lcov_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 --gcov-tool /usr/bin/gcov -c -o output.info
lcov -b ./ -d ./--gcov-tool /usr/bin/gcov -c -o output.info
# genhtml --rc genhtml_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 -o ../HtmlFile output.info|tee result.txt
genhtml -o ../HtmlFile output.info|tee result.txt
# 对获取到覆盖率进行处理
var=`tail -n -3 result.txt`
cov=`echo $var | \
    sed -e 's/[^%0-9 ]*//g;s/  */\n/g' | sed -n '/%/p'`
# 这一部分是获取百分比的
# OLD_IFS="$IFS"
# IFS=" "
array=($cov)
# IFS="$OLD_IFS"

# 这一部分是将种子程序的覆盖率部获取到数字，送到数组中进行比较
i=0
for vars in ${array[@]}
do

    nums[i]=`echo $vars | \
    sed 's/%//g' | awk '{print int($0)}'`
    echo ${nums[i]}
    let i++
done
# 因为会重复，所以要修改一下呢
find . -name "*.gcda" | xargs -i rm -f {}
echo `find . -name "*.gcda"`

# 这一部分对变异的结果进行测试
cd /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov
/JVMfuzzing/zjy/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/jdk/bin/javac *.java 

#这一部分对整个文件夹下的java文件进行编译
traverse_dir()
{
    filepath=$1
    
    for file in `ls -a $filepath`
    do
        if [ -d ${filepath}/$file ]
        then
            if [[ $file != '.' && $file != '..' ]]
            then
                #递归
                traverse_dir ${filepath}/$file
            fi
        else
            #调用查找指定后缀文件
            check_suffix ${filepath}/$file
        fi
    done
}
##
check_suffix()
{
    file=$1
    if [ "${file##*.}"x = "class"x ];then
        /JVMfuzzing/zjy/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/jdk/bin/java $(basename $file .class)
    fi    
}
## 测试的路径——需要修改
traverse_dir /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov

# 进行覆盖率统计
cd /JVMfuzzing/zjy/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs
pwd
# lcov -b ./ -d ./ --rc lcov_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 --gcov-tool /usr/bin/gcov -c -o output1.info
lcov -b ./ -d ./ --gcov-tool /usr/bin/gcov -c -o output1.info
# genhtml --rc genhtml_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 -o ../HtmlFile output1.info|tee result1.txt
genhtml -o ../HtmlFile output1.info|tee result1.txt
# 对获取到覆盖率进行处理
var1=`tail -n -3 result1.txt`
cov1=`echo $var1 | \
    sed -e 's/[^%0-9 ]*//g;s/  */\n/g' | sed -n '/%/p'`
# 这一部分是获取百分比的
array1=($cov1)
# 这一部分是将种子程序的覆盖率部获取到数字，送到数组中进行比较
i1=0
for vars1 in ${array1[@]}
do
    nums1[i1]=`echo $vars1 | \
    sed 's/%//g' | awk '{print int($0)}'`
    echo ${nums1[i1]}
    let i1++
done
i_cov=0
numss=0
# 这一部分进行比较的
for int_cov in ${nums1[@]}
do
    if [ ${nums[i_cov]} -gt $int_cov ];
    then echo ""
        echo ${nums[i_cov]}
        echo $int_cov
    else let numss++
	fi
    let i_cov++
done

if(( $numss > 1 ));then 
    mv /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov/*.java /JVMfuzzing/zjy/zjy/Result/MutationAndSeed
    rm -rf /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov_temp/*
    echo  `sed -i '1,$d' /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov_Txt/test_result.txt`
    echo "0">>/JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov_Txt/test_result.txt
    echo "覆盖率增加保存文件"
else 
    rm -rf /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov/*
    rm -rf /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov_temp/*
    echo  `sed -i '1,$d' /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov_Txt/test_result.txt`
    echo "2">>/JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov_Txt/test_result.txt
    echo "覆盖率没有增加"

fi
find . -name "*.gcda" | xargs -i rm -f {}
echo "---finish----"

