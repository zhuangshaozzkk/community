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

    /**
     * @Description // 实体类型：用户
     **/
    int ENTITY_TYPE_USER = 3;

    /**
     * @Description //系统用户ID
     **/
    int SYSTEM_USER_ID = 1;

    /**
     * @Description // 主题： 发帖
     **/
    String TOPIC_PUBLISH = "publish";

    /**
     * @Description // 主题： 删帖
     **/
    String TOPIC_DELETE = "delete";

    /**
     * 权限: 普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限: 管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限: 版主
     */
    String AUTHORITY_MODERATOR = "moderator";
}
