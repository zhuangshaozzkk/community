package com.zzkk.community.controller.interceptor;

import com.zzkk.community.entity.User;
import com.zzkk.community.service.DataService;
import com.zzkk.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zzkk
 * @ClassName DataInterceptor
 * @Description Todo
 **/
@Component
public class DataInterceptor implements HandlerInterceptor {
    @Resource
    private DataService dataService;

    @Resource
    private HostHolder hostHolder;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        User user = hostHolder.getUser();
        if(user != null){
            dataService.recordDAU(user.getId());
        }
        return  true;
    }
}
