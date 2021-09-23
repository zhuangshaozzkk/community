package com.zzkk.community.dao;

import com.zzkk.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    // 查询单前用户会话列表，每一组会话只返回最新的一条私信
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户会话数量
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话包含私信的数量
    int selectLettersCount(String conversationId);

    // 查询用户未读私信的数量(动态拼接会话id实现局部查询功能)
    int selectLettersUnreadCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 修改消息状态
    int updateStatus(List<Integer> ids, int status);
}
