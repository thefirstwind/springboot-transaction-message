package com.tdw.transaction.constant;

/**
 * 
 */
public enum BZStatusCode {

    OK(200, "请求成功"),

    // 30xxx 状态操作不合法
    INVALID_STATE_OPTION(30001, "翻转消息状态违规"),

    // 40xxx 客户端不合法的请求
    INVALID_MODEL_FIELDS(40001, "字段校验非法"),

    /**
     * 参数类型非法，常见于SpringMVC中String无法找到对应的enum而抛出的异常
     */
    INVALID_PARAMS_CONVERSION(40002, "参数类型非法"),

    /**
     * 参数不允许为空
     */
    INVALID_PARAMS_IS_NULL(40003, "参数不允许为空"),

    // 41xxx 请求方式出错
    /**
     * http media type not supported
     */
    HTTP_MESSAGE_NOT_READABLE(41001, "HTTP消息不可读"),

    /**
     * 请求方式非法
     */
    REQUEST_METHOD_NOT_SUPPORTED(41002, "不支持的HTTP请求方法"),

    // 成功接收请求, 但是处理失败
    /**
     * Duplicate Key
     */
    DUPLICATE_KEY(42001, "操作过快, 请稍后再试"),

    // 50xxx 服务端异常
    /**
     * 用于处理未知的服务端错误
     */
    SERVER_UNKNOWN_ERROR(50001, "服务端异常, 请稍后再试"),

    /**
     * 用于远程调用时的系统出错
     */
    SERVER_IS_BUSY_NOW(50002, "系统繁忙, 请稍后再试");

    private final int code;

    private final String message;


    BZStatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

}
