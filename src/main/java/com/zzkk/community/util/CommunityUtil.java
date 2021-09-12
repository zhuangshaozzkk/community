package com.zzkk.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author zzkk
 * @ClassName CommunityUtil
 * @Description Todo
 **/
public class CommunityUtil {

    /**
     * @Description //生成随机的字符串
     **/
    public static String generateRandString(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * @Description // md5加密
     **/
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
