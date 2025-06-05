package org.xiaomi.service;


import org.xiaomi.dto.WarnInfoDTO;
import org.xiaomi.dto.WarnInfoVO;
import org.xiaomi.entity.WarnInfo;
import java.util.List;

public interface WarnInfoService {
    int addWarnInfo(WarnInfoDTO warnInfoDTO, List<WarnInfoVO>warnInfoVOList);
    List<WarnInfo> getWarnsByCarId(Integer carId);
    List<WarnInfo> getAllWarns();
}