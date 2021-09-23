package com.zzkk.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Resource;

/**
 * @author zzkk
 * @ClassName kafkaTest
 * @Description Todo
 **/
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class kafkaTest {
    @Resource
    private  KafkaProducer kafkaProducer;
    @Test
    public void test() throws InterruptedException {
        kafkaProducer.sendMessage("test","hkjhsakjfh");
        kafkaProducer.sendMessage("test","发顺丰的");
        Thread.sleep(30000);
    }
}

@Component
class KafkaProducer{
    @Resource
    private KafkaTemplate kafkaTemplate;
    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic, content);
    }
}

@Component
class KafkaConsumer{
    @KafkaListener(topics = {"test"})
    public void handlerMessage(ConsumerRecord consumerRecord){
        System.out.println(consumerRecord.value());
    }
}
