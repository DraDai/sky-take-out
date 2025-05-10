package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.models.auth.In;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Autowired
    public ShoppingCartServiceImpl(ShoppingCartMapper shoppingCartMapper, DishMapper dishMapper, SetmealMapper setmealMapper) {
        this.shoppingCartMapper = shoppingCartMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //动态查询购物车
        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        //根据用户id和菜品id或套餐id或口味数据查询购物车
        ShoppingCart cart = shoppingCartMapper.selectOne(shoppingCart);

        //如果数据库内没有购物车数据
        if(cart == null){
            String name = null;
            Integer number = 1;
            BigDecimal amount = null;
            String image = null;
            LocalDateTime createTime = LocalDateTime.now();

            //判断商品是套餐还是菜品
            if(shoppingCartDTO.getDishId() != null){
                //查询菜品
                DishVO dishVO = dishMapper.selectById(shoppingCartDTO.getDishId());
                name = dishVO.getName();
                amount = dishVO.getPrice();
                image = dishVO.getImage();
            }else{
                //查询套餐
                SetmealVO setmealVO = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
                name = setmealVO.getName();
                amount = setmealVO.getPrice();
                image = setmealVO.getImage();
            }

            //构建购物车
            shoppingCart.setName(name);
            shoppingCart.setNumber(number);
            shoppingCart.setAmount(amount);
            shoppingCart.setImage(image);
            shoppingCart.setCreateTime(createTime);

            //插入购物车
            shoppingCartMapper.insertOne(shoppingCart);
        }else{
            cart.setNumber(cart.getNumber()+1);
            //更新购物车
            shoppingCartMapper.updateNumberById(cart);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId()).build();
        return shoppingCartMapper.selectBatchByUserId(shoppingCart);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //动态查询购物车
        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        //根据用户id和菜品id或套餐id或口味数据查询购物车
        ShoppingCart cart = shoppingCartMapper.selectOne(shoppingCart);

        //如果数据库内有购物车数据
        if(cart != null) {
            Integer number = cart.getNumber();
            //如果数量大于1，则减少数量
            if (number > 1) {
                cart.setNumber(number - 1);
                //更新购物车
                shoppingCartMapper.updateNumberById(cart);
            } else {
                //删除购物车
                shoppingCartMapper.deleteById(cart.getId());
            }
        }
    }

    @Override
    public void clean() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }
}
