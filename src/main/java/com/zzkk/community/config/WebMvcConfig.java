package com.zzkk.community.config;

import com.zzkk.community.controller.interceptor.AlphaInterceptor;
import com.zzkk.community.controller.interceptor.LoginRequiredInterceptor;
import com.zzkk.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author zzkk
 * @ClassName WebMvcConfig
 * @Description interceptor配置 实现接口
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private AlphaInterceptor alphaInterceptor;
    @Resource
    private LoginTicketInterceptor loginTicketInterceptor;
    @Resource
    private LoginRequiredInterceptor loginRequiredInterceptor;

    // 处理请求排除静态资源
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(alphaInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        // 用户登录状态显示header
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        // 拦截用于不能特定路径
        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
