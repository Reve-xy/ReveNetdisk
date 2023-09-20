package cn.reve.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 简要描述
 * redis-aop
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/22 18:01
 */
@Retention(RetentionPolicy.RUNTIME) // 什么时期有效    此处为运行期有效
@Target(ElementType.METHOD) // 在方法中使用
public @interface CacheFind {

    // 用户必须指定 key
    String key();

    // 设定超时时间    默认值为-1，即不需要超时
    int expire() default -1;
}
