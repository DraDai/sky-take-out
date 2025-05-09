package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.WeChatConstant;
import com.sky.properties.WeChatProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信登录工具类
 */
@Component
public class WeChatLoginUtil {
    private final WeChatProperties weChatProperties;

    @Autowired
    public WeChatLoginUtil(WeChatProperties weChatProperties) {
        this.weChatProperties = weChatProperties;
    }

    public String getOpenid(String code){
        Map<String, String> param = new HashMap<>();
        param.put("appid", weChatProperties.getAppid());
        param.put("secret", weChatProperties.getSecret());
        param.put("js_code", code);
        param.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WeChatConstant.WX_LOGIN, param);

        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
