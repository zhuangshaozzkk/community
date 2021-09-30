package com.zzkk.community;

import com.zzkk.community.util.CommunityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zzkk
 * @ClassName fixTest
 * @Description Todo
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class fixTest {
    @Resource
    private Scheduler scheduler;
    @Test
    public void deleteJob(){
        try {
            scheduler.deleteJob(new JobKey("postScoreRefreshJob","communityJobGroup"));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
