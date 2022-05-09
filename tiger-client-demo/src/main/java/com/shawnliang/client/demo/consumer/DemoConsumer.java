package com.shawnliang.client.demo.consumer;

import com.shawnliang.server.demo.provider.HelloService;
import com.shawnliang.tiger.client.annonations.TigerRpcReference;
import org.springframework.stereotype.Service;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Service
public class DemoConsumer implements IDemoConsumer {

    @TigerRpcReference(version = "1.0", invoke = "async")
    private HelloService helloService;

    public String doConsume(String word) {
        return helloService.sayHello(word);
    }
}
