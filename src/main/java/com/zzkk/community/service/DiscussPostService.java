package com.zzkk.community.service;

import com.zzkk.community.dao.CommentMapper;
import com.zzkk.community.dao.DiscussPostMapper;
import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zzkk
 * @ClassName DiscussPostService
 * @Description Todo
 **/
@Service
public class DiscussPostService {
    @Resource
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Resource
    private SensitiveFilter sensitiveFilter;

    // 发布帖子
    public int addDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw  new IllegalArgumentException("参数不能为空");
        }
        // 转义成HTML HtmlUtils.htmlEscape
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤敏感字符
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
}

    // 更新帖子的评论
    public int updateCommentCountById(int id,int commentCount){
        return discussPostMapper.updateCommentCountById(id,commentCount);
    }
}
