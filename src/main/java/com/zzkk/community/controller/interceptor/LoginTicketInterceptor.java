package com.zzkk.community.controller.interceptor;

import com.zzkk.community.dao.LoginTicketMapper;
import com.zzkk.community.entity.LoginTicket;
import com.zzkk.community.entity.User;
import com.zzkk.community.service.UserService;
import com.zzkk.community.util.CookieUtil;
import com.zzkk.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author zzkk
 * @ClassName LoginTicketInterceptor
 * @Description 登录信息拦截器
 **/
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Resource
    private UserService userService;
    @Resource
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie获取凭证String
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket!=null){
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicketByTicket(ticket);
            // 验证凭证是否有效 状态 超时时间
            if(loginTicket!=null&&loginTicket.getStatus()==0&&loginTicket.getExpired().after(new Date())){
                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 将本次请求的用户(线程隔离)保存起来
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null && modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
