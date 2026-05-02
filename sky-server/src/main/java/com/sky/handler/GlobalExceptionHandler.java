package com.sky.handler;

import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    // ... existing code ...
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("异常信息：{}", ex.getMessage());
        String message = ex.getMessage();

        // 1. 区分“非空约束”和“唯一性约束”
        if (message.contains("cannot be null")) {
            // 提取为空的列名，例如 Column 'name' cannot be null
            try {
                int start = message.indexOf("'") + 1;
                int end = message.indexOf("'", start);
                String columnName = message.substring(start, end);
                return Result.error("必填项 [" + columnName + "] 不能为空");
            } catch (Exception e) {
                return Result.error("必填项不能为空");
            }
        }

        // 2. 处理“数据已存在”的情况
        if (message.contains("Duplicate entry")) {
            String duplicateValue = "";
            try {
                int startIndex = message.indexOf("'") + 1;
                int endIndex = message.indexOf("'", startIndex);
                duplicateValue = message.substring(startIndex, endIndex);
            } catch (Exception e) {
                log.error("提取重复值失败", e);
            }

            if (message.contains("employee") || message.contains("username")) {
                return Result.error("用户名 " + duplicateValue + " 已存在");
            } else if (message.contains("dish") || message.contains("idx_dish")) {
                return Result.error("菜品名称 " + duplicateValue + " 已存在");
            } else if (message.contains("setmeal") || message.contains("idx_setmeal")) {
                return Result.error("套餐名称 " + duplicateValue + " 已存在");
            } else if (message.contains("category") || message.contains("idx_category")) {
                return Result.error("分类名称 " + duplicateValue + " 已存在");
            } else {
                return Result.error("数据 " + duplicateValue + " 已存在");
            }
        }

        // 3. 兜底处理
        return Result.error("数据库操作失败，请检查数据格式");
    }
}
