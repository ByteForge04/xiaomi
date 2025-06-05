package org.xiaomi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignalRule {

    private Integer id;

    private Integer warnId;

    private String warnName;

    private String batteryType;

    private String warningRule;

}