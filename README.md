# 商维宝-api-代理
客户内网部署，用以代理到商维宝开放平台（open.shangwb.com/open.sweib.com）的api调用

> 本应用 为独立运行的web应用（发布目录为 dist）

> 主要作用是简化客户调用商维宝开放平台api所需的访问签名认证信息


#### 项目构建
- 条件：系统安装有jdk 8、maven、 python3 等基础必要的java构建环境工具

- 方法：执行 build.bat

- 非windows系统：需要调整 build.py 中的 'mvn.cmd' 为mvn可执行文件名，直接用 python ./build.py 构建


#### 配置调整（dist/conf目录下）
- base.properties 主要调整 服务端口（默认为：9900）

- app.properties 主要为客户自己对应于商维保平台（测试/生产环境）的App访问认证信息


#### 调用方式
> 以本机ip为 192.168.0.100，服务端口为 9900 为例（http://192.168.0.100:9900）

> 假定访问的为商维保平台的生产环境开放平台 https://open.sweib.com

- 对 http://192.168.0.100:9900/api/xxxxx 的访问 相当于 访问 https://open.sweib.com/api/xxxxx

- 而 客户无需关心api调用所必须的 access_token, auth_code, timestamp 等信息 以及 http 协议问题

---
#### 客户与商维宝开放平台对接各环境差异主要有3点：

> 测试环境：

1、开放平台基础url  http://open.shangwb.com   （未来可能改为https）

2、AppKey（client_id）：???

3、AppSecret（client_secret）：??？  
    可进入 管理控制台 自行修改

> 生产环境：

1、开放平台基础url  https://open.sweib.com

2、AppKey（client_id）：??？

3、AppSecret（client_secret）：??？  
    可进入 管理控制台 自行修改

> 提示：
- api的绝对url =  开放平台基础url + （api文档中以/开头的）相对url
- 开放平台的 api文档 和 管理控制台 请打开 开放平台基础url，自行查看 和 管理
- 针对某个api的具体url我们只会在最初几次给，以后客户需要自行到api文档查找记录/收藏
- 使用api-proxy时，就可以用自行部署的api-proxy的基础Url 替换 开放平台基础url了
- 自行对接开发可参考开放平台api文档中 **基本使用说明** 的 **快速接入参考代码**
<img width="600" height="480" alt="image" src="https://user-images.githubusercontent.com/6115009/135312886-bc170be5-12b3-4c99-b596-4b5e0929a14e.png">
