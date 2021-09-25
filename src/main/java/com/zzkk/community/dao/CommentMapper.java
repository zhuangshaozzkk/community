package com.zzkk.community.dao;

import com.zzkk.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface CommentMapper {
    // 根据实体查询评论
    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);
    // 评论的数量
    int selectCountByEntity(int entityType,int entityId);
    // 插入评论
    int insertComment(Comment comment);
    // 查找评论
    Comment selectCommentById(int id);
}
