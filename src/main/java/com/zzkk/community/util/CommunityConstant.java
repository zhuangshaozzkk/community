package com.zzkk.community.util;

/**
 * @Description // 常量接口，全局复用
 **/
public interface CommunityConstant {
    // 激活成功
    int ACTIVATION_SUCCESS = 0;
    // 激活失败
    int ACTIVATION_FAILURE = 1;
    // 重复激活
    int ACTIVATION_REPEAT = 2;

    /**
     * @Description //设置默认失效时间
     **/
    int DEFAULT_EXPIRED_SECOND = 60*60*10;

    /**
     * @Description //设置最大失效时间
     **/
    int REMEMBER_EXPIRED_SECOND = 60*60*24*100;

    /**
     * @Description // 实体类型：帖子
     **/
    int ENTITY_TYPE_POST = 1;

    /**
     * @Description // 实体类型：评论
     **/
    int ENTITY_TYPE_COMMENT = 2;
}
