package org.xiaomi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaomi.entity.SignalRule;

import java.util.List;

@Mapper
public interface SignalRuleMapper {
    List<SignalRule>selectAll(@Param("warnId") Integer warnId, @Param("batteryType") String batteryType);
}
