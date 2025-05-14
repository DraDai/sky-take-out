package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.properties.GaoDeProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.GaoDeUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderMapper orderMapper;
    private final ShoppingCartService shoppingCartService;
    private final OrderDetailMapper orderDetailMapper;
    private final AddressBookMapper addressBookMapper;
    private final WeChatPayUtil weChatPayUtil;
    private final GaoDeUtil gaoDeUtil;
    private final GaoDeProperties gaoDeProperties;

    public OrderServiceImpl(OrderMapper orderMapper, ShoppingCartService shoppingCartService, OrderDetailMapper orderDetailMapper, AddressBookMapper addressBookMapper, WeChatPayUtil weChatPayUtil, GaoDeUtil gaoDeUtil, GaoDeProperties gaoDeProperties) {
        this.orderMapper = orderMapper;
        this.shoppingCartService = shoppingCartService;
        this.orderDetailMapper = orderDetailMapper;
        this.addressBookMapper = addressBookMapper;
        this.weChatPayUtil = weChatPayUtil;
        this.gaoDeUtil = gaoDeUtil;
        this.gaoDeProperties = gaoDeProperties;
    }

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //获取地址簿数据
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());

        //判断地址簿数据是否为空
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        String userAddress = addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail();
        //判断地址是否在配送范围内
        Boolean isOut = gaoDeUtil.checkOutOfRange(userAddress, gaoDeProperties.getAddress(), 5000);
        if(isOut){
            throw new OrderBusinessException(MessageConstant.OUT_OF_DELIVERY_RANGE);
        }
        //获取购物车数据
        List<ShoppingCart> shoppingCarts = shoppingCartService.list();

        //判断购物车数据是否为空
        if(shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //将ordersSubmitDTO转成实体类插入到订单表中
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        String address = addressBook.getProvinceName() +
                addressBook.getCityName() +
                addressBook.getDistrictName() +
                addressBook.getDetail();


        orders.setAddress(address);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());
        orderMapper.insertOne(orders);


        //将购物车数据转换为订单明细数据
        List<OrderDetail> orderDetails = shoppingCarts.stream().map(shoppingCart ->
                OrderDetail.builder()
                        .name(shoppingCart.getName())
                        .image(shoppingCart.getImage())
                        .orderId(orders.getId())
                        .dishId(shoppingCart.getDishId())
                        .setmealId(shoppingCart.getSetmealId())
                        .dishFlavor(shoppingCart.getDishFlavor())
                        .number(shoppingCart.getNumber())
                        .amount(shoppingCart.getAmount())
                        .build()
        ).collect(Collectors.toList());

        //将订单明细数据插入到订单明细表中
        orderDetailMapper.insertBatch(orderDetails);
        //清空购物车数据
        shoppingCartService.clean();

        //返回订单提交结果
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    @Override
    public PageResult listOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        //查询订单数据
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);


        if(page != null && page.getTotal() > 0){{
            List<Orders> ordersList = page.getResult();

            //根据订单id查询订单明细数据
            List<OrderVO> orderVOS = ordersList.stream().map(orders -> {
                List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(orders.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                return orderVO;
            }).collect(Collectors.toList());

            return new PageResult(page.getTotal(), orderVOS);
        }}
        return new PageResult(0L, new ArrayList<>());
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
        OrderVO orderVO = orderMapper.selectById(id);
        if(orderVO == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }


    @Override
    public void cancelOrder(Long id) {
        OrderVO orderVO = orderMapper.selectById(id);

        if (orderVO == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if(orderVO.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        if(orderVO.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setId(orderVO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(id);
        if(orderDetails == null || orderDetails.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        List<ShoppingCart> shoppingCarts = orderDetails.stream().map(orderDetail ->
                ShoppingCart.builder()
                        .userId(BaseContext.getCurrentId())
                        .name(orderDetail.getName())
                        .dishId(orderDetail.getDishId())
                        .setmealId(orderDetail.getSetmealId())
                        .dishFlavor(orderDetail.getDishFlavor())
                        .number(orderDetail.getNumber())
                        .amount(orderDetail.getAmount())
                        .image(orderDetail.getImage())
                        .createTime(LocalDateTime.now())
                        .build()
        ).collect(Collectors.toList());
        shoppingCartService.clean();
        shoppingCartService.batchAdd(shoppingCarts);
    }

    @Override
    public PageResult allList(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        //查询订单数据
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);

        if(orders != null && orders.getTotal() > 0){
            List<Orders> ordersList = orders.getResult();

            //根据订单id查询菜品名
            List<Long> ids = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
            List<OrderDetailNameDTO> orderDetailNameDTOS = orderDetailMapper.selectNameByOrderIds(ids);

            List<OrderVO> orderVOS = ordersList.stream()
                    .map(order -> {
                        OrderVO orderVO = new OrderVO();
                        BeanUtils.copyProperties(order, orderVO);
                        String name = orderDetailNameDTOS.stream().filter(orderDetailNameDTO -> orderDetailNameDTO.getOrderId().equals(order.getId()))
                                        .findFirst().orElse(null).getName();
                        orderVO.setOrderDishes(name);
                        return orderVO;
                    }).collect(Collectors.toList());

            return new PageResult(orders.getTotal(), orderVOS);
        }
        return new PageResult(0L, new ArrayList<>());

    }

    @Override
    public OrderStatisticsVO statistics() {
        List<Integer> statusList = List.of(Orders.CONFIRMED, Orders.DELIVERY_IN_PROGRESS, Orders.TO_BE_CONFIRMED);
        Map<Integer, OrderStatusCountDTO> orderStatusCountDTOS = orderMapper.countByStatusList(statusList);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        OrderStatusCountDTO confirmed = orderStatusCountDTOS.get(Orders.CONFIRMED);
        OrderStatusCountDTO toBeConfirmed = orderStatusCountDTOS.get(Orders.TO_BE_CONFIRMED);
        OrderStatusCountDTO deliveryInProgress = orderStatusCountDTOS.get(Orders.DELIVERY_IN_PROGRESS);

        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress == null ? 0 : deliveryInProgress.getCount());
        orderStatisticsVO.setConfirmed(confirmed == null ? 0 : confirmed.getCount());
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed == null ? 0 : toBeConfirmed.getCount());
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //根据订单id获取订单状态
        OrderVO orderVO = orderMapper.selectById(ordersRejectionDTO.getId());
        //判断订单状态是否为待接单
        if(orderVO == null || !orderVO.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //判断用户是否已支付
        Integer payStatus = orderVO.getPayStatus();
        //TODO 退款功能
        if(payStatus.equals(Orders.PAID)){
            log.info("用户已支付, 进行退款操作");
        }

        // 更新订单状态
        Orders orders = Orders.builder()
                .id(orderVO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);

    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        // 根据订单id查询订单
        OrderVO orderVO = orderMapper.selectById(ordersCancelDTO.getId());

        if(orderVO == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //判断订单状态是否为已支付
        Integer payStatus = orderVO.getPayStatus();
        //TODO 退款功能
        if(payStatus.equals(Orders.PAID)){
            log.info("用户已支付, 进行退款操作");
        }

        //更新订单状态、取消原因、取消时间
        Orders orders = Orders.builder()
                .id(orderVO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        OrderVO orderVO = orderMapper.selectById(id);
        if(orderVO == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if(!orderVO.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        OrderVO orderVO = orderMapper.selectById(id);

        if(orderVO == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if(!orderVO.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(orders);
    }
}
