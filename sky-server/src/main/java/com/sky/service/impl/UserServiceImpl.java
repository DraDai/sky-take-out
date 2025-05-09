package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatLoginUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {


    private final WeChatLoginUtil weChatLoginUtil;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(WeChatProperties weChatProperties, WeChatLoginUtil weChatLoginUtil, UserMapper userMapper) {
        this.weChatLoginUtil = weChatLoginUtil;
        this.userMapper = userMapper;
    }

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = weChatLoginUtil.getOpenid(userLoginDTO.getCode());

        //如果openid为空，说明登录失败
        if(openid == null || openid.isEmpty()){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.selectOneByOpenid(openid);
        if(user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }


}
