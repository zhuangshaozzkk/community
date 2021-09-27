package com.zzkk.community.service;

import com.zzkk.community.dao.LoginTicketMapper;
import com.zzkk.community.dao.UserMapper;
import com.zzkk.community.entity.LoginTicket;
import com.zzkk.community.entity.User;
import com.zzkk.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.time.chrono.IsoEra;
import java.util.*;

/**
 * @author zzkk
 * @ClassName UserService
 * @Description Todo
 **/
@Service
public class UserService implements CommunityConstant {
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Resource
    private TemplateEngine templateEngine;

    // 邮件客户端
    @Resource
    private MailClient mailClient;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

//    @Resource
//    private LoginTicketMapper loginTicketMapper;


    /**
     * @Description //根据用户id查找用户
     **/
    public User findUserById(int userId) {
        User user = getCache(userId);
        if(user == null){
            user = initCache(userId);
        }
        return user;
//        return userMapper.selectById(userId);
    }

    /**
     * @return 返回map String错误注释，object错误信息
     * @Description //注册用户
     **/
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 判断传入user的username，password，email是否为空
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        // 验证账号
        User selectUser = userMapper.selectByName(user.getUsername());
        if (selectUser != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }
        // 验证邮箱
        selectUser = userMapper.selectByEmail(user.getEmail());
        if (selectUser != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }
        // 注册用户，将用户信息存入数据库
        // 设置盐
        user.setSalt(CommunityUtil.generateRandString().substring(0, 5));
        // 密码md5加密
        String md5Password = CommunityUtil.md5(user.getPassword() + user.getSalt());
        user.setPassword(md5Password);
        // 设置账号的状态0（未激活），类型0（普通用户），激活码
        user.setStatus(0);
        user.setType(0);
        user.setActivationCode(CommunityUtil.generateRandString());
        // 设置初始头像地址 images.nowcoder.com/head/1t.png [0-1000]
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1001)));
        user.setCreateTime(new Date());
        // 将用户信息插入到数据库
        userMapper.insertUser(user);

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 设置激活链接 http://localhost:8080/commmunity/activation/id/activationCode
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        // 用模板引擎主动生成内容（HTML）
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    /**
     * @Description //激活用户账号 返回激活的状态  当用户status == 1 且激活码匹配才激活成功
     **/
    public int activation(int userId, String activationCode) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * @return 包含登录失败信息（如果有的话），凭证
     * @Description //登录账号
     * @Param 账号，密码，凭证超时秒数
     **/
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if (!password.equals(user.getPassword())) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }
        //生成凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateRandString());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisTicket = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisTicket, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * @Description //退出账号
     **/
    public void logout(String ticket) {
//        loginTicketMapper.updateByTicket(ticket,1);
        String redisTicket = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisTicket);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisTicket, loginTicket);
    }

    /**
     * @Description // 根据ticket查询用户的凭证
     **/
    public LoginTicket findLoginTicketByTicket(String ticket) {
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        String redisTicket = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisTicket);
        return loginTicket;
    }

    /**
     * @Description //根据id修改用户头像url
     **/
    public int updateHeader(int userId, String headerUrl) {
        int row = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return row;
    }

    /**
     * @Description //修改密码
     **/
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        // 空值判断
        if (StringUtils.isBlank(newPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }
        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (user == null || !user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }
        // 更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        clearCache(userId);
        return map;
    }

    public User findUserByName(String name) {
        return userMapper.selectByName(name);
    }

    // 优先从缓存中取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        User user = (User) redisTemplate.opsForValue().get(userKey);
        return user;
    }

    // 取不到初始化，初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user);
        return user;
    }

    // 数据变更时，清除缓存数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
