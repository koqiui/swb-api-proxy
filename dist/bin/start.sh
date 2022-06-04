#!/bin/bash

script_path=$(readlink -f "$0")
script_dir=$(dirname $script_path)


jar_file=""
#确定jar文件
for file in $script_dir/*.jar
do
    if test -f $file 
    then
        jar_file=$file
        break
    fi
done

#echo $jar_file

#jar_name=$(basename $jar_file)

#echo $jar_name

#选项设置
java_opts="-Xms256m -Xmx1g -XX:NewRatio=1 -XX:SurvivorRatio=10 -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"

#echo $java_opts

nohup java ${java_opts} -jar ${jar_file}  > /dev/null 2>&1 &
