package com.zzkk.community.service;

import com.zzkk.community.util.CommunityConstant;
import com.zzkk.community.util.HostHolder;
import com.zzkk.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.font.LineMetrics;
import java.util.*;

/**
 * @author zzkk
 * @ClassName followService
 * @Description 关注的业务
 **/
@Service
public class FollowService implements CommunityConstant {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private HostHolder hostHolder;

    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations op) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityId, entityType);
                op.multi();
                redisTemplate.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return op.exec();
            }
        });
    }

    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations op) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityId, entityType);
                op.multi();
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                redisTemplate.opsForZSet().remove(followerKey, userId);
                return op.exec();
            }
        });
    }

    // 查询实体的关注的数量
    public long findFolloweeCount(int userId, int entityType) {
        return redisTemplate.opsForZSet().zCard(RedisKeyUtil.getFolloweeKey(userId, entityType));
    }

    // 查询实体的粉丝的数量
    public long findFollowerCount(int entityType, int entityId) {
        return redisTemplate.opsForZSet().zCard(RedisKeyUtil.getFollowerKey(entityId, entityType));
    }

    // 查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询某用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            map.put("user", userService.findUserById(targetId));
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            if(hostHolder.getUser() != null){
                boolean hasFollowed = hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, targetId);
                map.put("hasFollowed",hasFollowed);
            }else{
                map.put("hasFollowed",false);
            }
            list.add(map);
        }
        return list;
    }

    // 查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            map.put("user", userService.findUserById(targetId));
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            if(hostHolder.getUser() != null){
                boolean hasFollowed = hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, targetId);
                map.put("hasFollowed",hasFollowed);
            }else{
                map.put("hasFollowed",false);
            }
            list.add(map);
        }
        return list;
    }
}
