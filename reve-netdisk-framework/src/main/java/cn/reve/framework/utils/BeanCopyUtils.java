package cn.reve.framework.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 简要描述
 * <p>
 * bean转换VO的封装
 * 定义一个泛型方法的语法如下
 * 方法作用域修饰符<泛型类型> 返回值类型 方法名（泛型）{
 * <p>
 * }
 * 定义泛型方法时的<>尖括号里面的泛型类型可以是泛型列表，中间用“，”号隔开，这一点与泛型接口和泛型类雷同
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/3 14:49
 */
public class BeanCopyUtils {

    private BeanCopyUtils() {
    }

    /**
     * @param source
     * @param clazz
     * @return T
     * @date 2023/4/3 14:55
     * @description 希望通过copyBean方法去进行类的复制，并且返回类型应该是传入的需要复制的target，
     */
    public static <T> T copyBean(Object source, Class<T> clazz) {
        if(Objects.isNull(source)){
            return null;
        }
        T target=null;
        try {
            //实现bean同属性拷贝
            target = clazz.newInstance();
            BeanUtils.copyProperties(source,target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return target;
    }

    /**
     * @param list
     * @param clazz
     * @return T
     * @date 2023/4/3 15:01
     * @description 处理查询出的集合，进行遍历拷贝
     */
    public static <T,V> List<V> copyBeanList(List<T> list,Class<V> clazz){
        return list.stream().map(m -> copyBean(m,clazz)).collect(Collectors.toList());
    }
}
