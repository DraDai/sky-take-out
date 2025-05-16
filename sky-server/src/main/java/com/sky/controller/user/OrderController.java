package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端订单接口")
@Slf4j
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApiOperation("用户下单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("下单参数: {}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @ApiOperation("用户历史订单查询")
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单查询参数: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.listOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("用户查询订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        log.info("查询订单详情参数: {}", id);
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    @ApiOperation("用户取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        log.info("取消订单参数: {}", id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    @ApiOperation("用户支付订单")
    @PutMapping("payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        log.info("支付订单参数: {}", ordersPaymentDTO);
        OrderPaymentVO paymentVO = OrderPaymentVO.builder()
                .nonceStr("123456")
                .paySign("123456")
                .timeStamp(String.valueOf(System.currentTimeMillis()))
                .signType("MD5")
                .packageStr("123456")
                .build();
        return Result.success(paymentVO);
    }

    @ApiOperation("用户再来一单")
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id) {
        log.info("再来一单参数: {}", id);
        orderService.repetition(id);
        return Result.success();
    }

    @ApiOperation("催单")
    @GetMapping("reminder/{id}")
    public Result reminder(@PathVariable Long id) {
        log.info("催单参数: {}", id);
        orderService.reminder(id);
        return Result.success();
    }

}
