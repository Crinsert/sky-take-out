package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@Slf4j
@Component
@Aspect
public class AutoFillAspect {
    @Pointcut(" @annotation(com.sky.annotation.AutoFill)&&execution(* com.sky.mapper.*.*(..)) ")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行数据填充");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取签名
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);//获取实体类的注释
        OperationType value = annotation.value();
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();
        //获取实体
        Object[] args = joinPoint.getArgs();
        Object object = args[0];
        if (value == OperationType.INSERT){
            try {
                object.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class).invoke(object, now);
                object.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(object, now);
                object.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class).invoke(object, id);
                object.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(object, id);
                //通过反射获取set方法
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (value == OperationType.UPDATE){
            try {
                object.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(object, now);
                object.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(object, id);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
