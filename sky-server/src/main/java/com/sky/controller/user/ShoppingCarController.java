package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCarService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端-购物车接口")
public class ShoppingCarController {
    @Autowired
    private ShoppingCarService shoppingCarService;
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result list(){
        log.info("查看购物车");
        Long id = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCart =shoppingCarService.list(id);
        return Result.success(shoppingCart);
    }

    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车:{}",shoppingCartDTO);
        shoppingCarService.add(shoppingCartDTO);
        return Result.success();
    }
    @ApiOperation("清空购物车")
    @Delete("/clear")
    public Result clear(){
        log.info("清空购物车");
        shoppingCarService.clear();
        return Result.success();
    }
    @ApiOperation("删除购物车中的商品")
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车中的商品:{}",shoppingCartDTO);
        shoppingCarService.sub(shoppingCartDTO);
        return Result.success();
    }

}
