package org.xiaomi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarnInfoDTO {
    private Integer carId;
    private Integer warnId;

    private Signal signal;


}
