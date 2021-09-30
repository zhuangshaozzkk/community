package com.zzkk.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author zzkk
 * @ClassName KWconfig
 * @Description Todo
 **/
@Configuration
public class KWconfig {
    private final static Logger logger = LoggerFactory.getLogger(KWconfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init(){
        // 创建wk图片的目录
        File file = new File(wkImageStorage);
        if(!file.exists()){
            file.mkdirs();
            logger.info("创建wk图片的目录"+wkImageStorage);
        }
    }
}
