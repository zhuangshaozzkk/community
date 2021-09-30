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
            logger.error("消息的内容为空！");
            return;
        }
        Event event =  JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        // content 谁猝发 哪个实体 实体的id
        Map<String,Object> map = new HashMap<>();
        map.put("userId",event.getUserId());
        map.put("entityType",event.getEntityType());
        map.put("entityId",event.getEntityId());

        // 将事件中data字段都存入map（content字段）
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
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }


        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand +" --quality 75 "+htmlUrl+" "+wkImageStorage+"/"+fileName+suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功: "+cmd);
        } catch (IOException e) {
            logger.error("生成长图失败: "+e);
        }


        // 所以要定时任务执行，监视该图片，一旦生成，则上传至七牛云
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class UploadTask implements Runnable {
        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;

        private Future future;
        // 开始时间
        private long startTime;
        // 上传次数
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
            // 生成失败
            if(System.currentTimeMillis()-startTime > 30000){
                logger.error("执行时间过长,终止任务："+fileName);
                future.cancel(true);
                return;
            }
            // 上传失败
            if(uploadTimes >= 3){
                logger.error("上传次数过多,终止任务："+fileName);
                future.cancel(true);
                return;
            }
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("开始第%d次上传[%s].",++uploadTimes,fileName));
                // 设置相应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone2()));
                try {
                    // 开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                }
            } else {
                logger.info("等待图片生成[" + fileName + "].");
            }
        }
    }
}
