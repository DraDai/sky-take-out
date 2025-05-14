package com.sky.apidto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeoCodeDTO implements Serializable {
    private String country; //国家
    private String province; //省
    private String city; //城市
    private String cityCode; //城市编码
    private String district; //区
    private String street; //街道
    private String number; //门牌号
    private String adCode; //行政区划编码
    private String location; //经纬度
    private String lever; //地址级别
}
