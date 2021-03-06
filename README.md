# jtool-apiclient    
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)[
![Build Status](https://travis-ci.org/JavaServerGroup/jtool-apiclient.svg?branch=master)](https://travis-ci.org/JavaServerGroup/jtool-apiclient)
[![Coverage Status](https://coveralls.io/repos/github/JavaServerGroup/jtool-apiclient/badge.svg?branch=master)](https://coveralls.io/github/JavaServerGroup/jtool-apiclient?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8ad8c94ccc7d4b3280c8ce9e6ccf8c5b)](https://www.codacy.com/app/jiale-chan/jtool-apiclient?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=JavaServerGroup/jtool-apiclient&amp;utm_campaign=Badge_Grade)   
   
简易基于http的api请求客户端。直接使用HttpURLConnection，支持keepalives。支持map和pojo作为请求参数。支持设置header。默认支持jtool的_logId日志系统。

# Quick start
1.添加jtool的github的repository
```xml
<repositories>
    <repository>
        <id>jtool-mvn-repository</id>
        <url>https://raw.github.com/JavaServerGroup/jtool-mvn-repository/master/releases</url>
    </repository>
</repositories>
```
2.添加依赖
```xml
<dependency>
    <groupId>com.jtool</groupId>
    <artifactId>jtool-apiclient</artifactId>
    <version>0.0.18</version>
</dependency>
```
2.添加import
```xml
import static com.jtool.apiclient.ApiClient.Api;
```
3.使用
```java
Api().get("http://www.example.org");
or
Api().post("http://www.example.org");
```
## 方法介绍
### header方法。当发送的请求需要带上header:
```java
Map<String, String> header = new HashMap<String, String>();
header.put("Authorization", "Basic xxx");
Api().header(header).post("http://www.example.org");
```
### param方法。当请求需要带上参数:   
带pojo参数：
```java
public class User{
  private String name;
  ...
}
...
User user = new User();
user.setName("Andy");
Api().param(user).post("http://www.example.org");
```
带Map参数（推荐：Map&lt;String, Object&gt;）：
```java
//普通参数
Map<String, Object> user = new HashMap<>();
user.put("user", "Andy");
Api().param(user).post("http://www.example.org");
```
```java
//发文件
Map<String, Object> uploadImgParam = new HashMap<>();
uploadImgParam.put("img", new File("~/photo.jpg"));
uploadImgParam.put("fileName", "myphoto");
Api().param(uploadImgParam).post("http:/www.example.org");
```
# REST post
可以调用restPost直接发送rest风格的post请求
```java
User user = new User();
user.setName("Andy");

String result = Api().param(people).restPost("http://www.xxx.com/restpost");
```
# 异常：StatusCodeNot200Exception
当请求返回的status code返回的码不在大于等于200，小于400的时候，会抛出一个自定义的运行时异常:StatusCodeNot200Exception
