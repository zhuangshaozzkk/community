package com.zzkk.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zzkk
 * @ClassName RedisTest
 * @Description Todo
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey,1);
        String redisKey1 = "test:age";

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash(){
        String key = "userInfo";
        redisTemplate.delete("userInfo");
        redisTemplate.opsForList().leftPush("userInfo","aa");
        redisTemplate.opsForList().leftPush("userInfo","a2a");
        redisTemplate.opsForList().rightPush("userInfo","bb");
        redisTemplate.opsForList().rightPush("userInfo","b2b");
        System.out.println(redisTemplate.opsForList().range("userInfo", 0, -1));
        redisTemplate.opsForList().leftPop(key);
        redisTemplate.opsForList().leftPop(key);
        System.out.println(redisTemplate.opsForList().range("userInfo", 0, -1));
    }

    @Test
    public void testSet(){
        String key = "test:set";
        redisTemplate.opsForSet().add(key,"刘备");
        redisTemplate.opsForSet().add(key,"关羽");
        redisTemplate.opsForSet().add(key,"诸葛亮");
        redisTemplate.opsForSet().add(key,"诸葛亮","卧龙凤雏");
        System.out.println(redisTemplate.opsForSet().members(key));
        System.out.println(redisTemplate.opsForSet().randomMember(key));
    }

    @Test
    public void testSortedSet(){
        String redisKey = "test:students";
        redisTemplate.opsForZSet().add(redisKey,"唐僧",80);
        redisTemplate.opsForZSet().add(redisKey,"悟空",90);
        redisTemplate.opsForZSet().add(redisKey,"八戒",50);
        redisTemplate.opsForZSet().add(redisKey,"沙僧",40);
        redisTemplate.opsForZSet().add(redisKey,"白龙马",60);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"悟空"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"悟空"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,-1));
    }


    @Test
    public void testKeys(){
        System.out.println(redisTemplate.hasKey("test:student"));
        String s = "zzkk";

        Map<String , Object > map = new HashMap<>();
        Set<Character> set =new HashSet<>();
        System.out.println("abc".compareTo("abcd"));
        System.out.println("b".compareTo("s"));
        System.out.println("s12a".compareTo("s12a"));
    }

    // 统计20万个重复数据的独立总数
    @Test
    public void testHyperLog(){
        String redisKey = "test:hll:01";
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,(int) (Math.random()*100000+1));
        }
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);
        for (int i = 0; i < 10; i++) {
            System.out.println(""+i+redisTemplate.opsForValue().getBit(redisKey, i));
        }
        Object count = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(count);
    }
}
