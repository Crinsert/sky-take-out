package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {
    @Autowired
    private ReportService reportService;
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计接口")
    public Result ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
            ){
        log.info("订单统计");
        OrderReportVO orderReportVO=reportService.ordersStatistics(begin,end);
        return Result.success(orderReportVO);
    }

    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计接口")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end)
    {
        log.info("营业额统计");
        TurnoverReportVO vo =reportService.turnoverStatistics(begin,end);
        return Result.success(vo);
    }
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计接口")
    public Result userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end)
    {
        log.info("用户统计");
       UserReportVO vo = reportService.userStatistics(begin,end);
       return Result.success(vo);
    }
    @GetMapping("/top10")
    @ApiOperation("查询top10")
    public Result top10(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end)
    {
        log.info("查询top10");
        SalesTop10ReportVO vo =reportService.top10(begin,end);
        return Result.success(vo);
    }
}
