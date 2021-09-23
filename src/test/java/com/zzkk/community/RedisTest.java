package com.zzkk.community;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zzkk
 * @ClassName RedisTest
 * @Description Todo
 **/
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
}
