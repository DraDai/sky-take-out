package com.sky.exception;

/**
 * 菜品不存在异常
 */
public class DishNotFoundException extends BaseException{
    public DishNotFoundException(String msg) {
        super(msg);
    }

}
