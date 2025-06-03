# 银行管理系统

本项目是一个基于 Spring Boot 的简易银行管理系统，支持多银行、多账户、转账、充值、取款、利息计算、管理员管理等功能，适配 Docker 部署。
本项目仅仅是本人JAVA大作业项目，所以写的可能并不规范，部分代码例如UI部分以及web部分（ Spring Boot 框架）不太熟悉，使用了AI辅助。

在线体验[点击](https://bank.enoch.host/)
ps：可能需要国际网络

---

## 目录

- [功能简介](#功能简介)
- [快速开始](#快速开始)
- [本地运行](#本地运行)
- [Docker 部署](#docker-部署)
- [主要功能说明](#主要功能说明)
- [系统结构](#系统结构)
- [常见问题](#常见问题)

---

## 功能简介

- 用户注册/登录
- 多银行、多账户管理
- 账户转账、充值、取款
- 账户利息自动计算
- 管理员后台（银行、用户、账户管理）
- 数据持久化（使用JSON 轻量化存储）
- 支持 Docker 部署(未上传dockerhub，需要手动部署)

---

## 快速开始

### 1. 本地运行

1. **环境要求**  
   - JDK 17  
   - Maven 3.6+  
   - （推荐）IntelliJ IDEA

2. **构建项目**
   ```sh
   mvn clean package
   ```

3. **运行**
   ```sh
   java -jar target/banking-1.0-SNAPSHOT.jar
   ```
   默认端口：8080

4. **访问系统**  
   浏览器打开 [http://localhost:8080](http://localhost:8080)

---

### 2. Docker 部署

1. **构建 jar 包**  
   参考上面“本地运行”第2步。

2. **构建 Docker 镜像**
   ```sh
   docker build -t banking-app:latest .
   ```

3. **运行容器**
   ```sh
   docker run -d -p 8080:8080 --name banking-app banking-app:latest
   ```

4. **数据持久化（可选）**  
   挂载 data.json 到主机：
   ```sh
   docker run -d -p 8080:8080 -v /your/host/path/data.json:/app/data.json --name banking-app banking-app:latest
   ```

---

## 主要功能说明

- **注册/登录**：支持多用户注册，密码加密存储。
- **账户管理**：每个用户可在不同银行下拥有多个账户。
- **转账/充值/取款**：支持账户间转账、充值、取款（余额实时变动）。
- **利息计算**：主页和账户详情页均可查看一年利息（余额 × 利率）。
- **管理员后台**：可管理银行、用户、账户，支持重置密码、修改余额/利率等。默认admin/admin
- **安全**：基于 Spring Security，支持角色权限控制。

---

## 系统结构

- `controller/`：Web 控制器，处理页面和接口请求
- `service/`：业务逻辑与数据持久化
- `model/`：实体类（Bank、Customer、Account等）
- `resources/templates/`：前端页面（Thymeleaf）
- `data.json`：数据文件（自动生成）

---

## 常见问题

- **JDK 版本不符**：请确保本地 JDK 和 `JAVA_HOME` 都为 17。
- **端口冲突**：如 8080 被占用，可在 `application.properties` 配置端口。
- **数据丢失**：如需持久化数据，建议挂载 `data.json` 到主机目录。

---

## 其他

- 如有问题欢迎提 issue 或联系开发者。
- [个人博客链接](https://enoch.host)

---