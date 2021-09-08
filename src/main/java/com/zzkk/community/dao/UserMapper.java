package com.zzkk.community.dao;

import com.zzkk.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

//该接口类的实现类对象交给mybatis底层创建，然后交由Spring框架管理。
@Mapper()
public interface UserMapper {
    User selectById(Integer id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(Integer id, Integer status);

    int updateHeader(Integer id, String headerUrl);

    int updatePassword(Integer id, String password);
}
