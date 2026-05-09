package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderDetileMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCarMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.soceket.WebSocketServer;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class OrderServiceimpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetileMapper orderDetileMapper;
    @Autowired
    private ShoppingCarMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @ApiOperation("订单搜索")
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        List<Orders> orders = orderMapper.conditionSearch(ordersPageQueryDTO);
        Page<Orders> page = (Page<Orders>) orders;
        return new PageResult(page.getTotal(),page.getResult());
    }



    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersDTO ordersDTO) {
        //处理业务异常
        /**
         * 业务逻辑
         * 设置支付时间
         * 查询订单详情
         * 插入数据库
         */
        //地址为空或者购物车为空
        AddressBook book = addressBookService.getById(ordersDTO.getAddressBookId());
        if (book==null) {
            throw new AddressBookBusinessException("地址为空");
        }
        Long id = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartMapper.list(id);
        if (list == null || list.size() == 0) {
            throw new AddressBookBusinessException("购物车为空");
        }
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(book.getPhone());
        orders.setConsignee(book.getConsignee());
        orders.setAddress(book.getDetail());
        orders.setUserId(id);
        orderMapper.insert(orders);
        //向订单明细表插入数据
      List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : list)
        {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetileMapper.insertBatch(orderDetails);
        //清空购物车
        shoppingCartMapper.deleteByUserId(id);
        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .orderNumber(ordersDTO.getNumber())
                .orderAmount(ordersDTO.getAmount())
                .orderTime(ordersDTO.getOrderTime())
                .id(orders.getId())
                .build();
        return submitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public void repetition(Long id) {
        //根据订单查询上次订单信息
        Orders order=orderMapper.list(id);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(Orders.PENDING_PAYMENT);
        orderMapper.update(order);
    }

    @Override
    public PageResult history(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        List<Orders> orders = orderMapper.listUser(BaseContext.getCurrentId());
        Page<Orders> pagelist = (Page<Orders>) orders;
        return new PageResult(pagelist.getTotal(),pagelist.getResult());
    }

    @Transactional
    @Override
    public void cancel(Long id) {
        //根据订单号查询订单
        Orders orders = orderMapper.list(id);
        //更新订单信息
        orderDetileMapper.deleteByorderId(id);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orders.setPayStatus(Orders.REFUND);
        orderMapper.insert(orders);
        //退款
    }

    @Override
    public OrdersDTO detail(Long id) {
        Orders orders = orderMapper.list(id);
        OrdersDTO ordersDTO = new OrdersDTO();
        BeanUtils.copyProperties(orders,ordersDTO);
        ordersDTO.setOrderDetails(orderDetileMapper.listByorderId(id));
        return ordersDTO;
    }

    @Override
    public List<Orders> overtimeOrders(Integer status, LocalDateTime time) {
       List<Orders>ordersList= orderMapper.overtimeOrders(status,time);
       return ordersList;
    }

    @Override
    public void update(Orders orders) {
        orderMapper.update(orders);
    }

    @Override
    public void notifys(Long id) {
        Orders orders = orderMapper.list(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "2");//来单提醒 1 催单 2
        map.put("orderId", String.valueOf(id));
        map.put("content", "订单号:" + id);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    //派送订单
    @Override
    public void delivery(OrdersDTO ordersDTO) {
        orderMapper.delivery(ordersDTO);
    }

    @Override
    public void reject(OrdersRejectionDTO orders) {
        Orders ordersrejection = new Orders();
        ordersrejection.setId(orders.getId());
        ordersrejection.setRejectionReason(orders.getRejectionReason());
        ordersrejection.setStatus(Orders.CANCELLED);
        ordersrejection.setCancelTime(LocalDateTime.now());
        orderMapper.reject(orders);
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO =orderMapper.statistics();
        return orderStatisticsVO;
    }
}
