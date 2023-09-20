package cn.reve.framework.aop;

import cn.reve.framework.annotation.SystemLog;
import cn.reve.framework.domain.entity.LoginUser;
import cn.reve.framework.utils.SecurityUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/12 22:25
 */
@Aspect
@Order(2)
@Component
@Slf4j
public class LogAspect {

    //注解定义切点



    /**
     * @param joinPoint
     * @param systemLog
     * @return Object
     * @date 2023/5/4 12:56
     * @description 如果需要动态接受注解对象,则在切入点表达式中直接写注解参数名称即可虽然看到的是名称,但是解析时变成了包名.类
     */
    @Around("within(cn.reve.web.controller..*)&&@annotation(systemLog)")
    public Object printLog(ProceedingJoinPoint joinPoint, SystemLog systemLog) throws Throwable {
        Object result;
        try {
            handleBefore(joinPoint,systemLog);
            result = joinPoint.proceed();
            handleAfter(result);
        } finally {
            // 结束后换行
            log.info("=======End=======" + System.lineSeparator());
        }
        return result;
    }

    private void handleBefore(ProceedingJoinPoint joinPoint,SystemLog systemLog) {
        //通过spring包装的request上下文获取当前的request对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        //访问到切入方法的注解内部的businessNAME值
        // SystemLog systemLog=getSystemLog(joinPoint);

        log.info("=======Start=======");
        // 打印请求 URL
        log.info("URL            : {}",request.getRequestURL());
        // 打印描述信息
        log.info("BusinessName   : {}", systemLog.value());
        // 打印 Http method
        log.info("HTTP Method    : {}",request.getMethod() );
        // 打印调用 controller 的全路径(getDeclaringTypeName)以及执行方法
        log.info("Class Method   : {}.{}",joinPoint.getSignature().getDeclaringTypeName(), ((MethodSignature) joinPoint.getSignature()).getName());
        // 打印请求的 IP
        log.info("IP             : {}",request.getRemoteHost());
        // 打印请求入参
        Object[] args = joinPoint.getArgs();
        Object[] arguments = new Object[args.length];
        //以下几个类型无法json序列化
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse || args[i] instanceof MultipartFile) {
                continue;
            }
            arguments[i] = args[i];
        }
        log.info("Request Args   : {}", JSON.toJSONString(arguments));
    }

    private void handleAfter(Object result) {
        // 打印出参
        log.info("Response       : {}", JSON.toJSONString(result));
    }


    /**
     * @param joinPoint
     * @return SystemLog
     * @date 2023/4/12 22:37
     * @description 获取方法上的注解
     */
   /* private SystemLog getSystemLog(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SystemLog annotation = signature.getMethod().getAnnotation(SystemLog.class);
        return annotation;
    }*/

}
