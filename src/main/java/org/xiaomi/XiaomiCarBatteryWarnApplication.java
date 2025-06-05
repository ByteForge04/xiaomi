package org.xiaomi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XiaomiCarBatteryWarnApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaomiCarBatteryWarnApplication.class, args);
    }

}
