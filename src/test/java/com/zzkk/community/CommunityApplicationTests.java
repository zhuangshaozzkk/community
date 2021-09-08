package com.zzkk.community;

import com.zzkk.community.dao.AlphaDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {
    private  static ApplicationContext context;

    @Test
    void testApplicationContext() {
        System.out.println(context.getBean("hibernate",AlphaDao.class).select());
    }

    @Test
    void testSimpleDateFormat() {
        System.out.println(context.getBean(SimpleDateFormat.class).format(new Date()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
