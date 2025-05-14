package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrderStatusCountDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     * @param orders 订单对象
     */
    void insertOne(Orders orders);


    /**
     * 分页查询订单表数据
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * ·
     * @param id
     * @return
     */
    @Select("SELECT * FROM sky_take_out.orders WHERE id = #{id}")
    OrderVO selectById(Long id);

    void update(Orders orders);

    /**
     * 不同订单状态的数量
     * @param statusList
     * @return
     */
    @MapKey("status")
    Map<Integer, OrderStatusCountDTO> countByStatusList(List<Integer> statusList);
}
