package com.zzkk.community;

import com.zzkk.community.dao.DiscussPostMapper;
import com.zzkk.community.dao.LoginTicketMapper;
import com.zzkk.community.dao.UserMapper;
import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.entity.LoginTicket;
import com.zzkk.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author zzkk
 * @ClassName CRUDTest
 * @Description Todo
 **/

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CRUDTest {
    @Resource
    private LoginTicketMapper loginTicketMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private DiscussPostMapper discussPostMapper;
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
    @Test
    public void testSelectPost(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 20);
        for (DiscussPost post : discussPosts) {
            System.out.println(post);
        }
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1);
        loginTicket.setStatus(0);
        loginTicket.setTicket("qqq");
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    public void testSelectLoginTicketAndUpdate(){
        LoginTicket qqq = loginTicketMapper.selectByTicket("qqq");
        System.out.println(qqq);
        loginTicketMapper.updateByTicket("qqq",1);
        LoginTicket qqqq = loginTicketMapper.selectByTicket("qqq");
        System.out.println(qqqq);
    }
}
