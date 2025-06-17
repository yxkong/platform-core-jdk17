[TOC]
## 系统演示
> https://platform.5ycode.com/
> 
以下用户的密码为111111
- uiuser  ui用户
- tduser  技术负责人
- pmuser  产品经理
- pmouser 项目经理
- qauser 测试人员
- feduser 前端开发
- beduser 后端开发

## 架构
[架构指南](./docs/architecture/architecture.md)

## 开发约定
### 提交约定
```markdown
feat:     增加新功能
fix:      修复bug
merge:    分支合并
release:  发布版本
docs:     只改动了文档相关的内容
style:    代码格式的更改，例如去掉空格、改变缩进、增删分号
build:    构造工具的或者外部依赖的改动，例如maven
refactor: 代码重构时使用
revert:   版本回退
test:     添加测试或者修改现有测试
perf:     性能优化提高性能的改动
ci:       与CI（持续集成服务）有关的改动
chore:    不修改src或者test的其余修改，例如构建过程或辅助工具的变动
```
### 模块约定
在新起的项目中，建议使用如下的parent，会引入所有的platform-core的包
```xml
    <parent>
        <groupId>com.github.platform.core</groupId>
        <artifactId>core</artifactId>
        <version>对应的版本</version>
    </parent>
```
### 代码包结构
#### 统一的包结构
当有数据库实体引入
- mapper前缀使用 `com.github.platform.core.persistence.mapper`
- entity前缀使用 `com.github.platform.core.persistence.entity`


方便在集成的时候扫描到mapper

例如：
- com.github.platform.core.persistence.mapper.file
- com.github.platform.core.persistence.mapper.sms
- com.github.platform.core.persistence.mapper.schedule

```java
@Configuration
@MapperScan(basePackages = {
        "com.github.platform.core.persistence.mapper",
        "com.自己扩展的包.infra.persistence.mapper"
        }, sqlSessionFactoryRef = "sqlSessionFactorySys")

```

## 开发使用
### 环境
- jdk
  - 该版本最低支持jdk17,jdk8 版本已经不维护了
- maven 
  - 目前使用3.9.8 （建议3.6以上）
- mysql 建议5.7及以上
    - 需要设置大小写不敏感 [mysqld] 下设置 `lower_case_table_names=1`
    - 需要支持非`only_full_group_by` 模式 ，设置`sql_mode = "STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"`
- nacos 2.1
- redis
    - redis 5.0以上，可以用其中的stream消息
- kafka 可选
  - 设置服务器上单条消息的最大字节数 message.max.bytes=10485760  # 10MB
  - 设置生产者可以发送的最大消息大小 max.request.size=10485760  # 10MB
  - 设置消费者每次拉取消息的最大字节数 fetch.max.bytes=10485760  # 10MB

### 使用
- 先对platform-core进行 mvn clean install
- 对demo进行 mvn clean install
- 项目使用nacos，开发环境配置都已经配置，可以直接在本地启动gateway 和sys这两个项目;
### 开发规范
#### 表名规范
> t_分类_模块_业务

| 分类 | 模块  | 英文全称 | 缩写  | 示例 |
| ---- | ---- | ---- | ---- | ---- |
| 系统(t开头) |  系统管理   |  system    |  sys    | t_sys_user

#### 表名主键映射关系
| 表名 | id  | 外部引用 |
| ---- | ---- | ---- |
| t_sys_role |  id   |  role_id    | 
| t_sys_menu |  id   |  menu_id    | 
| t_sys_dept |  id   |  dept_id    | 
| t_sys_config |  id   |  cfg_id    | 
| t_sys_dict_data |  id   |  dict_id    | 
#### 字段字典
| 字段          | 类型           | 默认值   |描述  |
|-------------|--------------|-------| ---- |
| id          | bigint       | 自增    |所有表主键都叫id
| mobile      | varchar      | 11    | 手机号码
| cert_id     | varchar      | 18    | 身份证号
| status      | tinyint      | 4     | 状态，1启用/0停用(取值字典)
| deleted     | tinyint      | 4     | 删除状态，1启用/0停用(取值字典)
| sort        | int          | 8     | 排序字段
| tenant_id   | int          | null  | 租户id
| create_by   | varchar(32)  | 空字符串  | 创建人(数据归属人)，存储登录名 
| create_time | timestamp    | 当前时间  | 创建时间
| update_by   | varchar(32)  | 空字符串  | 更新人，存储登录名
| update_time | timestamp    | 更新时间戳 | 更新时间
| remark      | varchar(200) | 空字符串  |备注,只做简单的备注，不一定用

### 缓存
- 缓存操作统一以`ICacheService`，已针对缓存操作做了切面
    - 禁止使用redis 客户端 (排除redisson)
- config
    - 缓存为redis 的一个hash，key为：common:config:
    - hash中 kv，k是config的config_key，v为ConfigEntity
- dict
    - 一个分类一个redis的hash，key为：common:dict:type  type 为具体的分类
    - hash中的kv, 其中k为dict_key， v为DictEntity

以上两个统一封装到了platform-core-cache里，谁需要使用，谁引入。
- sys 模块进行初始化、新增、更新和删除
- 其他模块直接使用

### 数据权限
- 注解声明: `@DataScope(tableAlias = "t"),位置-基础设施层-持久层mapper需要数据权限的方法上`
- 数据权限sql嵌入: `查询sql尾部加上${params.dataScope}`
- 注意点:`mapper的方法第一个参数需要是BaseEntity的子类`

### 前后端交互
正常返回  ResultBean
```json
{ 
  "message": "消息提示",
  "status": "返回状态码",
  "timestamp": "时间戳",
  "data": "业务数据"
}
```
分页数据返回实体
ResultBean嵌套PageBean
```json
{
  "message": "消息提示",
  "status": "返回状态码",
  "timestamp": "时间戳",
  "data": {
    "pageNum": 1,
    "totalSize": 101,
    "pages": 11,
    "pageSize": 10,
    "data": "列表数据"
  }
}
```

## 待完善功能

| 模块      | 功能                        | 认领人    | 备注         |
|---------|---------------------------|--------|------------|
| 文件上传    | 页面管理                      | yxkong | 已完成        |
| 文件上传    | 集成腾讯云和七牛的上传               | yxkong | 阿里云已完成，    |
| 系统管理    | 完善在线和踢出功能                 | yxkong | 已完成        |      
| 登录      | 钉钉三方登录                    | yxkong | 已完成        |
| 系统管理    | 同步钉钉部门，同步钉钉用户             | yxkong | 已完成接口      |
| 短信功能    | 集成阿里云、腾讯云的短信              |        |            |
| 消息中心    | 短信重构为消息中心                 |        |            |
| 灰度发布    | 通过redis订阅发到各在线节点          | yxkong | 已完成        |
| 灰度发布    | 熔断限流发布                    |        |            |
| 流程管理    | 标准工作流                     | yxkong | 已完成        |
| 模块管理    | 首页改造，以及其他系统集成）            |        |            |
| 流程管理    | 钉钉权限审批                    |        |            |
| 菜单      | 增加系统模块                    | yxkong | 已完成        |
| 缓存管理    | 统一放入zset，分页               | yxkong |            |
| 系统监控    | 系统日志中无请求头和body的问题         | yxkong | 已完成        |
| redis监控 | 添加redis的简单监控，以及根据key前缀scan扫描key |        | 已完成        |
| 公共      | 增加指定账户演示模式                |        |
| 公共      | 同一个用户可多处登录                | yxkong | 通过配置可以     |
| 工作流     | 工作流支持部门人员，部门负责人           |        |            |          |     
| 项目管理    | 需求支持内容+链接                 |        |            |   已完成       |   
| 工作流     | 表单支持附件                    |        |            |  已完成        |
| 工作流     | 流程回退                      |        |            |      |
| 低代码     | 低代码平台的设计                  |        |            |      |
| 项目管理    | 需求节点评分                    |        |            |      |
| 项目管理    | 增加bug登记                   |        |            |   已完成   |
| 项目管理    | 自动生成开发任务                  | yxkong | 已完成，通过hook |      |
| 项目管理    | 增加评论备注功能                  |        |            |  已完成    |
| 系统管理    | 配置抽到数据库(ldap,钉钉，附件等)      |        |            |   已完成   |