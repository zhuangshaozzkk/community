package com.zzkk.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
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

    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code );
        jsonObject.put("msg",msg);
        if(map != null){
            for(String key: map.keySet()){
                jsonObject.put(key,map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }

    public static String getJSONString(int code,String msg){
        return getJSONString(code,msg,null);
    }

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("name","zzkk");
        map.put("age",18);
        System.out.println(getJSONString(0, "ok", map));
    }
}
