package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/shop")
@Slf4j
@Api(tags = "用户店铺状态查询")
public class UserShopController {
    @Autowired
    private RedisTemplate redisTemplate;

    final String key = "SHOP_STATUS";
   @GetMapping("/status")
   @ApiOperation("查询店铺状态")
    public Result get() {
       Integer shopStatus = (Integer) redisTemplate.opsForValue().get(key);
       log.info("查询店铺状态:{}",shopStatus==1?"营业中":"打烊中");
       return Result.success(shopStatus);
    }
}
