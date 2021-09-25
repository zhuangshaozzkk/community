package com.zzkk.community.controller;

import com.zzkk.community.entity.Event;
import com.zzkk.community.entity.Page;
import com.zzkk.community.entity.User;
import com.zzkk.community.event.EventProducer;
import com.zzkk.community.service.FollowService;
import com.zzkk.community.service.UserService;
import com.zzkk.community.util.CommunityConstant;
import com.zzkk.community.util.CommunityUtil;
import com.zzkk.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author zzkk
 * @ClassName FollowController
 * @Description Todo
 **/
@Controller
public class FollowController implements CommunityConstant {
    @Resource
    private FollowService followService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @Resource
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        followService.follow(hostHolder.getUser().getId(),entityType,entityId);

        // 触发点赞事件
            Event follow = new Event()
                    .setTopic("follow")
                    .setEntityId(entityType)
                    .setEntityId(entityId)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityUserId(entityId);
            eventProducer.fireEvent(follow);

        return CommunityUtil.getJSONString(0,"已关注！");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        followService.unfollow(hostHolder.getUser().getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注！");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("followees/"+userId);
        page.setTotal((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        model.addAttribute("users",userList);
        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("followers/"+userId);
        page.setTotal((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));
        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        model.addAttribute("users",userList);
        return "/site/follower";
    }
}
