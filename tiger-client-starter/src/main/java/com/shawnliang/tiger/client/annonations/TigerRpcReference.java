package com.shawnliang.tiger.client.annonations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Autowired
public @interface TigerRpcReference {

    /**
     * 版本号
     * @return
     */
    String version() default "1.0";

    /***
     * 调用方式 默认是同步
     * @return
     */
    String invoke() default "sync";

    /**
     * 长连接池的个数
     * @return
     */
    int connectNum() default 1;

}
