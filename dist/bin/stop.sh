#!/bin/bash

script_path=$(readlink -f "$0")
script_dir=$(dirname $script_path)

# 目标进程标记串
dest_flag="${script_dir}"

dest_pid=$(ps -ef |grep ${dest_flag} |grep -v grep | awk '{print $2}')
dest_pid=$((${dest_pid:=0})) # 给默认值并转为数值

if [ $dest_pid -gt 0 ]; then
    echo "${script_dir} is being killed ${dest_pid}"

    /usr/bin/kill -9 $dest_pid

    echo "${script_dir} has been killed"
else
    echo "${script_dir} not running"
fi