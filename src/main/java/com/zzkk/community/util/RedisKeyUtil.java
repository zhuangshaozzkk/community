package com.zzkk.community.util;

import sun.java2d.pipe.SolidTextRenderer;

import java.util.Date;

/**
 * @author zzkk
 * @ClassName RedisKeyUtil
 * @Description Todo
 **/
public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    // 统计我关注的目标
    private static final String PREFIX_FOLLOWEE = "followee";
    // 统计我的粉丝
    private static final String PREFIX_FOLLOWER = "follower";
    // 缓存验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";
    // 用户
    private static final String PREFIX_USER = "user";
    // 独立访客
    private static final String PREFIX_UV = "uv";
    // 单日活跃用户
    private static final String PREFIX_DAU = "dau";
    //
    private static final String PREFIX_POST = "post";

    // 某个实体的赞
    // like:entity:entityType:entityId --> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId --> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId + SPLIT;
    }

    // 某个用户关注的实体
    // followee:userId:entityType --> zset(entityId, nowTime)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId --> zset(userId, nowTime)
    public static String getFollowerKey(int entityId, int entityType) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 保存用户缓存
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 当日uv
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 当日dau
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间uv
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 区间dau
    public static String getDAUKey(String startDate,String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    //帖子分数
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }
}
