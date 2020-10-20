spring boot 整合 activiti

---
title: Activiti 学习笔记
date: 2020-10-15 09:40:11
tags:
---

[TOC]

## 1 Activiti 可以做什么

Activiti 是一个业务流程管理(BPM)框架，它是覆盖了业务流程管理、工作流、服务协作等领域的一个开源的、灵活的、易扩展的可执行流程语言框架。通过使用 Activiti，可以实现业务流程整体或部分的自动化。

## 2 Activiti 简介
### 2.1 BPMN
业务流程建模与标注（Business Process Model and Notation，BPMN) ，描述流程的基本符号，包括这些图元如何组合成一个业务流程图（Business Process Diagram）

### 2.2 Activiti 工作流程
1. 定义工作流: 就是工作流的步骤的怎么样的
2. 部署工作流
3. 执行该工作流: 工作流就按照预定义的步骤执行

### 2.1 表分类及简介

Activiti 工作流总共包含23张数据表，所有的表名默认以`ACT_`开头，并且表名的第二部分用两个字母表明表的用例。

1. **ACT_GE_***:`GE`代表`General(通用)`

2. **ACT_HI_***:`HI`代表`History(历史)`，这些表中保存的都是历史数据，比如执行过的流程实例、变量、任务等
    Activit默认提供了4种历史级别:
    * none: 不保存任何历史记录，可以提高系统性能
    * activity：保存所有的流程实例、任务、活动信息
    * audit：也是 Activiti 的默认级别，保存所有的流程实例、任务、活动、表单属性
    * full：最完整的历史记录，除了包含 audit 级别的信息之外还能保存详细，例如：流程变量
 

3. **ACT_ID_***:`ID`代表`Identity(身份)`，这些表中保存的都是身份信息，如用户和组以及两者之间的关系。如果 Activiti 被集成在某一系统当中的话，这些表可以不用，可以直接使用现有系统中的用户或组信息

4. **ACT_RE_***:`RE`代表`Repository(身份)`，这些表中保存一些静态资源，如流程定义和流程资源(如图片、规则等)

5. **ACT_RU_***:`RU`代表`Runtime(运行时)`，这些表中保存一些流程实例、用户任务、变量等的运行时数据。Activiti 只保存流程实例在执行过程中的运行时数据，并且当流程结束后会立即移除这些数据，这是为了保证运行时表尽量的小并运行的足够快

### 2.2 表结构
| 表名 | 备注 |
| --- | --- |
|ACT_GE_BYTEARRAY|通用的流程定义和流程资源|
|ACT_GE_PROPERTY|系统相关属性|
|ACT_HI_ACTINST|历史的流程实例|
|ACT_HI_ATTACHMENT|历史的流程附件|
|ACT_HI_COMMENT|历史的说明性信息|
|ACT_HI_DETAIL|历史的流程运行中的细节信息|
|ACT_HI_IDENTITYLINK|历史的流程运行过程中用户关系|
|ACT_HI_PROCINST|历史的流程实例|
|ACT_HI_TASKINST|历史的任务实例|
|ACT_HI_VARINST|历史的流程运行中的变量信息|
|ACT_ID_GROUP|身份信息-组信息|
|ACT_ID_INFO|身份信息-组信息|
|ACT_ID_MEMBERSHIP|身份信息-用户和组关系的中间表|
|ACT_ID_USER|身份信息-用户信息|
|ACT_RE_DEPLOYMENT|部署单元信息|
|ACT_RE_MODEL|模型信息|
|ACT_RE_PROCDEF|已部署的流程定义|
|ACT_RU_EVENT_SUBSCR|运行时事件|
|ACT_RU_EXECUTION|运行时流程执行实例|
|ACT_RU_IDENTITYLINK|运行时用户关系信息|
|ACT_RU_JOB|运行时作业|
|ACT_RU_TASK|运行时任务|
|ACT_RU_VARIABLE|运行时变量表|


## 3 Activiti 业务名词

![Activiti实例关系](/images/activit实例关系.png)

### 3.1 ProcessDefinition 流程定义
ProcessDefinition 是整个流程步骤的说明而 ProcessInstance 就是指流程定义从开始到结束的那个最大的执行路线。 

### 3.2 ProcessInstance 流程实例

流程实例就是根据一次业务数据用流程驱动的入口，两者之间是一对一的关系。

流程引擎会创建一条数据到`ACT_RU_EXECUTION`表，同时也会根据history的级别决定是否查询相同的历史数据到`ACT_HI_PROCINST`表。

启动完流程之后业务和流程已经建立了关联关系，第一步结束。

启动流程和业务关联区别：

1. 对于自定义表单来说启动的时候会传入businessKey作为业务和流程的关联属性
2. 对于动态表单来说不需要使用businessKey关联，因为所有的数据都保存在引擎的表中
3. 对于外部表单来说businessKey是可选的，但是一般不会为空，和自定义表单类似

### 3.3 Execution 执行实例
Execution 是按照 ProcessDefinition 的规则执行的当前的路线，
如果 ProcessDefinition 只有一个执行路线的话，那么 Execution 和 ProcessInstance 就是完全一样。如果 ProcessDefinition 中有多个执行路线的话，Execution 和 ProcessInstance 可能是同一个也可能不是同一个。所以一个流程中 ProcessInstance 有且只能有一个，而 Execution 可以存在多个。

```
ProcessInstance(1)--->Execution(N>=1)。
```

### 3.4 Task 任务
就是当流程执行到某步骤或某环节时生产的任务信息。Task 是在流程定义中看到的最大单位，每当一个 Task 标记完成 (complete) 时候会引擎把当前的任务移动到历史中，然后插入下一个任务插入到 `ACT_RU_TASK` 中。

### 3.5 HistoryActivity 历史活动

在 Task 中完成流程的时候所有下一步要执行的任务(包括各种分支)都会创建一个 Activity 记录到数据库。

## 4 Activiti 使用
### 4.1 环境搭建
IDE 使用 IDEA。
框架及中间件依赖如下:

* jdk 1.8
* spring boot 2.3.4.RELEASE
* mysql-connector-java
* mybatis 3.5.0
* activiti-spring-boot-starter-basic 5.23.0
* spring-boot-starter-test
* junit-platform-launcher
* maven 3.6.3 

### 4.2 定义流程图
IDEA 的 actiBPM 插件有点问题，推荐使用 eclipse。
> 本文以请假为例进行演示，包含`开始->请假申请->部门领导审批->HR审批->结束`

**创建一个流程图**

`IDEA->File->BPMN FILE`输入需要创建的流程图名称

![请假流程图](/images/请假流程图.png) 
一个流程图必须包含开始和结束事件
### 4.4 Activiti 组件
有事件、任务、容器、网关等，常用的如下:

* Palette 连线
* Start event 开启任务事件
* End event 任务结束事件
* Task 任务(用户任务，接收任务)
* Gateway 网关(并行网关，排它网

> IDEA 2020 不能显示`BPM Editor`，如果想编辑设置参数属性之类，还是推荐使用 eclipse，下文将通过代码方式操作设置，所以略过eclipse设置

### 4.5 流程部署与执行
>由于是基于Spring boot，项目设置比较简答，无需繁琐的设置，只需要在 application.yml配置基本设置

创建一个 Spring boot 项目后，引入上文提到的依赖，并修改配置文件

```
spring:
  datasource:
    username: root
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/activiti_test?useSSL=false&characterEncoding=utf8&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true
  activiti:
    database-schema-update: true
#    check-process-definitions: false
```

Spring boot 默认在 resources/processes 寻找流程定义文件，且文件夹不能为空，可以通过`check-process-definitions`设置关闭

#### 4.5.1 获取工作流引擎

![获取工作流引擎](/images/获取工作流引擎.png)

#### 4.5.2 定义工作流
可以通过插件可视化编辑工作流的 name、assignee 等，本文忽略，由下文通过代码操作演示

#### 4.5.3 部署工作流

![部署工作流](/images/部署工作流.png)

此时在表`ACT_RE_DEPLOYMENT`中新增一条记录

#### 4.5.4 开启一个流程实例

![开启工作流实例](/images/开启工作流实例.png)

#### 4.5.5 查询流程实例状态

![查询流程实例状态](/images/查询流程实例状态.png)

#### 4.5.6 查询所有任务

![查询所有任务](/images/查询所有任务.png)

#### 4.5.7 完成任务

![完成任务](/images/完成任务.png)

#### 4.5.8 查询流程定义

![查询流程定义](/images/查询流程定义.png)

#### 4.5.9 删除流程定义

![删除流程定义](/images/删除流程定义.png)

#### 4.5.10 查询历史流程实例

![查询历史流程实例](/images/查询历史流程实例.png)

#### 4.5.11 查询历史流程实例任务信息

![查询历史流程实例任务信息](/images/查询历史流程实例任务信息.png)

### 4.6 流程变量

#### 4.6.1 设置参数

![设置参数](/images/设置参数.png)

## 5. Spring 整合 activiti web 设计器
### 5.1 创建 spring boot web 项目
新建一个 spring boot web 项目，主要依赖如下组件:

* spring-boot-starter-web
* spring-boot-starter-thymeleaf
* mybatis-spring-boot-starter
* mysql-connector-java
* activiti-spring-boot-starter-basic
* activiti-json-converter

### 5.2 官网下载 activiti-5.22.0

### 5.3 导入静态资源
1. 解压 `activiti-5.22.0/wars/activiti-explorer.war`，将`diagram-viewer`,`editor-app` copy 至`resources/static`目录下。
2. 将`modeler.html` copy 至`resources/templates`目录下，并修改内容使用 thymeleaf 模版。
3. 将`stencilset.json` copy 至`resources`目录下
4. 修改`editor-app/app-cfg.js`的`contextRoot`为项目的`context-path`,本项目为``

![activiti静态资源](/images/activiti静态资源.png)

### 5.4 导入类

解压`libs/activiti-modeler-5.22.0-sources.jar`，将`/org/activiti/rest/editor`目录下三个类copy至项目`src/main/java`下，并修改包名

![activiti-editor类](/images/activiti-editor类.png)

### 5.5 实现流程查询、展示
1. `resources/templates`下新建`index`页面，并实现展示流程列表及新建流程按钮
2. 新建`ActivitiController`, 并实现流程增删改查
### 5.6 新建流程及设计
点击首页`新建工作流`最终会跳转 activiti web 工作流设计页面

[GitHub: Activiti Demo]()

## 问题记录
### 1 启动异常
#### 1.1  processes cannot be resolved to URL
nested exception is java.io.FileNotFoundException: class path resource [processes/] cannot be resolved to URL because it does not exist。两种处理方法:

1. 在resource目录下添加processes文件夹，并且文件夹不能为空
2. 在application.properties下配置忽略检查

```
spring:
    activiti:
        check-process-definitions: false
```

#### 1.2 Could not find class [org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration] 错误

启动排除指定类

```
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
```

#### 1.3 启动后不创建表

datasource.url 增加参数设置 

```
nullCatalogMeansCurrent=true
```

