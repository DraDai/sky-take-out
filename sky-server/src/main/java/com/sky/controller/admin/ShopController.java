package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
@Api(tags = "商家相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;


    @PutMapping("/{status}")
    @ApiOperation("修改商家状态")
    public Result editStatus(@PathVariable Integer status) {
        log.info("修改商家状态为：{}", status == 1 ? "营业中" : "打烊");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("shopStatus", status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查询商家状态")
    public Result<Integer> getStatus() {

        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer status = (Integer) valueOperations.get("shopStatus");
        if (status == null) {
            status = 0; // 默认状态
        }
        log.info("查询商家状态为{}", status == 1 ? "营业中" : "打烊");
        return Result.success(status);
    }
}
