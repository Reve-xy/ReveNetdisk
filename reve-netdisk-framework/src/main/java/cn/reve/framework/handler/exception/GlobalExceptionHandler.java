package cn.reve.framework.handler.exception;


import cn.reve.framework.enums.HttpCodeEnum;
import cn.reve.framework.exception.SystemException;
import cn.reve.framework.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 简要描述
 * 全局异常处理
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/7 10:06
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 权限校验异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',权限校验失败'{}'", requestURI, e.getMessage());
        return Result.failure(HttpCodeEnum.NO_OPERATOR_AUTH);
    }

    //捕获自定义异常
    @ExceptionHandler(SystemException.class)
    public Result systemExceptionHandler(SystemException e,HttpServletRequest request) {
        log.error("请求地址：'{}',出现自定义异常({})--->{}", request.getRequestURI(),e.getClass().getSimpleName(), e.getMsg());
        e.printStackTrace();
        return Result.failure(e.getCode(), e.getMsg());
    }


    /*
     * 全局异常补捉在security的认证处理器之前
     * */
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public Result badCredentialsExceptionExceptionHandler(InternalAuthenticationServiceException e,HttpServletRequest request) {
        log.error("请求地址：'{}',出现自定义异常({})--->{}", request.getRequestURI(),e.getClass().getSimpleName(), e.getMessage());
        return Result.failure(61,e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result badCredentialsExceptionExceptionHandler(BadCredentialsException e,HttpServletRequest request) {
        log.error("请求地址：'{}',出现自定义异常({})--->{}", request.getRequestURI(),e.getClass().getSimpleName(), e.getMessage());
        return Result.failure(HttpCodeEnum.LOGIN_ERROR);
    }

    //捕捉@vaild抛出的异常
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Result paramExceptionHandler(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("出现参数错误({})--->{}", e.getClass().getSimpleName(), e.getMessage());
        BindingResult exceptions = e.getBindingResult();
        // 判断异常中是否有错误信息，如果存在就使用异常中的消息，否则使用默认消息
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();
            if (!errors.isEmpty()) {
                // 这里列出了全部错误参数，按正常逻辑，只需要第一条错误即可
                FieldError fieldError = (FieldError) errors.get(0);
                return Result.failure(fieldError.getDefaultMessage());
            }
        }
        return Result.failure("请求参数错误");
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址：'{}',不支持'{}'请求", requestURI, e.getMethod());
        return Result.failure(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e,HttpServletRequest request) {
        log.error("请求地址为：'{}',出现未知内部错误({})--->{}",request.getRequestURL(), e.getClass().getSimpleName(), e.getMessage());
        e.printStackTrace();
        return Result.failure(e.getMessage());
    }

}
