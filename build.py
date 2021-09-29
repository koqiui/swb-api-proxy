#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import shutil
import subprocess

"""
    构建 + 发布项目（到dist目录）
    用法：build.py
"""

# 当前脚本所在目录
__dir__ = os.path.abspath(os.path.dirname(__file__))

# 发布目录
distDir = os.path.join(__dir__, 'dist')
distBinDir = os.path.join(distDir, 'bin')
distJarsDir = os.path.join(distDir, 'jars')

# 自定义设置提示
customizeSettings = '调整好 dist/conf 下的 base.properties 和 app.properties， 即可运行 或 打包发分发了'


def copyJars():

    os.chdir(__dir__)

    # 1、构建项目
    mvnArgs = [
        'mvn.cmd',
        'clean',
        'package',
        'install'
    ]
    result = subprocess.call(mvnArgs)
    if result == 0:
        print('项目构建 成功')
    else:
        print('项目构建 失败')
        sys.exit(result)

    # 2、复制 依赖jar
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

    # 3、复制 执行jar
    jarName = os.path.basename(__dir__) + '.jar'
    jarSrcFilePath = os.path.join(__dir__, 'target', jarName)
    if os.path.exists(jarSrcFilePath):
        shutil.copy(jarSrcFilePath, distBinDir)
        print('{0} 已经复制到 {1}'.format(jarName, distBinDir))

    print()
    print('------------------- :-) --------------------')
    print()
    print('恭喜您！dist 目录下就是项目运行所必需的全部文件')
    print()
    print(customizeSettings)

    sys.exit()


# 单独调用
if __name__ == '__main__':
    copyJars()
else:
    print('不支持模块式调用')
    sys.exit(-1)
