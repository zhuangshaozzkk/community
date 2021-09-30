package com.zzkk.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.zzkk.community.dao.DiscussPostMapper;
import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zzkk
 * @ClassName DiscussPostService
 * @Description Todo
 **/
@Service
public class DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Value("${caffein.post.max-size}")
    private int maxSize;

    @Value("${caffeine.post.expire-seconds}")
    private int expireSecond;

    @Resource
    private DiscussPostMapper discussPostMapper;

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowCache;

    @PostConstruct
    public void init(){
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSecond, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(String key) throws Exception {
                        if(key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] params = key.split(":");
                        if(params == null || params.length != 2){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存： Redis -> mysql

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });

        // 初始化帖子总数缓存
        postRowCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSecond,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(Integer key) throws Exception {
                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        if(userId == 0 && orderMode == 1){
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int findDiscussPostRows(int userId) {
        if(userId == 0){
            return postRowCache.get(userId);
        }
        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Resource
    private SensitiveFilter sensitiveFilter;

    // 发布帖子
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空");
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
    public int updateCommentCountById(int id, int commentCount) {
        return discussPostMapper.updateCommentCountById(id, commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int postId, double score) {
        return discussPostMapper.updateScore(postId, score);
    }
}
