package org.xiaomi.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarnInfo {
    private Integer id;
    private Integer carId;
    private Integer ruleId;
    private String warnType;
    private String warnMsg;
    private Double signalValue;
    private Date createTime;
}
