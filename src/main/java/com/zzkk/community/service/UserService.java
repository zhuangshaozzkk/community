package com.zzkk.community.service;

import com.zzkk.community.dao.LoginTicketMapper;
import com.zzkk.community.dao.UserMapper;
import com.zzkk.community.entity.LoginTicket;
import com.zzkk.community.entity.User;
import com.zzkk.community.util.CommunityConstant;
import com.zzkk.community.util.CommunityUtil;
import com.zzkk.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    private LoginTicketMapper loginTicketMapper;

    /**
     * @Description //根据用户id查找用户
     **/
    public User findUserById(int userId) {
        return userMapper.selectById(userId);
    }

    /**
     * @Description //激活用户账号 当用户status == 1 且激活码匹配才激活成功
     **/
    public int activation(int userId, String activationCode) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
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
        // 判断传入user的username，password， email是否为空
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
        // 注册用户
        // 设置盐
        user.setSalt(CommunityUtil.generateRandString().substring(0, 5));
        // 密码md5加密
        String md5Password = CommunityUtil.md5(user.getPassword() + user.getSalt());
        user.setPassword(md5Password);
        // 设置账号的状态（未激活），类型（普通用户），激活码
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
     * @Description //登录账号
     * @Param 账号，密码，凭证超时秒数
     * @return 包含登录失败信息（如果有的话），凭证
     **/
    public Map<String, Object> login(String username,String password,long expiredSeconds){
        Map<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","该账号未激活！");
            return map;
        }
        password = CommunityUtil.md5(password+user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("passwordMsg","密码不正确！");
            return map;
        }
        //生成凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateRandString());
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * @Description //退出账号
     **/
    public void logout(String ticket){
        loginTicketMapper.updateByTicket(ticket,1);
    }

    /**
     * @Description //查询用户的凭证
     **/
    public LoginTicket findLoginTicketByTicket(String ticket){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        return loginTicket;
    }

    /**
     * @Description //根据id修改用户头像
     **/
    public int updateHeader(int userId,String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }

    /**
     * @Description //修改密码
     **/
    public int updatePassword(int userId,String newPassword){
        return userMapper.updatePassword(userId,newPassword);
    }
}
