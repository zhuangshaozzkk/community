package com.zzkk.community.dao;

import com.zzkk.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Bean;

import java.util.List;
@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    // @Param 注解用于给参数起别名，只有一个参数，同时在if条件中使用必须加上别名
    int selectDiscussPostRows(@Param("userId") int userId);
}
