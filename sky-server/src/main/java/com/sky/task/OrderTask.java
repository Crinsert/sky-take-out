package com.sky.task;

import com.sky.entity.Orders;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrderService orderService;

    public void removeTimeoutOrders(){
        log.info("开始处理超时订单");
    }
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单: {}", java.time.LocalDateTime.now());
        //查询超时订单
        LocalDateTime time = LocalDateTime.now();
        time=time.plusMinutes(-15);
        List<Orders> ordersList=orderService.overtimeOrders(Orders.UN_PAID,time);
        if (ordersList != null && ordersList.size() > 0) {
            //处理订单
            for (Orders orders : ordersList) {
              orders.setCancelTime(LocalDateTime.now());
              orders.setCancelReason("订单超时未支付,已自动取消");
              orders.setStatus(Orders.CANCELLED);
              orderService.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupData() {
        log.info("定时清理数据: {}", java.time.LocalDateTime.now());
        //清理已完成订单
        LocalDateTime  time = LocalDateTime.now().plusHours(-1);
        List<Orders> ordersList = orderService.overtimeOrders(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderService.update(orders);
            }
        }
    }
}
