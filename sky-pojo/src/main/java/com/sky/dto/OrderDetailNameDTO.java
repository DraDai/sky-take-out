package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderDetailNameDTO implements Serializable {
    private Long orderId;
    private String name;
}
