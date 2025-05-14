package com.sky.mapper;

import com.sky.dto.OrderDetailNameDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {
    void insertBatch(List<OrderDetail> orderDetails);

    /**
     * 根据订单id查询订单详情
     * @param orderId 订单id
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> selectByOrderId(Long orderId);


    List<OrderDetailNameDTO> selectNameByOrderIds(List<Long> ids);
}
