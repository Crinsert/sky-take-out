package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;

import java.util.List;

public interface ShoppingCarService {


    List<ShoppingCart> list(Long id);

    void add(ShoppingCartDTO shoppingCartDTO);

    void clear();

    void sub(ShoppingCartDTO shoppingCartDTO);
}
