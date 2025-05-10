package com.sky.service;

import java.util.regex.Pattern;

/**
 * Redis缓存服务接口
 */
public interface RedisCacheService {
    /**
     * 清理指定缓存
     * @param pattern 缓存的正则表达式
     */
    void cleanCache(String pattern);
}
