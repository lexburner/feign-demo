# feign-demo
对于Spring Cloud Feign入门示例的一点思考

## Spring Cloud Feign

Spring Cloud Feign是一套基于Netflix Feign实现的声明式服务调用客户端。它使得编写Web服务客户端变得更加简单。我们只需要通过创建接口并用注解来配置它既可完成对Web服务接口的绑定。它具备可插拔的注解支持，包括Feign注解、JAX-RS注解。它也支持可插拔的编码器和解码器。Spring Cloud Feign还扩展了对Spring MVC注解的支持，同时还整合了Ribbon和Eureka来提供均衡负载的HTTP客户端实现。

分布式应用早在十几年前就开始出现，各自的应用运行在各自的tomcat，jboss一类的容器中，他们之间的相互调用变成了一种远程调用，而实现远程调用的方式很多。按照协议划分，可以有RPC，Webservice，http。不同的框架也对他们有了各自的实现，如dubbo(x)，motan就都是RPC框架，本文所要讲解的Feign便可以理解为一种http框架，用于分布式服务之间通过Http进行接口交互。说他是框架，有点过了，可以理解为一个http工具，只不过在spring cloud全家桶的体系中，它比httpclient，okhttp，retrofit这些http工具都要强大的多。

## 入门

先用一个简单的例子，看看如何在项目中使用Feign。示例项目使用maven多module构建，采用springcloud的Dalston.SR1版本

	<dependencyManagement>
	     <dependencies>
	         <dependency>
	             <groupId>org.springframework.cloud</groupId>
	             <artifactId>spring-cloud-dependencies</artifactId>
	             <version>Dalston.SR1</version>
	             <type>pom</type>
	             <scope>import</scope>
	         </dependency>
	     </dependencies>
	 </dependencyManagement>

### 服务提供方
在本例子中，使用两个应用模块，展示分布式应用中如何进行接口交互。`restful-provider`担任服务提供方，`restful-consumer`担任服务消费者。


-  `restful-provider`新建一个module`restful-provider-app`,模块中只需要写一个`CalculateController.java`即可

		@RestController
		@RequestMapping("/api")
		public class CalculateController {

		    @PostMapping("/add")
		    public Integer add(@RequestParam Integer a,@RequestParam Integer b){
		        return a+b;
		    }
		
		    @PostMapping("/subtract")
		    public Integer subtract(@RequestParam Integer a,@RequestParam Integer b){
		        return a-b;
		    }

		}
- 配置文件application.yml：

		server:
		  port: 7070

 一个服务端就写好了，提供两个计算服务的接口，可以通过http访问
 
### 服务消费方

- 使用Feign编写消费方，在restful-consumer项目中，我们将接口的定义和消费者应用分成两个module，`restful-consumer-api-definition`和`restful-consumer-app`。

- 在接口定义模块中，只有一个Feign接口：

		@FeignClient(value = "calculate",path = "/api")
		public interface CalculateApi {
		
		    @PostMapping(path = "/add")
		    Integer add(@RequestParam("a") Integer a,@RequestParam("b") Integer b);
		
		    @PostMapping(path = "/subtract")
		    Integer subtract(@RequestParam("a") Integer a,@RequestParam("b") Integer b);
		
		}
tip：@RequestParam中的参数值不能省略，否则会出现错误

-  `restful-consumer-app`依赖上面的`restful-consumer-api-definition`模块，并且启用Feign代理，自动生成一个远程调用。
启动类配置：

		@EnableFeignClients(basePackages = {"sinosoftsh.consumer.api"})
		@SpringBootApplication
		public class ConsumerApp {
		
		    public static void main(String []args){
		        SpringApplication.run(ConsumerApp.class,args);
		    }
		
		}
使用`@EnableFeignClients(basePackages = {"sinosoftsh.consumer.api"})`扫描接口类所在的包，spring的容器中才会有代理实现类。

- 不要忘记配置消费者的相关属性，在application.yml中

		server:
		  port: 7080
		
		ribbon:
		  eureka:
		   enabled: false
		
		calculate:
		  ribbon:
		    listOfServers: localhost:7070
		
		logging:
		  level:
		    org.apache.http: trace
	    
	在CalculateApi 接口的定义中，我们使用了一个calculate作为服务名称，必须要在配置文件中配置calculate所在的ip地址才行，由于本文只是作为一个示例，所以没有使用注册中心，在配置中禁用了eureka。最后一行的日志配置，可以发现其实Feign内部其实使用的是现成的http工具：httpclient，okhttp3，可以通过配置替换实现

- 整体的项目结构如下：
<center>
![这里写图片描述](http://img.blog.csdn.net/20170803170857775?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzgxNTU0Ng==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
</center>
<center>图一 第一种依赖关系结构</center>

- 再编写一个单元测试类，验证一下Feign是否被正确的配置了

		@RestController
		public class ConsumerController {
		
		    @Autowired
		    CalculateApi calculateApi;
		
		    @RequestMapping("/test")
		    public String test() {
		        Integer result = calculateApi.add(1, 2);
		        System.out.println("the result is " + result);
		        return "success";
		    }
		
		}

## 思考
回顾一下我们入门实例，服务提供方使用的是一个RestController暴露计算服务，服务消费方使用http工具（Feign）进行远程调用，这再清晰不过了，也是符合软件设计的，因为Feign接口的定义是存在于消费方，所以是真正的松耦合。但是习惯了使用rpc共享接口的设计，我们也可以将接口定义在服务提供方，这样做的好处是，服务可能被多个消费者使用，不需要每个消费者都定义一次Feign接口。
<center>
![这里写图片描述](http://img.blog.csdn.net/20170803171756343?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzgxNTU0Ng==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
</center>
<center>图2 第二种依赖关系结构</center>
在`restful-provider`创建一个`restful-provider-api-definition`模块，将`CalculateApi.java`的定义迁移到服务提供方，相应的`restful-provider-app`也可以进行改造：

	@RestController
	@RequestMapping("/api")
	public class CalculateController implements CalculateApi{
	
	//    @PostMapping("/add")
	    @Override
	    public Integer add(@RequestParam Integer a,@RequestParam Integer b){
	        return a+b;
	    }
	
	//    @PostMapping("/subtract")
	    @Override
	    public Integer subtract(@RequestParam Integer a,@RequestParam Integer b){
	        return a-b;
	    }
	
	}

因为接口的定义和服务提供方现在在一个限界上下文中，接口的定义同时也宣告了应该提供什么样的服务，所以直接继承CalculateApi。这里的理解比较绕，现在的设计中，CalculateApi在服务消费者和服务提供者中的定位是不一样的，服务消费者需要在启动类扫描CalculateApi所在的包，生成代理对象，远程调用；而在服务提供方则一定不能扫描CalculateApi所在的包，否则会污染容器中的CalculateApi实现类，要知道，CalculateController 之上有一个`@RestController`注解，意味着已经有一个本地代理实现了，我们也可以在服务提供方注入CalculateApi，便是进行的本地调用了，这符合我们的初衷：我自己的提供的服务，本地当然可以调用。在服务提供方的启动类上要额外注意@ComponentScan，@EnableFeignClients的扫描。

这样，当我们有多个消费者，只需要让他们配置Feign，并且引入服务提供方的接口定义，扫描，即可进行远程调用。有点类似于RPC的共享接口。

## 设计原则
restful设计以语言无关，松耦合的优势著称。在Spring Cloud Feign的相关文档中有这样的描述：
>It is generally not advisable to share an interface between a server and a client. It introduces tight coupling, and also actually doesn’t work with Spring MVC in its current form (method parameter mapping is not inherited).

不建议使用上述改进后的共享接口的方式，并且警告我们，springmvc的注解在Feign接口中的定义和实现类中是不可继承的。关于这点，仁者见仁，智者见智。我们现在项目依旧是采用共享接口的方式，这样可以使得开发变得便捷，多个消费者不需要重复定义。

下面是关于耦合和共享接口的一些讨论：
https://github.com/spring-cloud/spring-cloud-netflix/issues/951
https://github.com/spring-cloud/spring-cloud-netflix/issues/659
https://github.com/spring-cloud/spring-cloud-netflix/issues/646
https://jmnarloch.wordpress.com/2015/08/19/spring-cloud-designing-feign-client/

## 注意事项

- 当接口定义中出现了实体类时，需要使用@RequestBody注解。多个实体类，则需要用一个大的vo对其进行包裹，要时刻记住，Feign接口最终是会转换成一次http请求。

- 接口定义中的注解和实现类中的注解要分别写一次，不能继承。

- Feign调用一般配合eureka等注册中心使用，并且在客户端可以支持Hystrix机制，本文为了讲解共享接口这一设计，所以重心放在了Feign上，实际开发中，这些spring cloud的其他组件通常配套使用。

- 对http深入理解，在使用Feign时可以事半功倍。