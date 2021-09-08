package com.zzkk.community;

import com.zzkk.community.dao.UserMapper;
import com.zzkk.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author zzkk
 * @ClassName CRUDTest
 * @Description Todo
 **/

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CRUDTest {
    @Resource()
    private UserMapper userMapper;
    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(12);

        User user1 = userMapper.selectByName("fff");

        User user2 = userMapper.selectByEmail("nowcoder138@sina.com");
        System.out.println(user+"\n"+user1+"\n"+user2);
    }
    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("kkk");
        user.setEmail("123@aa.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int row = userMapper.insertUser(user);
        System.out.println(row+"\n"+user.getId());
    }
    @Test
    public void testUpdateUser(){
        int row = userMapper.updateHeader(150, "http://www.nowcoder.com/111.png");
        System.out.println(row);
        row = userMapper.updatePassword(150,"654321");
        System.out.println(row);
        row = userMapper.updateStatus(150,1);
        System.out.println(row);
    }
}
