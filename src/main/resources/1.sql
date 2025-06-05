create database xiaomi_car_battery_warn_db character set utf8mb4 collate utf8mb4_unicode_ci;
use xiaomi_car_battery_warn_db;

CREATE TABLE vehicle_info (
                              vid VARCHAR(255) NOT NULL UNIQUE,
                              car_id INT AUTO_INCREMENT PRIMARY KEY,
                              battery_type VARCHAR(255),
                              total_mileage DECIMAL(10, 2),
                              battery_health_percentage DECIMAL(5, 2)
);
CREATE TABLE signal_rule (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             warn_id INT,
                             warn_name VARCHAR(255),
                             battery_type VARCHAR(255),
                             warning_rule TEXT
);
CREATE TABLE warn_info (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT ,
                           car_id INT NOT NULL ,
                           rule_id INT NOT NULL ,
                           warn_type VARCHAR(32) NOT NULL,
                           warn_msg VARCHAR(255) NOT NULL ,
                           signal_value DECIMAL(10,2) NOT NULL ,
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO signal_rule (warn_id, warn_name, battery_type, warning_rule) VALUES
                                                                             (1, '电压差报警', '三元电池', '5<=(Mx - Mi)，报警等级：0; 3<=(Mx - Mi)<5, 报警等级：1; 1<=(Mx - Mi)<3, 报警等级：2; 0.6<=(Mx - Mi)<1, 报警等级：3; 0.2<=(Mx - Mi)<0.6, 报警等级：4; (Mx - Mi)<0.2，不报警'),
                                                                             (1, '电压差报警', '铁锂电池', '2<=(Mx - Mi)，报警等级：0; 1<=(Mx - Mi)<2, 报警等级：1; 0.7<=(Mx - Mi)<1, 报警等级：2; 0.4<=(Mx - Mi)<0.7, 报警等级：3; 0.2<=(Mx - Mi)<0.4, 报警等级：4; (Mx - Mi)<0.2，不报警'),
                                                                             (2, '电流差报警', '三元电池', '3<=(Ix - Ii)，报警等级：0; 1<=(Ix - Ii)<3, 报警等级：1; 0.2<=(Ix - Ii)<1, 报警等级：2; (Ix - Ii)<0.2，不报警'),
                                                                             (2, '电流差报警', '铁锂电池', '1<=(Ix - Ii)，报警等级：0; 0.5<=(Ix - Ii)<1, 报警等级：1; 0.2<=(Ix - Ii)<0.5, 报警等级：2; (Ix - Ii)<0.2，不报警');
