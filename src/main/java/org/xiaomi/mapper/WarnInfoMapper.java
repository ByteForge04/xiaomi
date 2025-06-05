package org.xiaomi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.xiaomi.entity.WarnInfo;
import java.util.List;

@Mapper
public interface WarnInfoMapper {
    int insert(WarnInfo warnInfo);
    List<WarnInfo> selectByCarId(Integer carId);
    List<WarnInfo> selectAll();
}
