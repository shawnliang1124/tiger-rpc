package com.shawnliang.client.demo.controller;

import com.shawnliang.client.demo.consumer.IDemoConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    @Qualifier("demoConsumer")
    private IDemoConsumer demoConsumer;

    @Autowired
    @Qualifier("demoSyncConsumer")
    private IDemoConsumer iDemoConsumer2;

    @GetMapping("/test")
    public String test(String word) {
        return demoConsumer.doConsume(word);
    }

    @GetMapping("/sync-test")
    public String test2(String word) {
        return iDemoConsumer2.doConsume(word);
    }

}
