package com.zzkk.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author zzkk
 * @ClassName KaptchaConfig
 * @Description 生成验证码配置类
 **/
@Configuration
public class KaptchaConfig {
    @Bean
    public Producer getDefaultKaptcha(){
        // 实例化实现类
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.color","black");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        // 有无噪声干扰
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");
        properties.setProperty("kaptcha.textproducer.char.string","1234567890qwertyuiopasdfghjklzxcvbnm");
        properties.setProperty("kaptcha.textproducer.char.length","4");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}











