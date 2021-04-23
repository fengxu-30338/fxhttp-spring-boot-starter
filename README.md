## 1.添加依赖

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.fengxu-30338</groupId>
    <artifactId>fxhttp-spring-boot-starter</artifactId>
    <version>0.2.2.1</version>
</dependency>
```





## 2.开始使用

该版本为 [fengxu-30338/fengxu-http: 基于安卓的Http请求库 (github.com)](https://github.com/fengxu-30338/fengxu-http) 的springboot整合版，将该starter引入到springboot的项目中，就可以简单的使用该库了！



### 2.1 快速开始



> 1.创建请求接口

```java
public interface UserHttp {

    @FxHttp(value = "/api/pub/notice",patterMore = true)
    String getNotice();

}
```



> 2.yml文件配置

```yaml
fxhttp:
  http-proxy:
    userHttp: # 配置接口代理注册到容器中的bean名称
      class-name: com.test.http.UserHttp # 配置刚创建的接口的全类名
      base-url: http://192.168.0.102  # 配置url基地址
      start-log: true  # 是否开启日志(默认false)
    promHttp:
      class-name: com.test.http.PromHttp # 配置刚创建的接口的全类名
      base-url: http://192.168.0.103  # 配置url基地址  
```



**注： 在0.2.2.1版本以后(包括0.2.2.1)可以使用注解的方式配置接口，只需要在yml文件中配置基本包即可！**

```java
@FxHttpInterface(baseUrl = "http://8.131.71.174",startLog = true)
public interface UserHttp {

    @FxHttp(value = "/api/pub/notice",patterMore = true)
    String getNotice();

}

// 在yml文件中，只需要配置这些需要被fxhttp动态代理接口的基本包即可,如:
fxhttp:
  base-package: com.test.http # 配置接口所在的基本包
```

虽然还可以使用yml的方式配置接口属性，但是还是建议使用基本包+@FxHttpInterface的方式配置!





> 3.自动注入

```java
@RestController
public class TestController {

    @Autowired
    @Lazy
    @SuppressWarnings("all")
    private UserHttp userHttp;

    @RequestMapping("/")
    public Object test(){
        return userHttp.getNotice();
    }
}
```

注意：因为该代理是动态生成的，所以在注册bean时，spring并不能准确识别到依赖，所以这里一定要使用@Lazy,而@SuppressWarnings("all")是因为spring容器扫描不到该类，idea会爆红，加上这个就不会爆红了！

这里的变量名不用和yml文件中配置的一样！



注： 在0.2.2.1版本以后(包括0.2.2.1)使用@FxHttpInterface注解配置的接口，不需要使用@Lazy和@SuppressWarnings("all")了只需要@Autowired即可完成注入！



### 2.2 配置拦截器

指定一个类用@FxBlocke标注，其中target指向yml中配置的bean的名称，也就是指定该类拦截器作用的对象！

```java
@FxBlocker(target = "userHttp")
public class UserHttpInterceptor {

    @FxBlockerMethod("/api")
    public void blocker1(FxHttpInterceptor interceptor){
        interceptor.addHeader("token","1234451xsaf");
    }

    @FxBlockerMethod(value="/api2",name="promHttp")
    public void blocker2(FxHttpInterceptor interceptor){
        interceptor.addHeader("token","1234451xsaf");
    }

}
```

@FxBlockerMethod用于指定拦截器匹配的正则，其中name还可以指定该方法作用的http接口代理，若同时在接口和方法注解上都指定了那么方法的优先级较高！若都没有指定则报错！

注：方法的参数一定要有一个FxHttpInterceptor类型的参数，也同样可以指定其他类型的参数，其会自动在ioc容器中寻找并注入，若未找到则报错！



## 更新日志



### v0.2.2.1更新日志



增加使用@FxHttpInterface注解 + yml基本包配置的方式，配置要让fxhttp代理的接口！

**用法**

```java
import com.fengxu.fxhttp.annotation.FxHttpInterface;
import com.fengxu.http.FxHttp;

@FxHttpInterface(baseUrl = "http://8.131.71.174",startLog = true)
public interface UserHttp {

    @FxHttp(value = "/api/pub/notice",patterMore = true)
    String getNotice();

}
```



**yml配置**

```yaml
fxhttp:
  base-package: com.test.http
```



**@FxHttpInterface属性**

| 属性名   | 含义                                               |
| -------- | -------------------------------------------------- |
| value    | 注入到容器中的bean名称，若没有配置则默认首字母小写 |
| baseUrl  | 该接口对应的基础url                                |
| startLog | 是否开启日志: false                                |

