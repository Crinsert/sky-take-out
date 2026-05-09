package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCarMapper {

    @Select("SELECT * FROM shopping_cart where id=#{id}")
    List<ShoppingCart> list(Long id);

    List<ShoppingCart> selectByDishOrSetmeal(ShoppingCart shoppingCart);

    @Update("UPDATE shopping_cart SET number = #{number} WHERE id = #{id}")
    void update(ShoppingCart cart);

    void insert(ShoppingCart shoppingCart);

    void deleteByUserId(Long currentId);

    void deleteByStemealIdAndDishId(ShoppingCartDTO shoppingCartDTO);
}
