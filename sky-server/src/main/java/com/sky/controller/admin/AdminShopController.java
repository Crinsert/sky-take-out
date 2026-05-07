package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺状态管理")
public class AdminShopController {
    @Autowired
    private RedisTemplate redisTemplate;

    final String key = "SHOP_STATUS";
   @GetMapping("status")
   @ApiOperation("查询店铺状态")
    public Result get() {
       Integer shopStatus = (Integer) redisTemplate.opsForValue().get(key);
       log.info("查询店铺状态:{}",shopStatus==1?"营业中":"打烊中");
       return Result.success(shopStatus);
    }
    @ApiOperation("设置店铺营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {
       log.info("设置店铺营业状态:{}",status==1?"营业中":"打烊中");
       redisTemplate.opsForValue().set(key,status);
       return Result.success();
    }
}
