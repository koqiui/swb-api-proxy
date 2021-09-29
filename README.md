# swb-api-proxy
客户内网部署，用以代理到商维宝开放平台（open.shangwb.com/open.sweib.com）的api调用

> 本应用 为独立运行的web应用（发布目录为 dist）
> 主要作用是简化客户调用商维宝开放平台api所需的访问签名认证信息


#### 项目构建
- 条件：系统安装有jdk 8、maven、 python3 等基础必要的java构建环境工具

- 方法：执行 build.bat

- 非windows系统：需要调整 build.py 中的 'mvn.cmd' 为mvn可执行文件名，直接用 python ./build.py 构建


#### 配置调整（dist/conf目录下）
- base.properties 主要调整 服务端口（默认为：9900）

- app.properties 主要为客户自己对应与商维保平台（测试/生产环境）的App访问认证信息


#### 调用方式
> 以本机ip为 192.168.0.100，服务端口为 9900 为例（http://192.168.0.100:9900）
> 假定访问的为商维保平台的生产环境开放平台 https://open.sweib.com
- 对 http://192.168.0.100:9900/api/xxxxx 的访问 相当于 访问 https://open.sweib.com/api/xxxxx
- 而 客户无需关心api调用所必须的 access_token, auth_code, timestamp 等信息 以及 http 协议问题