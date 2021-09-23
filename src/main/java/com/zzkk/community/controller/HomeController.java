package com.zzkk.community.controller;

import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.entity.Page;
import com.zzkk.community.entity.User;
import com.zzkk.community.service.DiscussPostService;
import com.zzkk.community.service.LikeService;
import com.zzkk.community.service.UserService;
import com.zzkk.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zzkk
 * @ClassName HomeController
 * @Description 示例控制器
 **/
@Controller
public class HomeController implements CommunityConstant {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private UserService userService;
    @Resource
    private LikeService likeService;

    // 处理访问首页请求
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        page.setTotal(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<Map<String, Object>> list = new ArrayList<>();
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        // 将每一个discussPost取出，将用户ID对应的用户取出存在map里，再包装成list返回
        if (discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", discussPost);
                // 用户
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);
                // 帖子点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount",likeCount);
                list.add(map);
            }
        }
        model.addAttribute("discussPosts", list);
        // 方法调用前，SpringMVC会自动化实例Model和Page，并将page注入到model
        // model.addAttribute("page",page);
        return "/index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }
}
