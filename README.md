## tiger-rpc是什么? 
属于shwanliang学习完netty的一个小练手项目，很多人在学习完netty之后，总想写点什么来实战，包括简易版im, 手写rpc。  
简易版im我也写了，但是总觉得偏理论，现实工作中，因为我个人是偏业务的后端开发，平时接触更多的是各种业务代码，自然也少不了rpc框架。  

关于rpc框架，自己工作的三年多里面，feign, dubbo, sofa-rpc等都用过，之前也尝试过去阅读他们的源码，总是一会就忘了，到底他们涉及的巧妙的地方在哪里，值得我们学习的地方要在哪里？   

因此通过手写一个rpc框架，再去对比优秀的开源框架，就能从中发现自己手写框架的不足，得到更多的思考，从而给工作中带来更多的启发。  

## tiger-rpc的技术选择   
- 网络通信：netty
- 注册中心：zookeeper
- 项目技术栈：springboot2.0, jdk8 
- 技术细节：curator(zookeeper客户端)，jdk动态代理，反射  

## 调用流程
直接使用代码进行说明： 
- client 端：
```java
    @TigerRpcReference(version = "1.0")
    private HelloService helloService;

    public String doConsume(String word) {
        return helloService.sayHello(word);
    }

``` 
client端直接使用一个注解，@TigerRpcReference，tiger-rpc底层，从注册中心拿到server端的ip + 端口，再通过netty进行网络通信，真正调用运行在远程服务器的server端代码  
业务开发无需关注底层网络细节，只需要做他们的业务开发就OK了  

- server端：
```java
@TigerRpcService(interfaceType = HelloService.class, version = "1.0")
public class HelloServiceImpl implements HelloService{

    public String sayHello(String word) {
        return "hello " + word;
    }
}

``` 
使用@TigerRpcService注解进行注册，将接口的信息，保存到注册中心zookeeper上去，提供给client端，让其进行相关的调用

## 整体实现方式  

## todo
- 实现SPI机制，支持多注册中心，多协议化方式
- client本地存储server的注册表，不用每次都到zk去拉取
- netty的交互方式是否有优化的空间
- 使用自定义协议代替protostuff



