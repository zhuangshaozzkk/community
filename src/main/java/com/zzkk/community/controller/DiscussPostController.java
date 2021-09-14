package com.zzkk.community.controller;

import com.zzkk.community.entity.Comment;
import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.entity.Page;
import com.zzkk.community.entity.User;
import com.zzkk.community.service.CommentService;
import com.zzkk.community.service.DiscussPostService;
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
import java.lang.annotation.Target;
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
    private HostHolder hostHolder;

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
        return CommunityUtil.getJSONString(0,"发布成功！ ");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int id, Model model, Page page){
        DiscussPost post = discussPostService.findDiscussPostById(id);
        model.addAttribute("post",post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        // 设置帖子评论
        // 分页设置
        page.setPath("/discussPost/detail/"+id);
        page.setTotal(post.getCommentCount());
        List<Comment> comments = commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> commentVOList = new ArrayList<>();
        if (comments!=null) {
            for(Comment comment : comments){
                Map<String,Object> map = new HashMap<>();
                map.put("comment",comment);
                map.put("user",userService.findUserById(comment.getUserId()));
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String,Object>> replyVOList = new ArrayList<>();
                if(replyList!=null){
                    for (Comment reply : replyList) {
                        Map<String,Object>  replyVO= new HashMap<>();
                        // 回复
                        replyVO.put("reply",reply);
                        // 作者
                        replyVO.put("user",userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVO.put("target", target);
                        replyVOList.add(replyVO);
                    }
                }
                map.put("replys",replyList );
                map.put("replyCount",commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId()));
                commentVOList.add(map);
            }
        }
        model.addAttribute("comments",commentVOList);
        return "/site/discuss-detail";
    }

}
