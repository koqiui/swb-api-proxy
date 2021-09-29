#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import shutil
import subprocess

"""
    复制要发布的（依赖的 和 可执行)jar
    用法：copy-dist-jars.py
"""

# 当前脚本所在目录
__dir__ = os.path.abspath(os.path.dirname(__file__))

# 发布目录
distDir = os.path.join(__dir__, 'dist')
distBinDir = os.path.join(distDir, 'bin')
distJarsDir = os.path.join(distDir, 'jars')


def copyJars():

    os.chdir(__dir__)

    # 1、复制 依赖jar
    mvnArgs = [
        'mvn.cmd',
        'dependency:copy-dependencies',
        '-DoutputDirectory={0}'.format(distJarsDir)
    ]

    result = subprocess.call(mvnArgs)

    if result == 0:
        print('依赖的jar文件已复制到：{0}'.format(distJarsDir))
    else:
        print('依赖的jar文件复制失败')
        sys.exit(result)

    # 2、复制 执行jar
    jarName = os.path.basename(__dir__) + '.jar'
    jarSrcFilePath = os.path.join(__dir__, 'target', jarName)
    if os.path.exists(jarSrcFilePath):
        shutil.copy(jarSrcFilePath, distBinDir)
        print('{0} 已经复制到 {1} 下'.format(jarName, distBinDir))

    sys.exit(result)


# 单独调用
if __name__ == '__main__':
    copyJars()
else:
    print('不支持模块式调用')
    sys.exit(-1)
