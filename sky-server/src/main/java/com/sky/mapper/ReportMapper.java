package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReportMapper {

    Integer turnoverStatistics(LocalDateTime begin, LocalDateTime end);

    Double turnoverStatisticsBatch(@Param("beginTime") LocalDateTime beginTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   @Param("completed") Integer completed);

    Integer newUserStatistics(@Param("beginTime") LocalDateTime beginTime,
                              @Param("endTime") LocalDateTime endTime);

    Integer totalUserStatistics(LocalDateTime beginTime);

    Integer orderCountList(@Param("beginTime") LocalDateTime beginTime,
                           @Param("endTime") LocalDateTime endTime,
                           @Param("completed") Integer completed);

    List<GoodsSalesDTO> orderSaleList(
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("completed") Integer completed
    );
}
