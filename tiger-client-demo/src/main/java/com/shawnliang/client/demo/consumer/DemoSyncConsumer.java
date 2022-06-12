package com.shawnliang.client.demo.consumer;

import com.shawnliang.server.demo.provider.HelloService;
import com.shawnliang.tiger.client.annonations.TigerRpcReference;
import org.springframework.stereotype.Service;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/6/12
 */
@Service
public class DemoSyncConsumer implements IDemoConsumer{

    @TigerRpcReference(version = "1.0", invoke = "sync")
    private HelloService helloService;

    public String doConsume(String word) {
        return helloService.sayHello(word);
    }
}
