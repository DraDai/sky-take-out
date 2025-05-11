package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
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
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final ShoppingCartService shoppingCartService;
    private final OrderDetailMapper orderDetailMapper;
    private final AddressBookMapper addressBookMapper;

    public OrderServiceImpl(OrderMapper orderMapper, ShoppingCartService shoppingCartService, OrderDetailMapper orderDetailMapper, AddressBookMapper addressBookMapper) {
        this.orderMapper = orderMapper;
        this.shoppingCartService = shoppingCartService;
        this.orderDetailMapper = orderDetailMapper;
        this.addressBookMapper = addressBookMapper;
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

        //获取购物车数据
        List<ShoppingCart> shoppingCarts = shoppingCartService.list();

        //判断购物车数据是否为空
        if(shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //将ordersSubmitDTO转成实体类插入到订单表中
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
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
}
