package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @GetMapping("conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索", ordersPageQueryDTO);
        PageResult pageResult =orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping("delivery/{id}")
    @ApiOperation("订单派送")
    public Result delivery(@PathVariable("id") Long id) {
        OrdersDTO ordersDTO = new OrdersDTO();
        ordersDTO.setNumber(String.valueOf( id));
        ordersDTO.setStatus(3);
        orderService.delivery(ordersDTO);
        return Result.success();
    }

    @GetMapping("details/{id}")
    @ApiOperation("订单详情")
    public Result<OrdersDTO> details(@PathVariable("id") Long id) {
        OrdersDTO ordersDTO = orderService.detail(id);
        return Result.success(ordersDTO);
    }

    @PutMapping("confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody Long id) {
        OrdersDTO ordersDTO = new OrdersDTO();
        ordersDTO.setNumber(String.valueOf( id));
        ordersDTO.setStatus(2);
        orderService.delivery(ordersDTO);
        return Result.success();
    }

    @PutMapping("rejection")
    @ApiOperation("拒绝订单")
    public Result rejection(@RequestBody OrdersRejectionDTO orders) {
        orderService.reject(orders);
        return Result.success();
    }
    @PutMapping("complete")
    @ApiOperation("完成订单")
    public Result complete(@RequestBody Long id) {
        OrdersDTO ordersDTO = new OrdersDTO();
        ordersDTO.setNumber(String.valueOf( id));
        ordersDTO.setStatus(4);
        orderService.delivery(ordersDTO);
        return Result.success();
    }

    @PutMapping("cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO  orders) {
        OrdersRejectionDTO ordersRejectionDTO = new OrdersRejectionDTO();
        ordersRejectionDTO.setId(orders.getId());
        ordersRejectionDTO.setRejectionReason(orders.getCancelReason());
        orderService.reject(ordersRejectionDTO);
        return Result.success();
    }

    @GetMapping("statistics")
    @ApiOperation("统计个个状态订单数")
    public Result statistics() {
       OrderStatisticsVO orderStatisticsVO =orderService.statistics();
       return Result.success(orderStatisticsVO);
    }
}
