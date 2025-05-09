package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO 菜品信息
     */
    void addWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 修改菜品状态
     * @param status 菜品状态
     * @param id 菜品id
     */
    void changeStatus(Integer status, Long id);

    /**
     * 根据id查询菜品
     * @param id 菜品id
     * @return 菜品视图类
     */
    DishVO getById(Long id);

    /**
     * 根据分类id查询菜品
     * @param categoryId 分类id
     * @return 菜品列表
     */
    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 修改菜品
     * @param dishDTO 菜品信息
     */
    void editWithFlavor(DishDTO dishDTO);

    /**
     * 批量删除菜品
     * @param ids 菜品id列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

}
