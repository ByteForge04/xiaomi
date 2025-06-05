package org.xiaomi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaomi.entity.VehicleInfo;
import org.xiaomi.entity.Result;
import org.xiaomi.service.VehicleInfoService;
import java.util.*;

@RestController
@RequestMapping("/vehicle")
public class VehicleInfoController {
    @Autowired
    private VehicleInfoService vehicleInfoService;

    @PostMapping("/add")
    public Result<VehicleInfo> addVehicle(@RequestBody VehicleInfo vehicleInfo) {
        int res = vehicleInfoService.addVehicle(vehicleInfo);
        if (res > 0) {
            return Result.success(vehicleInfo);
        } else {
            return Result.error("新增失败");
        }
    }

    @GetMapping("/list")
    public Result<List<VehicleInfo>> listVehicles() {
        return Result.success(vehicleInfoService.listVehicles());
    }

    @GetMapping("/get/{id}")
    public Result<VehicleInfo> getVehicle(@PathVariable Integer id) {
        VehicleInfo info = vehicleInfoService.getVehicle(id);
        if (info == null) return Result.error("未找到该车辆信息");
        return Result.success(info);
    }

    @PostMapping("/update")
    public Result<VehicleInfo> updateVehicle(@RequestBody VehicleInfo vehicleInfo) {
        int res = vehicleInfoService.updateVehicle(vehicleInfo);
        if (res > 0) {
            return Result.success(vehicleInfo);
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> deleteVehicle(@PathVariable Integer id) {
        int res = vehicleInfoService.deleteVehicle(id);
        if (res > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("未找到该车辆信息");
        }
    }
} 