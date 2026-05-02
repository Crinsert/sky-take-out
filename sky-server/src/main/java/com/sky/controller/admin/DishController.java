package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result<Dish> save(@RequestBody DishDTO dishDTO) {
        log.info("接收到的菜品数据：{}", dishDTO);
        log.info("菜品名称 name：{}", dishDTO.getName());
        log.info("菜品分类 categoryId：{}", dishDTO.getCategoryId());
        dishService.save(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品管理分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品管理分页查询");
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @PostMapping("/status/{status}")
    @ApiOperation("菜品状态禁用/启用")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("{} {}", status, id);
        dishService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("编辑菜品：{}", dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }


    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品：{}", ids);
        dishService.delete(ids);
        return Result.success();
    }


}
