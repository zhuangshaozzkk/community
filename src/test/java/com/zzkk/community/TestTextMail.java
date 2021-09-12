package com.zzkk.community;

import com.zzkk.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

/**
 * @author zzkk
 * @ClassName TestTextMail
 * @Description Todo
 **/
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TestTextMail {
    @Resource
    private MailClient mailClient;

    @Resource
    private  TemplateEngine templateEngine;

    @Test
    public void testMail(){
        mailClient.sendMail("zhuangshao0826@163.com","offer","恭喜你被录用！");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","zzkk");
        // 返回html代码
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("zhuangshao0826@163.com","offer",content);
    }

    @Test
    public void testUtil(){
        String filename = "zafj.png";
        String substring = filename.substring(filename.lastIndexOf("."));
        System.out.println(substring);
    }
}
