package org.example.jubensha.common;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;    // 是否成功
    private Integer code;       // 状态码
    private String msg;         // 提示信息
    private T data;             // 数据

    // 成功返回
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 失败返回
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(500);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}