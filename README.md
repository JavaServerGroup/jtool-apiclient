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
```
2.添加配置到applicationContext.xml
```xml
<bean id="apiPost" class="com.jtool.apiclient.ApiPost" />
<bean id="apiGet" class="com.jtool.apiclient.ApiGet" />
```
3.注入并使用
```java
@Resource
private ApiGet apiGet;
...
String html = apiGet.sent("http://www.example.org");
```
#sent的用法
不带参数，直接请求：
```java
sent("http://www.example.org");
```
带pojo参数：
```java
public class User{
  private String name;
  ...
}
...
User user = new User();
user.setName("Andy");
sent("http://www.example.org", user);
```
带Map参数（推荐：Map<String, Object>）：
```java
//普通参数
Map<String, Object> user = new HashMap<>();
user.put("user", "Andy");
sent("http:/www.example.org", user);
```
```java
//发文件
Map<String, Object> uploadImgParam = new HashMap<>();
uploadImgParam.put("img", new File("~/photo.jpg"));
sent("http:/www.example.org", uploadImgParam);
```
#关于jtool的_logId的说明
sent方法默认会添加一个_logId参数到你的请求，如果要禁止这个功能，可以设置needLog为false:
```java
sent("http://www.example.org", false);
or
sent("http:/www.example.org", user, false);
```
_logId参数来自于ThreadLocal,当需要别的线程发送又希望请求带上_logId, 可以手动带上_logId:
```java
String _logId = "a_random_logId";
sent("http://www.example.org", _logId);
or
sent("http:/www.example.org", user, _logId);
```
