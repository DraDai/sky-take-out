package com.sky.controller.user;

import com.sky.constant.RedisKeyConstant;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "购物车相关接口")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车：{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "查询购物车")
    public Result<List<ShoppingCart>> list() {
        log.info("查询购物车");
        List<ShoppingCart> shoppingCartList = shoppingCartService.list();
        return Result.success(shoppingCartList);
    }

    @PostMapping("/sub")
    @ApiOperation(value = "减少购物车")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("减少购物车：{}", shoppingCartDTO);
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车")
    public Result clean() {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }
}
