package com.zzkk.community.util;

/**
 * @Description // 激活状态码 成功 0 失败 1 重复激活 2
 * @Param
 * @return
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
    int DEFAULT_EXPIRED_SECOND = 3600*10;

    /**
     * @Description //设置最大失效时间
     **/
    int REMEMBER_EXPIRED_SECOND = 3600*24*100;
}
