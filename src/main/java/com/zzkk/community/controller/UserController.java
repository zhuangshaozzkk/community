package com.zzkk.community.controller;

import com.zzkk.community.entity.User;
import com.zzkk.community.service.UserService;
import com.zzkk.community.util.CommunityUtil;
import com.zzkk.community.util.HostHolder;
import javafx.geometry.Pos;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author zzkk
 * @ClassName UserController
 * @Description Todo
 **/
@Controller
@RequestMapping(path = "/user")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.upload}")
    private String uploadPath;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        // 判断是否有上传图片
        if (headerImage == null) {
            model.addAttribute("error", "你没有上传图片");
            return "/site/setting";
        }
        // 获取图片名称
        String filename = headerImage.getOriginalFilename();
        // 获取图片名称后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 判断图片后缀是否合法
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不对");
            return "/site/setting";
        }
        // 生成随机文件名
        filename = CommunityUtil.generateRandString() + suffix;
        // 确定文件存放的位置(全限定名)
        File dest = new File(uploadPath + "/" + filename);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }
        // 更新当前用户头像路径(WEB路径)
        // http://localhost:8080/community/user/header/xxx.png
        // 获取用户
        User user = hostHolder.getUser();
        // 拼接路径
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        // 更新用户路径
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    /**
     * @Description //将服务器上的图片输出返回
     **/
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 文件真实存放路径
        fileName = uploadPath + "/" + fileName;
        // 设置响应图片返回格式
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        response.setContentType("img/" + suffix);
        // 获取字节流
        try (ServletOutputStream os = response.getOutputStream();
             // 获取文件输入流
             FileInputStream fileInputStream = new FileInputStream(fileName);
             BufferedInputStream bis = new BufferedInputStream(fileInputStream);
        ) {
            // 缓冲数组
            byte[] buffer = new byte[1024];
            int len = 0;
            // 每次读取1024字节
            while ((len = bis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            LOGGER.error("读取头像失败：" + e.getMessage());
        }
    }

    @RequestMapping(path = "/password", method = RequestMethod.POST)
    public String updatePassword(Model model,String oldPassword , String newPassword,String confirmPassword){
        User user = hostHolder.getUser();
        String password = user.getPassword();
        if(!oldPassword.equals(password)){
            model.addAttribute("oldPasswordLengthMsg","密码不正确");
            return "/site/setting";
        }

        if(newPassword.length()<8){
            model.addAttribute("newPasswordLengthMsg","密码长度不能小于8位!");
            return "/site/setting";
        }

        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("unlikeMsg","两次输入的密码不一致!");
            return "/site/setting";
        }

        userService.updatePassword(user.getId(),newPassword);
        return "redirect:/index";
    }
}
