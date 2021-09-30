package com.zzkk.community.controller;

import com.zzkk.community.entity.Event;
import com.zzkk.community.entity.User;
import com.zzkk.community.event.EventProducer;
import com.zzkk.community.service.LikeService;
import com.zzkk.community.util.CommunityConstant;
import com.zzkk.community.util.CommunityUtil;
import com.zzkk.community.util.HostHolder;
import com.zzkk.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzkk
 * @ClassName LikeController
 * @Description Todo
 **/
@Controller
public class LikeController implements CommunityConstant {
    @Resource
    private LikeService likeService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private EventProducer eventProducer;
    @Resource
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        long entityLikeCount = likeService.findEntityLikeCount(entityType, entityId);
        int entityLikeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", entityLikeCount);
        map.put("likeStatus", entityLikeStatus);

        // 触发点赞事件
        if(entityLikeStatus == 1){
            Event like = new Event()
                    .setTopic("like")
                    .setEntityId(entityType)
                    .setEntityId(entityId)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(like);
        }

        if(entityType == ENTITY_TYPE_POST){
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
