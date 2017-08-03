package sinosoftsh.consumer.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by xujingfeng on 2017/8/3.
 */
@FeignClient(value = "calculate",path = "/api")
public interface CalculateApi {

    @PostMapping(path = "/add")
    Integer add(@RequestParam("a") Integer a,@RequestParam("b") Integer b);

    @PostMapping(path = "/subtract")
    Integer subtract(@RequestParam("a") Integer a,@RequestParam("b") Integer b);

}
