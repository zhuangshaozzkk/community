package com.zzkk.community.config;

import com.zzkk.community.controller.interceptor.AlphaInterceptor;
import com.zzkk.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author zzkk
 * @ClassName WebMvcConfig
 * @Description Todo
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private AlphaInterceptor alphaInterceptor;
    @Resource
    private LoginTicketInterceptor loginTicketInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }


}
