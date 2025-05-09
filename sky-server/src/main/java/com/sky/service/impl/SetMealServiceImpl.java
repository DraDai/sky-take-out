package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;

    @Autowired
    public SetMealServiceImpl(SetmealMapper setmealMapper, SetmealDishMapper setmealDishMapper) {
        this.setmealMapper = setmealMapper;
        this.setmealDishMapper = setmealDishMapper;
    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void add(SetmealDTO setmealDTO) {
        Setmeal setmeal = Setmeal.builder().build();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insertOne(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes == null || setmealDishes.isEmpty()) {
            return;
        }
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });

        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public void editStatus(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();

        setmealMapper.update(setmeal);
    }

    @Override
    @Transactional
    public SetmealVO getById(Long id) {
        List<SetmealDish> setmealDishes = setmealDishMapper.selectDishBySetmealId(id);
        SetmealVO setmealVO = setmealMapper.selectById(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void edit(SetmealDTO setmealDTO) {
        Setmeal setmeal = Setmeal.builder().build();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setmealDishMapper.deleteBySetmealId(setmeal.getId());
        if(setmealDishes.isEmpty()) {
            return;
        }else{
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    @Override
    @Transactional
    public void delete(Long[] ids) {
        setmealDishMapper.deleteBatchBySetmealId(ids);
        setmealMapper.deleteBatch(ids);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
