package com.zzkk.community.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zzkk
 * @ClassName AlphaInterceptor
 * @Description 拦截器示例
 **/
@Component
public class AlphaInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaInterceptor.class);

    /**
     * @Description // 在controller处理请求之前执行
     **/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.debug("preHandle"+handler.toString());
        return true;
    }

    /**
     * @Description // 在调用完controller之后，模板之前执行
     **/
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LOGGER.debug("postHandle"+handler.toString());
    }

    /**
     * @Description // 在templateEngine执行完后执行
     **/
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LOGGER.debug("afterCompletion"+handler.toString());
    }
}
