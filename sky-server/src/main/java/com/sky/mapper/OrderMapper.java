package com.sky.mapper;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Select("select * from orders where id=#{id} ")
    Orders list(Long id);

    @Select("select * from orders where user_id= #{currentId}")
    List<Orders> listUser(Long currentId);

    @Delete("delete from orders where id=#{id}")
    void delete(Long id);

    @Select("select  * from orders where status=#{status} and order_time< #{time}")
    List<Orders> overtimeOrders(Integer status, LocalDateTime time);

    Integer countByMap(Map map);


    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);
    @Select("select * from orders where id=#{outTradeNo} and user_id=#{userId}")
    Orders getByNumberAndUserId(String outTradeNo, Long userId);

    List<Orders> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    @Update("update orders set status=#{status} where id=#{id}")
    void delivery(OrdersDTO ordersDTO);


    void reject(OrdersRejectionDTO orders);


    OrderStatisticsVO statistics();
}
