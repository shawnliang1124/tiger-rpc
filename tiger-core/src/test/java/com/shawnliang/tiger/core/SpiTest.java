package com.shawnliang.tiger.core;

import com.shawnliang.tiger.core.register.RegistryService;
import com.shawnliang.tiger.core.spi.TigerSpiClass;
import com.shawnliang.tiger.core.spi.TigerSpiClassLoaderFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/22
 */
public class SpiTest {


    @Test
    public void ZookeeperSpi(){
        TigerSpiClass<? extends RegistryService> zookeeperRegister = TigerSpiClassLoaderFactory
                .getSpiLoader(RegistryService.class)
                .getSpiClass("zookeeper");
        RegistryService instance = zookeeperRegister.getInstance(new Class[]{String.class}, new Object[]{"127.0.0.1"});
        Assert.assertNotNull(instance);

    }

}
