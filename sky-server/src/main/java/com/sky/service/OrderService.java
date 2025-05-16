package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 订单提交
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 分页查询单个用户历史订单数据
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult listOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单id查询订单详情数据
     * @param id
     * @return
     */
    OrderVO getOrderDetail(Long id);

    /**
     * 取消订单
     * @param id 订单id
     */
    void cancelOrder(Long id);

    /**
     * 用户再来一旦业务功能,将订单内容重新放入购物车中
     * @param id
     * @return
     */
    void repetition(Long id);

    /**
     * 查询所有符合条件的订单数据
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult allList(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单,将订单状态修改为“已取消”,指定取消原因,取消订单时，如果用户已经完成了支付，需要为用户退款
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单,将订单状态修改为“派送中”,只有状态为“待派送”的订单可以执行派送订单操作
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成订单,将订单状态修改为“已完成”,只有状态为“派送中”的订单可以执行完成订单操作
     * @param id
     */
    void complete(Long id);

    /**
     * 催单
     * @param id 订单id
     */
    void reminder(Long id);
}
