package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
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

    @Delete("delete from dish where id={id}")
    void delete(Long id);

    List<Dish> listID(long categoryId);

    DishVO getById(Long id);

    List<Dish> deletelist(List<Long> ids);

    List<Dish> list(Dish dish);

    List<Dish> getBySetmealId(Long id);
}
