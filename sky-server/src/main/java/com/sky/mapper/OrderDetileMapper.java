package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetileMapper {

    void insertBatch(List<OrderDetail> list);

    @Delete("delete from order_detail where order_id=#{id}")
    void deleteByorderId(Long id);

    @Select("select * from order_detail where order_id=#{id}")
    List<OrderDetail> listByorderId(Long id);
}
