package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public void export(HttpServletResponse response) {
        LocalDate now = LocalDate.now();
        LocalDate begin = now.minusDays(30);
        LocalDate end = now.minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        InputStream resource = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        if (resource == null) {
            throw new RuntimeException("模板文件不存在，请联系管理员");
        }
        try {
            XSSFWorkbook  excel=new XSSFWorkbook(resource);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            // 创建居中样式
            XSSFCellStyle centerStyle = excel.createCellStyle();
            centerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            centerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);

            //设置统筹日期并居中
            XSSFCell titleCell = sheet.getRow(1).getCell(1);
            titleCell.setCellValue("时间:"+begin+"至"+end);
            titleCell.setCellStyle(centerStyle);

            //设置概览数据
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());
            //设置详情数据
            for (int i = 0; i < 30; i++) {
                BusinessDataVO businessData1 = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(begin, LocalTime.MAX));
                sheet.getRow(i+7).getCell(1).setCellValue(String.valueOf(begin));
                sheet.getRow(i+7).getCell(2).setCellValue(businessData1.getTurnover());
                sheet.getRow(i+7).getCell(3).setCellValue(businessData1.getValidOrderCount());
                sheet.getRow(i+7).getCell(4).setCellValue(businessData1.getOrderCompletionRate());
                sheet.getRow(i+7).getCell(5).setCellValue(businessData1.getUnitPrice());
                sheet.getRow(i+7).getCell(6).setCellValue(businessData1.getNewUsers());
                begin = begin.plusDays(1);
            }
            //导出数据
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
