# !/bin/bash
# 先进行变异，将一次变异完成后，就开始进行覆盖率测试

#遍历文件夹及其子文件夹内所有文件，并查看各个文件大小
dir=" /JVMfuzzing/zjy/zjy/Result/ResultOfMutationAndCov_temp/" #要遍历的目录

#子函数getdir
function getdir()
{
    for element in `ls $1`
    do
        file=$1"/"$element
        if [ -d $file ]
        then
            echo $file
            let i++
        fi
    done
}
getdir $dir #引用子函数

# for line in `cat /root/dir.out`  #读取文件dir.out的每行
# do
#     filesize=`ls -l $line | awk '{ print $5 }'`  #读取文件大小
#     echo $filesize
# done