package com.zzkk.community.controller.interceptor;

import com.zzkk.community.annotation.LoginRequired;
import com.zzkk.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author zzkk
 * @ClassName LoginRequiredInterceptor
 * @Description 编写拦截器在方法调用前判断是否有注解，根据当前登录状态决定是否执行
 **/
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Resource
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired annotation = method.getAnnotation(LoginRequired.class);
            // 有LoginRequired注解并且当前没有登录用户
            if(annotation!=null && hostHolder.getUser() == null){
                // 重定向到登录页面
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        return true;
    }
}
