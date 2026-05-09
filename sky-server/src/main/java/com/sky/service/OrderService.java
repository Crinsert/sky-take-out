package com.sky.service;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) ;

    OrderSubmitVO submit(OrdersDTO ordersDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    void repetition(Long id);

    PageResult history(OrdersPageQueryDTO ordersPageQueryDTO);

    void cancel(Long id);

    OrdersDTO detail(Long id);

    List<Orders> overtimeOrders(Integer status, LocalDateTime time);

    void update(Orders orders);

    void notifys(Long id);

    void delivery(OrdersDTO ordersDTO);

    void reject(OrdersRejectionDTO orders);

    OrderStatisticsVO statistics();

}
