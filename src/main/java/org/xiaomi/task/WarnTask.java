package org.xiaomi.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xiaomi.dto.WarnInfoDTO;
import org.xiaomi.dto.Signal;
import org.xiaomi.entity.VehicleInfo;
import org.xiaomi.mapper.VehicleInfoMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class WarnTask {

    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;

    private final Random random = new Random();
    private static final String TOPIC = "warn-topic";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Scheduled(fixedRate = 10000)
 // 每分钟执行一次
    public void generateAndSendWarnings() throws MQClientException {
        log.info("开始生成随机预警信息...");
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer("warn-producer-group");
        defaultMQProducer.setNamesrvAddr("localhost:9876");
        defaultMQProducer.start();
        // 获取所有车辆信息
        List<VehicleInfo> vehicles = vehicleInfoMapper.selectAll();
        if (vehicles.isEmpty()) {
            log.warn("没有找到车辆信息，无法生成预警");
            return;
        }

        List<WarnInfoDTO> warnInfoList = new ArrayList<>();

        // 遍历每个车辆，生成预警信息
        for (VehicleInfo vehicle : vehicles) {
            WarnInfoDTO warnInfoDTO = generateRandomSignal(vehicle.getCarId());
            warnInfoList.add(warnInfoDTO);
        }

        try {
            // 将预警信息列表转换为JSON字符串
            String messageBody = objectMapper.writeValueAsString(warnInfoList);
            Message message = new Message(TOPIC, messageBody.getBytes(StandardCharsets.UTF_8));

            // 发送消息
            SendResult sendResult = defaultMQProducer.send(message);
            log.info("生成预警信息并发送到RocketMQ: {}, 发送结果: {}", warnInfoList, sendResult);
        } catch (Exception e) {
            log.error("发送预警信息失败", e);
        }
    }

    private WarnInfoDTO generateRandomSignal(Integer carId) {
        WarnInfoDTO warnInfoDTO = new WarnInfoDTO();
        warnInfoDTO.setCarId(carId);

        // 随机决定是否生成电压差或电流差预警
        boolean generateVoltage = random.nextBoolean();
        boolean generateCurrent = random.nextBoolean();

        Signal signal = new Signal();

        if (generateVoltage) {
            double mx = 1.5+ random.nextDouble() * 2.0; // 1.5-3.5
            double mi = 0.5 + random.nextDouble() * 1.0; // 0.5-1.5
            signal.setMx(mx);
            signal.setMi(mi);
        }

        if (generateCurrent) {
            double ix = 1.5 + random.nextDouble() * 2.0; // 1.5-3.5
            double ii = 0.5 + random.nextDouble() * 1.0; // 0.5-1.5
            signal.setIx(ix);
            signal.setIi(ii);
        }

        warnInfoDTO.setSignal(signal);
        return warnInfoDTO;
    }
}
