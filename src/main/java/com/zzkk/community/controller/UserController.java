package com.zzkk.community.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.zzkk.community.annotation.LoginRequired;
import com.zzkk.community.entity.User;
import com.zzkk.community.service.FollowService;
import com.zzkk.community.service.LikeService;
import com.zzkk.community.service.UserService;
import com.zzkk.community.util.CommunityConstant;
import com.zzkk.community.util.CommunityUtil;
import com.zzkk.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.surround.query.SrndPrefixQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author zzkk
 * @ClassName UserController
 * @Description Todo
 **/
@Controller
@RequestMapping(path = "/user")
public class UserController implements CommunityConstant {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    @Resource
    private FollowService followService;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qinniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qinniu.bucket.header.url}")
    private String headerBucketUrl;

    // 需要登录才能访问
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 上传文件的名称
        String filename = CommunityUtil.generateRandString();
        // 设置相应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, filename, 3600, policy);
        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName", filename);
        return "/site/setting";
    }

    // 更新头像路径
    @RequestMapping(path = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空");
        }

        String url = headerBucketUrl +"/"+fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);
        return CommunityUtil.getJSONString(0);
    }


    // 废弃
    @LoginRequired
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

    // 废弃
    /**
     * @Description // 访问头像
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

    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(Model model,String oldPassword , String newPassword){
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if(map == null || map.isEmpty()){
            return "redirect:/logout";
        }else{
            model.addAttribute("oldMsg",map.get("oldPasswordMsg"));
            model.addAttribute("newMsg",map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}" , method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该用户不存在！");
        }
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        // 是否已关注
        boolean isFollowed = false;
        if(hostHolder.getUser() != null){
            isFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("isFollowed",isFollowed);
        return "/site/profile";
    }
}
