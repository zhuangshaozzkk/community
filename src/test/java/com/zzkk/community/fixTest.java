package com.zzkk.community;

import com.zzkk.community.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author zzkk
 * @ClassName fixTest
 * @Description Todo
 **/
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class fixTest {
    @Test
    public void changPassword(){
        String pass = "zzkk";
        String newPass = CommunityUtil.md5(pass + "3aaee");
        System.out.println(newPass);
    }
}
