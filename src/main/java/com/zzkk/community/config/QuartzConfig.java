package com.zzkk.community.config;

import com.zzkk.community.quartz.AlphaJob;
import com.zzkk.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author zzkk
 * @ClassName QuartzConfig
 * @Description 第一次读取信息把配置信息存储到数据库，以后访问数据库调度任务
 **/
@Configuration
public class QuartzConfig {
    // FactoryBean 可以简化Bean的实例化过程
    // 封装Bean的实例化过程
    // 将FactoryBean注入Spring容器
    // 实际的到时FactoryBean管理的对象的实例

    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(postScoreRefreshJobDetail);
        simpleTriggerFactoryBean.setName("postScoreRefreshTrigger");
        simpleTriggerFactoryBean.setGroup("communityTriggerGroup");
        simpleTriggerFactoryBean.setRepeatInterval(3000);
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());
        return simpleTriggerFactoryBean;
    }


//    @Bean
//    public JobDetailFactoryBean alphaJobDetail(){
//        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
//        factoryBean.setJobClass(AlphaJob.class);
//        factoryBean.setName("alphaJob");
//        factoryBean.setGroup("alphaJobGroup");
//        factoryBean.setDurability(true);
//        factoryBean.setRequestsRecovery(true);
//        return factoryBean;
//    }

//    @Bean
//    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
//        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
//        simpleTriggerFactoryBean.setJobDetail(alphaJobDetail);
//        simpleTriggerFactoryBean.setName("alphaTrigger");
//        simpleTriggerFactoryBean.setGroup("alphaTriggerGroup");
//        simpleTriggerFactoryBean.setRepeatInterval(3000);
//        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());
//        return simpleTriggerFactoryBean;
//    }
}
