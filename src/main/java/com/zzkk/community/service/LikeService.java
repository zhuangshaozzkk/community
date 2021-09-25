package com.zzkk.community.service;

import com.sun.org.omg.CORBA.RepositoryIdSeqHelper;
import com.zzkk.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.lang.model.element.NestingKind;

/**
 * @author zzkk
 * @ClassName LikeService
 * @Description Todo
 **/
@Service
public class LikeService {
    @Resource
    private RedisTemplate redisTemplate;

    // 点赞
    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        if (redisTemplate.opsForSet().isMember(entityLikeKey, userId)) {
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        } else {
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        // 两次的添加操作，所以要加上事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations op) throws DataAccessException {
                // 点赞的实体
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                // 点赞实体拥有者
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 用户是否有点赞
                Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                // 开启事务
                op.multi();
                if(isMember){
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                }else{
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                // 返回事务
                return op.exec();
            }
        });
    }

    // 查询某实体点赞数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    // 查询某个用户获得的赞的数量
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
