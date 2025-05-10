package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 根据用户id和菜品id或套餐id或口味数据查询购物车
     * @param shoppingCart
     * @return
     */
    ShoppingCart selectOne(ShoppingCart shoppingCart);

    /**
     * 插入一条购物车
     * @param shoppingCart
     */
    @Insert("INSERT INTO shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "VALUES (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insertOne(ShoppingCart shoppingCart);

    /**
     * 根据购物车id更新商品数量
     * @param cart
     */
    @Update("UPDATE shopping_cart SET number = #{number} WHERE id = #{id}")
    void updateNumberById(ShoppingCart cart);
}
