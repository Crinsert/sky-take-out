package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCarMapper;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.ShoppingCarService;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ShoppingCarServiceImpl implements ShoppingCarService {
    @Autowired
    private ShoppingCarMapper shoppingCarMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public List<ShoppingCart> list(Long id) {
        List<ShoppingCart> shoppingCarts=shoppingCarMapper.list(id);
        return shoppingCarts;
    }

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setCreateTime(LocalDateTime.now());
        Long userid = BaseContext.getCurrentId();
        shoppingCart.setUserId(userid);
        //判断当前菜品或套餐是否在购物车中
        List<ShoppingCart> shoppingCarts = shoppingCarMapper.selectByDishOrSetmeal(shoppingCart);
        if (shoppingCarts != null && shoppingCarts.size() > 0) {
            ShoppingCart cart = shoppingCarts.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCarMapper.update(cart);
        }
        else {
            //添加进购物车
            //判断添加进是菜品还是套餐
            if (shoppingCartDTO.getDishId() != null) {
                DishVO dish = dishMapper.getById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else{
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCarMapper.insert(shoppingCart);
        }
    }

    @Override
    public void clear() {
        shoppingCarMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        shoppingCarMapper.deleteByStemealIdAndDishId(shoppingCartDTO);
    }
}
