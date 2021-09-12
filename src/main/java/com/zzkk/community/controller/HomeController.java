package com.zzkk.community.controller;

import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.entity.Page;
import com.zzkk.community.entity.User;
import com.zzkk.community.service.DiscussPostService;
import com.zzkk.community.service.UserService;
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
 * @Description Todo
 **/
@Controller
public class HomeController {
    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private UserService userService;

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
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);
                list.add(map);
            }
        }
        model.addAttribute("discussPosts", list);
        model.addAttribute("page",page);
        return "/index";
    }
}
