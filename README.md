# jtool-apiclient
简易基于http的api请求客户端   

#Quick start
1.添加jtool的github的repository
```xml
<repositories>
    <repository>
        <id>jtool-mvn-repository</id>
        <url>https://raw.github.com/JavaServerGroup/jtool-mvn-repository/master/releases</url>
    </repository>
    <repository>
        <id>jtool-mvn-snapshots</id>
        <url>https://raw.github.com/JavaServerGroup/jtool-mvn-snapshots/master/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
2.添加依赖
```xml
<dependency>
    <groupId>com.jtool</groupId>
    <artifactId>jtool-apiclient</artifactId>
    <version>0.0.4</version>
</dependency>
```
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
#方法介绍
header方法。当发送的请求需要带上header:
```java
Map<String, String> header = new HashMap<String, String>();
header.put("Authorization", "Basic xxx");
Api().header(header).post("http://www.example.org");
```
param()方法。当请求需要单上参数:   
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
带Map参数（推荐：Map<String, Object>）：
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
#关于jtool的_logId的说明
get/post方法默认会添加一个_logId参数到你的请求，如果要禁止这个功能，可以调用needLog(false)默认为true:
```java
Api().needLog(false).get("http://www.example.org");
```
_logId参数来自于ThreadLocal,当需要别的线程发送又希望请求带上_logId, 可以手动设置_logId:
```java
Api().logId("a_random_logId").get("http://www.example.org");
```