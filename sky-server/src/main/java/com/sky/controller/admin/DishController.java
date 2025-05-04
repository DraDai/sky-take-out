package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理")
public class DishController {
    private static final Logger log = LoggerFactory.getLogger(DishController.class);
    private final DishService dishService;

    @Autowired
    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping
    @ApiOperation(value = "添加菜品")
    public Result add(@RequestBody DishDTO dishDTO){
        log.info("添加菜品，参数：{}", dishDTO);
        dishService.addWithFlavor(dishDTO);
        return Result.success();
    }
}
