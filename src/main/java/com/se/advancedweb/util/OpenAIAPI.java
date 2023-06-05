package com.se.advancedweb.util;


import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class OpenAIAPI {

    String chatEndpoint = "https://api.openai.com/v1/chat/completions";

    String apiKey = "Bearer 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";
    public String chat(String txt, List<Map<String, String>> dataList) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("model", "gpt-3.5-turbo");
        paramMap.put("temperature", 0.6);

        dataList.add(new HashMap<String, String>(){{
            put("role", "user");
            put("content", txt);
        }});

        paramMap.put("messages", dataList);
        JSONObject message = null;
        try {
            String body = HttpRequest.post(chatEndpoint)
                    .header("Authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSON.toJSONString(paramMap))
                    .execute()
                    .body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
//            System.out.println(jsonObject.toString());
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject result = choices.get(0, JSONObject.class, Boolean.TRUE);
            message = result.getJSONObject("message");
        } catch (HttpException e) {
            return "出现了异常";
        } catch (ConvertException e) {
            return "出现了异常";
        }
        return message.getStr("content");
    }
    public static void main(String[] args) {
        List<Map<String, String>> dataList = new ArrayList<>();
        dataList.add(new HashMap<String, String>(){{
            put("role", "assistant");
            put("content", "你好，我是机器人小助手，有什么可以帮助你的吗？");
        }});
        System.out.println("<request>:");
        System.out.println("你是谁");
        System.out.println("<response>:");
        System.out.println(chat("你是谁", dataList));
    }
}
