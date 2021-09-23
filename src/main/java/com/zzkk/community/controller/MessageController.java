package com.zzkk.community.controller;

import com.zzkk.community.entity.Message;
import com.zzkk.community.entity.Page;
import com.zzkk.community.entity.User;
import com.zzkk.community.service.MessageService;
import com.zzkk.community.service.UserService;
import com.zzkk.community.util.CommunityUtil;
import com.zzkk.community.util.HostHolder;
import org.apache.ibatis.javassist.compiler.TokenId;
import org.apache.tomcat.jni.Mmap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.ToDoubleBiFunction;

/**
 * @author zzkk
 * @ClassName MessageController
 * @Description Todo
 **/
@Controller
public class MessageController {
    @Resource
    private MessageService messageService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private UserService userService;

    // 获取私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页信息
        page.setPath("/letter/list");
        page.setTotal(messageService.findConversationCount(user.getId()));
        page.setLimit(5);
        // 会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                // 会话
                map.put("conversation", message);
                // 私信条数
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                // 未读消息数量
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 会话目标对象 设置头像
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询总的未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    // 私信详情
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterList(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        page.setPath("/letter/detail/"+conversationId);
        page.setLimit(5);
        page.setTotal(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList!=null){
            for (Message message : letterList) {
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                // 每一条信息发起方
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        // 私信目标对象
        model.addAttribute("target",getLetterTarget(conversationId));

        // 设置已读 在进入页面前把未读消息设为已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids,1);
        }
        return "/site/letter-detail";
    }

    // 获得私信目标
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    // 处理发送消息请求
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在!");
        }
        Message message = new Message();
        int fromId = hostHolder.getUser().getId();
        message.setFromId(fromId);
        int toId = target.getId();
        message.setToId(toId);
        String conversationId = (fromId <= toId)?fromId +"_"+toId:toId+"_"+fromId;
        message.setConversationId(conversationId);
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    // 读取用户信息列表发送给自己未读信息id列表，用于后续修改成已读状态
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids= new ArrayList<>();
        if(letterList!=null){
            for (Message message : letterList) {
                // 判断当前用户是接收者
                if(hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }
}
