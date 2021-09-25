package com.zzkk.community.service;

import com.zzkk.community.dao.MessageMapper;
import com.zzkk.community.entity.Message;
import com.zzkk.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zzkk
 * @ClassName MessageService
 * @Description Todo
 **/
@Service
public class MessageService {
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit){
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    public int findLetterCount(String conversationId){
        return messageMapper.selectLettersCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId){
        return messageMapper.selectLettersUnreadCount(userId,conversationId);
    }

    public int addMessage(Message  message){
        // 过滤字符
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    // 更改消息状态
    public int readMessage(List<Integer> ids, int status){
        return messageMapper.updateStatus(ids,status);
    }

    public Message findLatestNotice(int userId,String topic){
        return messageMapper.selectLatestNotice(userId,topic);
    }

    public int findNoticeCount(int userId,String topic){
        return messageMapper.selectNoticeCount(userId,topic);
    }

    public int findNoticeUnreadCount(int userId,String topic){
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

    public List<Message> findNotices(int userId,String topic,int offset,int limit){
        return messageMapper.selectNotices(userId,topic,offset,limit);
    }
}
