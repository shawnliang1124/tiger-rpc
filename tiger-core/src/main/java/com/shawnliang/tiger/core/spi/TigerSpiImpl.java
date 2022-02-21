package com.shawnliang.tiger.core.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TigerSpiImpl {

    /**
     * 实现类的别称
     * @return
     */
    String value() default "";

    /**
     * 加载的顺序，数字越小，越先加载
     * @return
     */
    int order() default 0;

}
