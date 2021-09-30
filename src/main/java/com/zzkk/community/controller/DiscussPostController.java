package com.zzkk.community.controller;

import com.zzkk.community.entity.*;
import com.zzkk.community.event.EventProducer;
import com.zzkk.community.service.CommentService;
import com.zzkk.community.service.DiscussPostService;
import com.zzkk.community.service.LikeService;
import com.zzkk.community.service.UserService;
import com.zzkk.community.util.CommunityConstant;
import com.zzkk.community.util.CommunityUtil;
import com.zzkk.community.util.HostHolder;
import com.zzkk.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zzkk
 * @ClassName DiscussPostController
 * @Description Todo
 **/
@Controller
@RequestMapping(path = "/discussPost")
public class DiscussPostController implements CommunityConstant {
    @Resource
    private DiscussPostService discussPostService;
    @Resource
    private UserService userService;
    @Resource
    private CommentService commentService;
    @Resource
    private LikeService likeService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private EventProducer eventProducer;

    @Resource
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"账号没有登录！");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // 触发发帖事件
        Event event = new Event().setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());

        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,discussPost.getId());


        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    // 获取帖子详情（这里要添加评论请求）
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int id, Model model, Page page){
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post",post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount",likeCount);
        // 点赞状态
        int likeStatus = (hostHolder.getUser()==null)?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeStatus",likeStatus);
        // 分页设置
        page.setPath("/discussPost/detail/"+id);
        page.setTotal(post.getCommentCount());
        // 帖子评论
        List<Comment> comments = commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (comments!=null) {
            for(Comment comment : comments){
                Map<String,Object> map = new HashMap<>();
                // 一条评论
                map.put("comment",comment);
                // 评论的作者
                map.put("user",userService.findUserById(comment.getUserId()));
                // 评论的点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                map.put("likeCount",likeCount);
                // 点赞状态
                likeStatus = (hostHolder.getUser()==null)?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                map.put("likeStatus",likeStatus);

                // 回复列表（评论的评论）
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList!=null){
                    for (Comment reply : replyList) {
                        Map<String,Object>  replyVO= new HashMap<>();
                        // 回复
                        replyVO.put("reply",reply);
                        // 作者
                        replyVO.put("user",userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = (reply.getTargetId() == 0) ? null : userService.findUserById(reply.getTargetId());
                        replyVO.put("target", target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVO.put("likeCount",likeCount);
                        // 点赞状态
                        likeStatus = (hostHolder.getUser()==null)?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVO.put("likeStatus",likeStatus);
                        replyVoList.add(replyVO);
                    }
                }
                map.put("replys",replyVoList);
                map.put("replyCount",commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId()));
                commentVoList.add(map);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }


    // 置顶
    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);
        // 更新elasticsearch数据库
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    // 加精
    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);
        // 更新elasticsearch数据库
        Event event = new Event().setTopic(TOPIC_PUBLISH).setUserId(hostHolder.getUser().getId()).setEntityType(ENTITY_TYPE_POST).setEntityId(id);
        eventProducer.fireEvent(event);
        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);
        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);
        // 更新elasticsearch数据库
        Event event = new Event().setTopic(TOPIC_PUBLISH).setUserId(hostHolder.getUser().getId()).setEntityType(ENTITY_TYPE_POST).setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}
