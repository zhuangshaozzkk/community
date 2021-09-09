package com.zzkk.community.service;

import com.zzkk.community.dao.DiscussPostMapper;
import com.zzkk.community.entity.DiscussPost;
import org.springframework.stereotype.Service;

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
}
