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

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * 营业额统计,获取某个时间段的已完成订单的金额总计
     * @param start 开始时间
     * @param end 结束时间
     * @return 营业额
     */
    Integer selectTurnoverByDate(LocalDateTime start, LocalDateTime end);

    /**
     * 订单总数
     * @return
     */
    @Select("select IFNULL(COUNT(id), 0) from orders")
    Integer allCount();

    /**
     * 有效订单数,即已完成的订单
     * @return
     */
    @Select("SELECT IFNULL(COUNT(id), 0) FROM orders WHERE status = 5")
    Integer validCount();

    /**
     * 查询某日新增订单数量
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("SELECT IFNULL(COUNT(id), 0) FROM orders WHERE orders.order_time > #{startTime} AND orders.order_time < #{endTime}")
    Integer selectOrderCountByDate(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询某日有效订单数量
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("SELECT IFNULL(COUNT(id), 0) FROM orders WHERE orders.order_time > #{startTime} AND orders.order_time < #{endTime} AND status = 5")
    Integer selectValidOrderCountByDate(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询某个时间段的订单id
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("SELECT id FROM orders WHERE order_time > #{startTime} AND order_time < #{endTime}")
    List<Long> selectOrderIdsByDate(LocalDateTime startTime, LocalDateTime endTime);
}
