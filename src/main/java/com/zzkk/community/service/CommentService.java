package com.zzkk.community.service;

import com.zzkk.community.dao.CommentMapper;
import com.zzkk.community.dao.DiscussPostMapper;
import com.zzkk.community.entity.Comment;
import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.util.CommunityConstant;
import com.zzkk.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.util.List;

/**
 * @author zzkk
 * @ClassName CommentService
 * @Description Todo
 **/
@Service
public class CommentService implements CommunityConstant {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private DiscussPostMapper discussPostMapper;
    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    public int findCommentCount(int entityType ,int entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    // 增加评论 事务管理
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 处理字符格式
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 插入评论
        int rows = commentMapper.insertComment(comment);

        // 更新帖子（type）评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            int commentCount = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateCommentCountById(comment.getEntityId(),commentCount);
        }

        return rows;
    }
}
