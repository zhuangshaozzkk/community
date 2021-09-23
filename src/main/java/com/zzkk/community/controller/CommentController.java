package com.zzkk.community.controller;

import com.zzkk.community.entity.Comment;
import com.zzkk.community.service.CommentService;
import com.zzkk.community.util.HostHolder;
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
public class CommentController {
    @Resource
    private CommentService commentService;

    @Resource
    private HostHolder hostHolder;

    // 处理插入评论请求 完成操作后要返回帖子，所以路径中要带上帖子id
    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
            comment.setUserId(hostHolder.getUser().getId());
            comment.setCreateTime(new Date());
            comment.setStatus(0);
            commentService.addComment(comment);
            return "redirect:/discussPost/detail/"+discussPostId;
    }
}
