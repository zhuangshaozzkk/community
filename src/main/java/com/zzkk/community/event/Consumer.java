package com.zzkk.community.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.zzkk.community.entity.DiscussPost;
import com.zzkk.community.entity.Event;
import com.zzkk.community.entity.Message;
import com.zzkk.community.service.DiscussPostService;
import com.zzkk.community.service.ElasticsearchService;
import com.zzkk.community.service.MessageService;
import com.zzkk.community.util.CommunityConstant;
import com.zzkk.community.util.CommunityUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 * @author zzkk
 * @ClassName Consumer
 * @Description Todo
 **/
@Component
public class Consumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Resource
    private MessageService messageService;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qinniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Resource
    private ThreadPoolTaskScheduler taskScheduler;

    @KafkaListener(topics = {"comment","like","follow"})
    public void handleCommentMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("????????????????????????");
            return;
        }
        Event event =  JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("?????????????????????");
            return;
        }

        // ??????????????????
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        // content ????????? ???????????? ?????????id
        Map<String,Object> map = new HashMap<>();
        map.put("userId",event.getUserId());
        map.put("entityType",event.getEntityType());
        map.put("entityId",event.getEntityId());

        // ????????????data???????????????map???content?????????
        if(! event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                map.put(entry.getKey(),entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(map));
        messageService.addMessage(message);
    }

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("????????????????????????");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("?????????????????????");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("????????????????????????");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("?????????????????????");
            return;
        }


        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("????????????????????????");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("?????????????????????");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand +" --quality 75 "+htmlUrl+" "+wkImageStorage+"/"+fileName+suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("??????????????????: "+cmd);
        } catch (IOException e) {
            logger.error("??????????????????: "+e);
        }


        // ????????????????????????????????????????????????????????????????????????????????????
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class UploadTask implements Runnable {
        // ????????????
        private String fileName;
        // ????????????
        private String suffix;

        private Future future;
        // ????????????
        private long startTime;
        // ????????????
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // ????????????
            if(System.currentTimeMillis()-startTime > 30000){
                logger.error("??????????????????,???????????????"+fileName);
                future.cancel(true);
                return;
            }
            // ????????????
            if(uploadTimes >= 3){
                logger.error("??????????????????,???????????????"+fileName);
                future.cancel(true);
                return;
            }
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("?????????%d?????????[%s].",++uploadTimes,fileName));
                // ??????????????????
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // ??????????????????
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // ??????????????????
                UploadManager manager = new UploadManager(new Configuration(Zone.zone2()));
                try {
                    // ??????????????????
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);
                    // ??????????????????
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("???%d???????????????[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("???%d???????????????[%s].", uploadTimes, fileName));
                }
            } else {
                logger.info("??????????????????[" + fileName + "].");
            }
        }
    }
}
