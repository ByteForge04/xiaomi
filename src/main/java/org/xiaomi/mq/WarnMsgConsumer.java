package org.xiaomi.mq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.xiaomi.dto.WarnInfoDTO;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class WarnMsgConsumer {

    private static final String TOPIC = "warn-topic";
    private static final String CONSUMER_GROUP = "warn-consumer-group";
    private static final String NAMESRV_ADDR = "localhost:9876";
    private static final String WARN_API_URL = "http://localhost:8081/api/warn";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() throws MQClientException {
        // 创建消费者实例
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(CONSUMER_GROUP);
        consumer.setNamesrvAddr(NAMESRV_ADDR);
        
        // 订阅主题
        consumer.subscribe(TOPIC, "*");
        
        // 注册消息监听器
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    try {
                        // 获取消息内容
                        String messageBody = new String(msg.getBody(), "UTF-8");
                        log.info("收到预警信息: {}", messageBody);
                        
                        // 将消息转换为对象
                        List<WarnInfoDTO> warnInfoList = objectMapper.readValue(messageBody, 
                            new TypeReference<List<WarnInfoDTO>>() {});
                        
                        // 设置请求头
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        
                        // 创建请求实体
                        HttpEntity<List<WarnInfoDTO>> requestEntity = new HttpEntity<>(warnInfoList, headers);
                        
                        // 调用预警接口
                        String response = restTemplate.postForObject(WARN_API_URL, requestEntity, String.class);
                        log.info("预警信息处理结果: {}", response);
                        
                    } catch (Exception e) {
                        log.error("处理预警信息失败", e);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        
        // 启动消费者
        consumer.start();
        log.info("RocketMQ consumer started successfully");
    }
} 