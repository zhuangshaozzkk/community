package com.zzkk.community.controller;

import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.entity.Page;
import com.zzkk.community.service.ElasticsearchService;
import com.zzkk.community.service.LikeService;
import com.zzkk.community.service.UserService;
import com.zzkk.community.util.CommunityConstant;
import javafx.concurrent.Worker;
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
 * @ClassName SearchController
 * @Description Todo
 **/
@Controller
public class SearchController implements CommunityConstant {
    @Resource
    private ElasticsearchService elasticsearchService;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        // 搜索帖子
        org.springframework.data.domain.Page<DiscussPost> result = elasticsearchService.searchDiscussPost(keyword, page.getCurrentPage() - 1, page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(result!=null){
            for (DiscussPost post : result) {
                Map<String,Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user",userService.findUserById(post.getUserId()));
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword", keyword);
        page.setPath("/search?keyword="+keyword);
        page.setTotal(result == null ?0:result.getNumberOfElements());
        return "/site/search";
    }
}
