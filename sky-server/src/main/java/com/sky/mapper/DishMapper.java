package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {


    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    void startOrStop(Dish dish);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    void delete(List<Long> ids);

}
