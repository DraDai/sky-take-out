package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.shop")
@Data
public class GaoDeProperties {
    private String gaodeKey; //高德地图key
    private String address; //商家地址
}
