package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceimpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;

    @Override
    @ApiOperation("营业额统计接口")
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Double tu = reportMapper.turnoverStatisticsBatch(beginTime, endTime,Orders.COMPLETED);
            tu = tu == null ? 0.0 : tu;
            turnoverList.add(tu);
        }

        List<String> dateStrList = dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateStrList))
                .turnoverList(turnoverList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .build();
    }

    @Override
    @ApiOperation("用户统计接口")
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        log.info("查询用户统计");
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //查询新用户//总用户
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer newUser = reportMapper.newUserStatistics(beginTime, endTime);
            newUser = newUser == null ? 0 : newUser;
            newUserList.add(newUser);
            Integer total = reportMapper.totalUserStatistics(beginTime);
            total = total == null ? 0 : total;
            totalUserList.add(total);
        }
        List<String> dateStrList = dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
        return UserReportVO.builder()
                .dateList(String.join(",", dateStrList))
                .newUserList(newUserList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .totalUserList(totalUserList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .build();

    }

    @Override
    @ApiOperation("订单统计接口")
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        log.info("查询订单统计");
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer>  validOrderCount = new ArrayList<>();
        for (LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询订单总数
            Integer orderCount=reportMapper.orderCountList(beginTime,endTime,null);
            //查询有效订单
            Integer validCount=reportMapper.orderCountList(beginTime,endTime,Orders.COMPLETED);
           orderCountList.add(orderCount);
           validOrderCount.add(validCount);
        }
        Integer count = orderCountList.stream().reduce(Integer::sum).get();
        //有效订单数和
        Integer sum = validOrderCount.stream().reduce(Integer::sum).get() ;
        //订单完成率
        Double orderCompletionRate = 0.0;
        if (count != 0) {
            orderCompletionRate = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        return OrderReportVO.builder()
                .dateList(dateList.stream()
                        .map(LocalDate::toString)
                        .collect(Collectors.joining(",")))
                .orderCountList(orderCountList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .validOrderCountList(validOrderCount.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .totalOrderCount(count)
                .validOrderCount(sum)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


    @Override
    @ApiOperation("查询top10")
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        log.info("查询top10");
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        Map<String, Integer> salesMap = new HashMap<>();
        for (LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            List<GoodsSalesDTO> dailyList = reportMapper.orderSaleList(beginTime, endTime, Orders.COMPLETED);

            if (dailyList != null && !dailyList.isEmpty()) {
                for (GoodsSalesDTO item : dailyList) {
                    if (item == null) {
                        continue;
                    }
                    String name = item.getName();
                    Integer number = item.getNumber();

                    if (name != null) {
                        int safeNumber = number == null ? 0 : number;
                        salesMap.merge(name, safeNumber, Integer::sum);
                    }
                }
            }
        }

        List<Map.Entry<String, Integer>> sortedList = salesMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        List<String> namelist = sortedList.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        List<Integer> numberList = sortedList.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", namelist))
                .numberList(numberList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .build();
    }
}
