package com.zzkk.community.dao;

import com.zzkk.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Bean;

import java.util.List;
@Mapper
public interface DiscussPostMapper {
    // 查询首页帖子，userId可以选参数（userId = 0）不拼接到sql中
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    // @Param 注解用于给参数起别名，只有一个参数，同时在if条件中使用必须加上别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    // 根据帖子id查询帖子
    DiscussPost selectDiscussPostById(int id);

    // 根据帖子id更新帖子的评论数量 (在添加评论时同时更新评论数量)
    int updateCommentCountById(int id,int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);
}
