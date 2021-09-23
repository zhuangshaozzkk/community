package com.zzkk.community.controller;

import com.google.code.kaptcha.Producer;
import com.zzkk.community.entity.User;
import com.zzkk.community.service.UserService;
import com.zzkk.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * @author zzkk
 * @ClassName LoginController
 * @Description Todo
 **/
@Controller
public class LoginController implements CommunityConstant {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    @Value("{server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private Producer producer;

    /**
     * @Description // 跳转到登录页面
     **/
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * @Description // 跳转到注册页面
     **/
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * @Description // 提交注册数据
     **/
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        // 注册成功 跳转页面 提示去邮箱激活
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            // 添加错误信息
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * @Description // 处理账号激活链接
     **/
    @RequestMapping(path = "/activation/{id}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("id") int userId, @PathVariable("code") String activationCode) {
        int result = userService.activation(userId, activationCode);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，你的账号已经可以正常的使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_FAILURE) {
            model.addAttribute("msg", "激活失败，你提供的激活码不正确！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "无效操作，该账号已经激活过了！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * @Description // 获取验证码图片 使用response手动输出
     **/
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptchaImg(HttpServletResponse response, HttpSession session) {
        String text = producer.createText();
        // 将验证字符串存入session
        session.setAttribute("kaptcha", text);
        // 将验证码字符串生成图片
        BufferedImage image = producer.createImage(text);
        // 设置返回图片格式
        response.setContentType("image/png");
        // 浏览器输出图片
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            LOGGER.error("响应验证码失败："+e.getMessage());
        }
    }

    /**
     * @Description // 登录功能
     **/
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(Model model, String username, String password, String code, boolean rememberMe, HttpSession session, HttpServletResponse response){
        // 检查验证码
        String kaptcha = (String) session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }
        // 检查账号和密码
        int expiredSecond = rememberMe?REMEMBER_EXPIRED_SECOND:DEFAULT_EXPIRED_SECOND;
        Map<String, Object> map = userService.login(username, password, expiredSecond);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            // 设置访问有效路径
            cookie.setPath(contextPath);
            // 设置最大失效时间
            cookie.setMaxAge(expiredSecond);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * @Description // 退出账号
     **/
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}


