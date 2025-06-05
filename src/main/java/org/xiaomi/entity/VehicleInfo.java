package org.xiaomi.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleInfo {

    private String vid;

    private Integer carId;

    private String batteryType;

    private BigDecimal totalMileage;

    private BigDecimal batteryHealthPercentage;
}