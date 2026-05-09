package com.sky.controller.user;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController("UserOrderController")
@RequestMapping("user/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderservice;

    @PostMapping("submit")
    @ApiOperation("用户下单")
    public Result ordersub(@RequestBody OrdersDTO ordersDTO) {
        OrderSubmitVO submitVO = orderservice.submit(ordersDTO);
        return Result.success(submitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
       OrderPaymentVO orderPaymentVO = orderservice.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Long id){
        orderservice.notifys(id);
        return Result.success("用户催单:"+id);
    }

    @PostMapping("repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        orderservice.repetition(id);
        return Result.success();
    }

    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    public Result history(OrdersPageQueryDTO ordersPageQueryDTO){
        PageResult pageResult= orderservice.history(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping("cancel/{id}")
    @ApiOperation("取消订单")
    public Result Cancel(@PathVariable Long id){
        orderservice.cancel(id);
        return Result.success();
    }

    @GetMapping("orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result orederDetail(@PathVariable Long id){
       OrdersDTO detail= orderservice.detail(id);
       return Result.success(detail);
    }
}
