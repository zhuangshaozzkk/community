package com.zzkk.community.controller;

import com.sun.mail.imap.protocol.ID;
import com.zzkk.community.entity.Comment;
import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.entity.Event;
import com.zzkk.community.event.EventProducer;
import com.zzkk.community.service.CommentService;
import com.zzkk.community.service.DiscussPostService;
import com.zzkk.community.util.CommunityConstant;
import com.zzkk.community.util.HostHolder;
import com.zzkk.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author zzkk
 * @ClassName CommentController
 * @Description Todo
 **/
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Resource
    private CommentService commentService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private EventProducer eventProducer;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private RedisTemplate redisTemplate;

    // 处理插入评论请求 完成操作后要返回帖子，所以路径中要带上帖子id
    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
            comment.setUserId(hostHolder.getUser().getId());
            comment.setCreateTime(new Date());
            comment.setStatus(0);
            commentService.addComment(comment);

            // 触发评论事件
            Event event = new Event()
                    .setTopic("comment")
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityId(comment.getEntityId())
                    .setEntityType(comment.getEntityType())
                    .setData("postId", discussPostId);

            if(comment.getEntityType() == ENTITY_TYPE_POST){
                DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
                event.setEntityUserId(target.getUserId());
            }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
                Comment target = commentService.findCommentById(comment.getEntityId());
                event.setEntityUserId(target.getUserId());
            }
            eventProducer.fireEvent(event);

            // 添加评论 修改帖子 触发发帖事件
            if(comment.getEntityType() == ENTITY_TYPE_POST){
                 event = new Event().setTopic(TOPIC_PUBLISH)
                        .setUserId(comment.getId())
                        .setEntityType(ENTITY_TYPE_POST)
                        .setEntityId(discussPostId);

                eventProducer.fireEvent(event);
                // 计算帖子分数
                String redisKey = RedisKeyUtil.getPostScoreKey();
                redisTemplate.opsForSet().add(redisKey,discussPostId);
            }

            return "redirect:/discussPost/detail/"+discussPostId;
    }
}
