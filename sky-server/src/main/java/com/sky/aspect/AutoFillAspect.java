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
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自动填充切面类,切点为所有的加了AutoFill注解的mapper接口,自动填充创建人、创建时间、修改人、修改时间
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void doFill(JoinPoint joinPoint){
        log.info("自动填充切面开始执行");

        //获取被拦截的方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        //获取被拦截的方法上的AutoFill注解
        AutoFill annotation = method.getAnnotation(AutoFill.class);
        //获取AutoFill注解的值(操作类型)
        OperationType value = annotation.value();

        //获取被拦截的方法的参数
        Object[] args = joinPoint.getArgs();
        //获取被拦截的方法的参数类型
        Class<?> aClass = args[0].getClass();

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据参数类型获得setUprateUser、setUprateTime、setCreateUser、setCreateTime方法
        if(OperationType.INSERT.equals(value)){
            //插入操作
            try {
                Method setCreateUser = aClass.getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setCreateTime = aClass.getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                setCreateUser.invoke(args[0], currentId);
                setCreateTime.invoke(args[0], now);
                Method setUpdateUser = aClass.getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = aClass.getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateUser.invoke(args[0], currentId);
                setUpdateTime.invoke(args[0], now);
            } catch (Exception e) {
                log.error("自动填充插入操作异常", e);
            }
        }else if(OperationType.UPDATE.equals(value)){
            //更新操作
            try {
                Method setUpdateUser = aClass.getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = aClass.getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateUser.invoke(args[0], currentId);
                setUpdateTime.invoke(args[0], now);
            } catch (Exception e) {
                log.error("自动填充更新操作异常", e);
            }
        }
    }
}
