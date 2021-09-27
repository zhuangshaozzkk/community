package com.zzkk.community;

import com.zzkk.community.dao.AlphaDao;
import com.zzkk.community.util.SensitiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {
    @Resource
    private SensitiveFilter sensitiveFilter;

    private static ApplicationContext context;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityApplicationTests.class);


    void testNum(){
        int a = 1000000;
        long b;
        b=a;
        System.out.println(b);
        System.out.println(a);
    }


    public void testSensitiveFilter(){
        String text = "这里可以读博,可以嫖娼,可以吸毒, 可以***...";
        text  =  sensitiveFilter.filter(text);
        System.out.println(text);

        text = "这里可以赌→博→,可以→嫖→娼→,可以吸→毒, 可以→***→...fabc";
        text  =  sensitiveFilter.filter(text);
        System.out.println(text);

        text = "fabcd";
        text  =  sensitiveFilter.filter(text);
        System.out.println(text);

        text = "fabcc";
        text  =  sensitiveFilter.filter(text);
        System.out.println(text);

        text = "fabc";
        text  =  sensitiveFilter.filter(text);
        System.out.println(text);
    }


    void testLogger(){
        LOGGER.error("error");
        LOGGER.debug("debug");
        LOGGER.info("info");
        LOGGER.warn("warn");
    }


    void testApplicationContext() {
        System.out.println(context.getBean("hibernate",AlphaDao.class).select());
    }


    void testSimpleDateFormat() {
        System.out.println(context.getBean(SimpleDateFormat.class).format(new Date()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
