package com.zzkk.community.util;

import com.zzkk.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author zzkk
 * @ClassName HostHolder
 * @Description 持有用户信息，用于代替session对象
 **/
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
