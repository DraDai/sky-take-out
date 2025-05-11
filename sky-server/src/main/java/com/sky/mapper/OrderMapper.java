package com.sky.mapper;

import com.sky.entity.Orders;
import com.sky.vo.OrderSubmitVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     * @param orders 订单对象
     */
    void insertOne(Orders orders);

}
