package cn.reve.framework.annotation;

/**
 * 简要描述
 * 删除缓存
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/4 15:22
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME) // 什么时期有效    此处为运行期有效
@Target(ElementType.METHOD) // 在方法中使用
public @interface CacheDel {
    /**
     * 具体操作得缓存前缀
     */
    String redisPrefix();

    /**
     * 具体的缓存名称，为空则删除 redisPrefix 下的所有缓存
     */
    String fieldName() default "";
}
