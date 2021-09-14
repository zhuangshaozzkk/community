package com.zzkk.community.service;

import com.zzkk.community.dao.AlphaDao;
import com.zzkk.community.dao.DiscussPostMapper;
import com.zzkk.community.dao.UserMapper;
import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.entity.User;
import com.zzkk.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author zzkk
 * @ClassName AlphaService
 * @Description 测试
 **/
@Service
public class AlphaService {
    @Resource
    private AlphaDao alphaDao;

    public String find(){
        return alphaDao.select();
    }

    @Resource
    private UserMapper userMapper;
    @Resource
    private DiscussPostMapper discussPostMapper;
    @Resource
    private TransactionTemplate transactionTemplate;

    // REQUIRED 支持当前事务，如果不存在则创建新事务
    // REQUIRES_NEW 创建一个新事务，并且暂停单前事务
    // NESTED 如果单前存在事务，则嵌套在该事务中执行
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save(){
        // 新增用户
        User user = new User();
        user.setUsername("zzkk");
        user.setId(150);
        user.setCreateTime(new Date());
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setActivationCode(CommunityUtil.generateRandString());
        user.setSalt(CommunityUtil.generateRandString().substring(0,5));
        user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(150);
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);

        Integer.valueOf("jadskf");
        return "ok";
    }

    public Object save1(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return "ok";
    }
}
