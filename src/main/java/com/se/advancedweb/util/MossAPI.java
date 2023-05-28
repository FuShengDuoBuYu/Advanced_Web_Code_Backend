package com.se.advancedweb.util;

import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.lang.hash.Hash;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import lombok.experimental.UtilityClass;

import java.util.HashMap;

@UtilityClass
public class MossAPI {
    private String apiKey = "K7QKpcbCEK1p77a6JNKtOxuPggBU0cdL";
    private String apiUrl = "http://124.220.26.113/api/inference"; // 校外


    public HashMap<String, String> sendRequest(String message, String context) {
        // 判断是否含有上下文
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("request", message);
        String response = "";
        String contextResponse = "";
        HashMap<String, String> result = new HashMap<>();
        if (!context.isEmpty()) {
            requestBody.put("context", context);
        }
        try {
            String body = HttpRequest.post(apiUrl)
                    .header("apikey", apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSON.toJSONString(requestBody))
                    .execute()
                    .body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            response = jsonObject.getStr("response");
            contextResponse = jsonObject.getStr("context");
        } catch (
                HttpException e) {
            System.out.println("出现了异常");
            return null;
        } catch (
                ConvertException e) {
            System.out.println("出现了异常");
            return null;
        }
        result.put("response", response);
        result.put("context", contextResponse);
        return result;
    }
    public static void main(String[] args) {
        // 示例1：不含有上下文的请求
        String requestText = "hi";
        String response = MossAPI.sendRequest(requestText, "").get("response");
        String context = MossAPI.sendRequest(requestText, "").get("context");
        System.out.println(response);
        System.out.println(context);

        // 示例2：含有上下文的请求
        requestText = "what's your name?";
        response = MossAPI.sendRequest(requestText, context).get("response");
        context = MossAPI.sendRequest(requestText, context).get("context");
        System.out.println(response);
        System.out.println(context);
    }
}

