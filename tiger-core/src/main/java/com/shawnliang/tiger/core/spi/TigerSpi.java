package com.shawnliang.tiger.core.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description :  spi注解，表示该接口/抽象类是可拓展的 .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TigerSpi {

    /**
     * 指定 自定义拓展文件的名称，默认就是接口/抽象类的全路径
     * @return
     */
    String fileName() default "";

    /**
     * 是否是单例，默认是true
     * @return
     */
    boolean isSingleton() default true;

}
