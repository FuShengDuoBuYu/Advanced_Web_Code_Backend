package com.se.advancedweb.util;

public class Response<T> {

    private Boolean success;

    private String message;

    private T data;

    public Response() {
    }

    public Response(Boolean success) {
        this.success = success;
    }

    public Response(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Response(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> yes() {
        return new Response(true, "操作成功");
    }

    public static <T> Response<T> yes(String message,T data) {
        return new Response(true, message, data);
    }

    public static <T> Response<T> yes(T data) {
        return new Response(true, "操作成功", data);
    }


    public static <T> Response<T> no() {
        return new Response(false, "操作失败");
    }

    public static <T> Response<T> no(String message) {
        return new Response(false, message);
    }


    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
