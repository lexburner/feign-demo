package sinosoftsh.consumer.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

/**
 * Created by xujingfeng on 2017/8/3.
 */
@EnableFeignClients(basePackages = {"sinosoftsh.provider.api"})
@SpringBootApplication
public class ConsumerApp {

    public static void main(String []args){
        SpringApplication.run(ConsumerApp.class,args);
    }

}
