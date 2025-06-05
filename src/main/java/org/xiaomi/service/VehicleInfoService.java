package org.xiaomi.service;

import org.xiaomi.entity.VehicleInfo;
import java.util.List;

public interface VehicleInfoService {
    int addVehicle(VehicleInfo vehicleInfo);
    List<VehicleInfo> listVehicles();
    VehicleInfo getVehicle(Integer id);
    int updateVehicle(VehicleInfo vehicleInfo);
    int deleteVehicle(Integer id);
} 