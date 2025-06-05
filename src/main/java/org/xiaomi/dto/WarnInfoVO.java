package org.xiaomi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarnInfoVO {
    private Integer  carId;
    private String batteryType;
    private String warnName;
    private String warnLevel;
}
