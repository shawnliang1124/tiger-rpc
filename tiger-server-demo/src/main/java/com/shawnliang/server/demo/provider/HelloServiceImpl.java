package com.shawnliang.server.demo.provider;

import com.shawnliang.tiger.server.annonations.TigerRpcService;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@TigerRpcService(interfaceType = HelloService.class, version = "1.0")
public class HelloServiceImpl implements HelloService{

    public String sayHello(String word) {
        return "hello " + word;
    }
}
