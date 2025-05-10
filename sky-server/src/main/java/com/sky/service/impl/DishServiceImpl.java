package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisKeyConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.DishNotFoundException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final RedisTemplate redisTemplate;

    @Autowired
    public DishServiceImpl(DishMapper dishMapper, DishFlavorMapper dishFlavorMapper, SetmealDishMapper setmealDishMapper, RedisTemplate redisTemplate) {
        this.dishMapper = dishMapper;
        this.dishFlavorMapper = dishFlavorMapper;
        this.setmealDishMapper = setmealDishMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public void addWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insertOne(dish);

        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void changeStatus(Integer status, Long id) {
        Dish build = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.updateById(build);
    }

    @Override
    @Transactional
    public DishVO getById(Long id) {
        DishVO dishVO = dishMapper.selectById(id);
        if(dishVO != null){
            List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);
            dishVO.setFlavors(flavors);
            return dishVO;
        }else{
            throw new DishNotFoundException(MessageConstant.DISH_NOT_FOUND);
        }
    }

    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        return dishMapper.selectByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public void editWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();

        dishMapper.updateById(dish);
        dishFlavorMapper.deleteByDishId(dish.getId());
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断当前菜品是否在售
        ids.forEach(id -> {
            DishVO dishVO = dishMapper.selectById(id);
            if(dishVO.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });

        List<Long> setmealId = setmealDishMapper.selectSetmealIdByDishId(ids);
        if(setmealId != null && !setmealId.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        dishMapper.deleteBatch(ids);
        dishFlavorMapper.deleteBetchByDishId(ids);
    }


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {

        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 根据分类id查询启售中的菜品
     * @param categoryId 分类id
     * @return
     */
    public List<DishVO> listWithFlavor(Long categoryId){
        //从缓存中查询
        String key = RedisKeyConstant.DISH_LIST_PREFIX + categoryId;
        List<DishVO> dishVOS = (List<DishVO>) redisTemplate.opsForValue().get(key);

        //缓存中存在数据
        if(dishVOS != null && !dishVOS.isEmpty()){
            return dishVOS;
        }

        //缓存中不存在数据
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        dishVOS = listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, dishVOS);

        return dishVOS;
    }

}
