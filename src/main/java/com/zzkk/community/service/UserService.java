package com.zzkk.community.service;

import com.zzkk.community.dao.UserMapper;
import com.zzkk.community.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzkk
 * @ClassName UserService
 * @Description Todo
 **/
@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    public User findUserById(int userId){
        return userMapper.selectById(userId);
    }
}
