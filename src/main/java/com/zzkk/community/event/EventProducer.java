package com.zzkk.community.event;

import com.alibaba.fastjson.JSONObject;
import com.zzkk.community.entity.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zzkk
 * @ClassName eventProducer
 * @Description Todo
 **/
@Component
public class EventProducer {
    @Resource
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event){
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
