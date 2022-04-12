package com.shawnliang.tiger.client.annonations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description :   调用rpc的形式.
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CallRpc {

    String method() default "sync";

}
