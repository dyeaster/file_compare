## 介绍

* `file-compare` 是一个配置文件对比同步工具，可以对比主机以及从机的配置文件之间的差别，然后同步主机的配置文件到从机
* 首先配置项目信息，项目下的服务器信息，服务器的配置文件信息
* 然后可以收集目标文件存储到本机，然后对比出文件差异展示到前台页面，同步配置文件覆盖远程服务器文件
* 对于properties和ini文件，能实现文件内容的精确对比以及差异展示，同时能替换掉配置文件跟与主机相关的配置信息
* 同步配置文件之前，由于会覆盖之前的文件，会先对目标文件或者文件夹进行备份，然后在进行同步

### 前台页面功能介绍
 
 * Project Config 项目增删改查，一个项目对应多台主机的配置文件，项目信息的维护
 * Server Config 服务器信息增删改查，配置对应项目下的服务器信息，固定信息(ip, port, user, password)，可选信息自行添加，此配置信息可用于替换配置文件中跟主机相关的特有信息
 * File Config 文件信息增删改查，配置文件信息，包括(源文件本机路径，目标文件路径，文件类型，是否为文件夹，对比方式，valueMap配置需要替换的字段)
 * File Compare(文件手机，对比差异展示，同步)
 
## 工具架构
 * 前端 layui
 * 后台 springboot jpa 
 * 数据库 sqllite
 * 其他工具 ganymed-ssh2 用于ssh登录，scp传输文件
 

## Introduction

`file-compare` is a simple, yet powerful tool to find differences between different files. And show the differences in the html, and then can backup the remote file and sync the local file to remote server.

### Features
 
 * CURD project information (contains name and comments, different project has different distributed server machine.)
 * CURD server information (such as ip, port, user, password and so on.)
 * CURD file information (files or dirs need to compare and sync. sourceFile, targetFile, fileType, compareMethod, dirFlag, valueMap, excludeFile)
 * File Compare(Main page: collect file by selected server, and then show the differences between targetFile and SourceFile, then can sync sourceFile to remote server after backup targetFile)
 
## Getting Started
 
clone this project and run in idea;

### tools and framework
  * springboot
  * jpa
  * sqlite (database)
  * ganymed-ssh2 (for ssh2 and scp file)
  * layui(frontEnd)
