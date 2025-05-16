package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openid}")
    User selectOneByOpenid(String openid);

    void insert(User user);

    /**
     * 查询某日新增用户数量
     * @param startTime 当日开始时间
     * @param endTime 当日结束时间
     * @return 新增用户数量
     */
    Integer selectDayNewUserCount(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询截止到某日的所有用户数量
     * @param endTime 截止时间
     * @return 所有用户数量
     */
    Integer selectAllUserCount(LocalDateTime endTime);
}
