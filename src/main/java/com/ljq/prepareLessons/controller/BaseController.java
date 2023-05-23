package com.ljq.prepareLessons.controller;

import com.alibaba.fastjson.JSONObject;

/**
 * @author 李佳琪
 * 2023-05-02
 */
public class BaseController {

    public static JSONObject ok(){
        JSONObject json = new JSONObject();
        json.put("code", "0000");
        json.put("msg", "成功");
        return json;
    }

    public static JSONObject error(String msg){
        JSONObject json = new JSONObject();
        json.put("code", "7777");
        json.put("msg", msg);
        return json;
    }
}
