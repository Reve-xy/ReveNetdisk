package cn.reve.framework.utils;

import cn.reve.framework.enums.HttpCodeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/3/29 16:56
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
public class Result<T> implements Serializable {

    //状态码
    private Integer code;

    //信息
    private String msg;

    //响应体数据
    private T data;

    private Result() {
        this.code= HttpCodeEnum.SUCCESS.getCode();
        this.msg= HttpCodeEnum.SUCCESS.getMsg();
    }

    public static <T>Result<T> success(Integer code, String msg, T data){
        Result<T> result=new Result<>();
        if (code != null) {
            result.setCode(code);
        }
        if(msg!=null) {
            result.setMsg(msg);
        }
        if(data!=null) {
            result.setData(data);
        }
        return result;
    }

    public static <T>Result<T> failure(Integer code,String msg,T data){
        Result<T> result=new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        if(data!=null){
            result.setData(data);
        }
        return result;
    }

    public static <T>Result<T> success(HttpCodeEnum appHttpCodeEnum, T data){
        return Result.success(appHttpCodeEnum.getCode(),appHttpCodeEnum.getMsg(),data);
    }

    public static <T>Result<T> success(){
        return new Result<>();
    }

    public static Result success(HttpCodeEnum appHttpCodeEnum){
        return Result.success(appHttpCodeEnum.getCode(),appHttpCodeEnum.getMsg(),null);
    }

    public static Result success(Integer code,String msg){
        return Result.success(code,msg,null);
    }

    public static <T>Result<T> success(String msg,T data){
        return Result.success(null,msg,data);
    }

    public static <T>Result<T> success(T data){
        Result result = new Result();
        result.setData(data);
        return result;
    }

    public static <T>Result<T> failure(){
        return Result.failure(HttpCodeEnum.FAILURE);
    }


    public static <T>Result<T> failure(HttpCodeEnum appHttpCodeEnum, T data){
        return Result.failure(appHttpCodeEnum.getCode(),appHttpCodeEnum.getMsg(),data);
    }

    public static <T>Result<T> failure(String msg){
        return Result.failure(400,msg,null);
    }

    public static Result failure(HttpCodeEnum appHttpCodeEnum){
        return Result.failure(appHttpCodeEnum.getCode(),appHttpCodeEnum.getMsg(),null);
    }

    public static Result failure(Integer code,String msg){
        return Result.failure(code,msg,null);
    }

}
