package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.apidto.GeoCodeDTO;
import com.sky.constant.MessageConstant;
import com.sky.exception.GaoDeException;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.GaoDeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GaoDeUtil {
    private static final Logger log = LoggerFactory.getLogger(GaoDeUtil.class);
    private final GaoDeProperties gaoDeProperties;

    @Autowired
    public GaoDeUtil(GaoDeProperties gaoDeProperties) {
        this.gaoDeProperties = gaoDeProperties;
    }

    /**
     * 获取高德地图地理编码
     * @param address 地址
     * @return GeoCodeDTO
     */
    private GeoCodeDTO geoCodeDTO(String address){
        //创建一个Map对象，用于存储请求参数
        Map<String, String> param = new HashMap<>();
        param.put("key", gaoDeProperties.getGaodeKey());
        param.put("address", address);
        //发送GET请求，获取响应结果
        String result = HttpClientUtil.doGet("https://restapi.amap.com/v3/geocode/geo", param);
        JSONObject jsonObject = JSON.parseObject(result);


        //判断请求是否成功
        if(jsonObject.getString("status").equals("0")){
            throw new GaoDeException(jsonObject.getString("errdetail"));
        }

        //获取地理编码结果
        JSONObject geocode = jsonObject.getJSONArray("geocodes").getJSONObject(0);
        //构造GeoCodeDTO对象

        return GeoCodeDTO.builder()
                .country(geocode.getString("country"))
                .province(geocode.getString("province"))
                .city(geocode.getString("city"))
                .cityCode(geocode.getString("citycode"))
                .district(geocode.getString("district"))
                .street(geocode.getString("street"))
                .number(geocode.getString("number"))
                .adCode(geocode.getString("adcode"))
                .location(geocode.getString("location"))
                .lever(geocode.getString("level"))
                .build();
    }

    /**
     * 获取指定地址的经纬度
     * @param address 商家地址
     * @return 经纬度字符串
     */
    public String getLocation(String address){
        //获取高德地图地理编码
        GeoCodeDTO geoCodeDTO = geoCodeDTO(address);
        return geoCodeDTO.getLocation();
    }

    /**
     * 获取两个地点之间的骑行距离
     * @param origin 起点经纬度
     * @param destination 终点经纬度
     * @return 距离（米）
     */
    public Integer getDistance(String origin, String destination){
        //创建一个Map对象，用于存储请求参数
        Map<String, String> param = new HashMap<>();
        param.put("key", gaoDeProperties.getGaodeKey());
        param.put("origin", origin);
        param.put("destination", destination);
        //发送GET请求，获取响应结果
        String result = HttpClientUtil.doGet("https://restapi.amap.com/v4/direction/bicycling", param);
        JSONObject jsonObject = JSON.parseObject(result);

        if(jsonObject.getInteger("errcode") != 0){
            throw new GaoDeException(jsonObject.getString("errdetail"));
        }

        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray paths = data.getJSONArray("paths");
        JSONObject jsonObject1 = paths.getJSONObject(0);
        Integer integer = jsonObject1.getInteger("distance");
        return jsonObject.getJSONObject("data").getJSONArray("paths").getJSONObject(0).getInteger("distance");
    }

    /**
     * 判断两个地点是否超出指定范围
     * @param origin 起点地址
     * @param destination 终点地址
     * @param range 范围（米）
     * @return
     */
    public Boolean checkOutOfRange(String origin, String destination, Integer range){
        String o = getLocation(origin);
        String d = getLocation(destination);
        return getDistance(o, d) > range;
    }
}
