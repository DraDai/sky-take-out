package com.sky.controller.admin;

import com.sky.constant.RedisKeyConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("AdminSetMealController")
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐管理")
public class SetMealController {
    private final SetMealService setMealService;

    @Autowired
    public SetMealController(SetMealService setMealService) {
        this.setMealService = setMealService;
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询套餐：{}", setmealPageQueryDTO);
        PageResult pageResult = setMealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    @ApiOperation(value = "新增套餐")
    @CacheEvict(cacheNames = RedisKeyConstant.SETMEAL_LIST_PREFIX, key = "#setmealDTO.categoryId")
    public Result add(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐：{}", setmealDTO);
        setMealService.add(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = RedisKeyConstant.SETMEAL_LIST_PREFIX, allEntries = true)
    @ApiOperation(value = "修改套餐状态")
    public Result editStatus(@PathVariable Integer status, @RequestParam Long id){
        log.info("修改套餐状态：{}，{}", status, id);
        setMealService.editStatus(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐：{}", id);
        SetmealVO setmealVO = setMealService.getById(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @CacheEvict(cacheNames = RedisKeyConstant.SETMEAL_LIST_PREFIX, allEntries = true)
    @ApiOperation(value = "修改套餐")
    public Result edit(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO);
        setMealService.edit(setmealDTO);
        return Result.success();
    }

    @DeleteMapping
    @CacheEvict(cacheNames = RedisKeyConstant.SETMEAL_LIST_PREFIX, allEntries = true)
    @ApiOperation(value = "批量删除套餐")
    public Result delete(@RequestParam Long[] ids) {
        log.info("批量删除套餐：{}", ids);
        setMealService.delete(ids);
        return Result.success();
    }



}
