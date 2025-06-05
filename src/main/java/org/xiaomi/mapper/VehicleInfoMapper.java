package org.xiaomi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.xiaomi.entity.VehicleInfo;
import java.util.List;
@Mapper
public interface VehicleInfoMapper {
    int insert(VehicleInfo vehicleInfo);
    List<VehicleInfo> selectAll();
    VehicleInfo selectById(Integer id);
    int update(VehicleInfo vehicleInfo);
    int delete(Integer id);
} 