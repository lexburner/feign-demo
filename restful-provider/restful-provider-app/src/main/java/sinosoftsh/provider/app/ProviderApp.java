package sinosoftsh.provider.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by xujingfeng on 2017/8/3.
 */
@SpringBootApplication
@ComponentScan(basePackages = "sinosoftsh.provider.app")
public class ProviderApp {

    public static void main(String []args){
        SpringApplication.run(ProviderApp.class,args);
    }

}
