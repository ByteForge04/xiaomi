package org.xiaomi.service.impl;

import cn.hutool.core.lang.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.xiaomi.entity.VehicleInfo;
import org.xiaomi.service.VehicleInfoService;
import org.xiaomi.mapper.VehicleInfoMapper;
import org.xiaomi.utils.SnowflakeIdGenerator;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class VehicleInfoServiceImpl implements VehicleInfoService {
    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String VEHICLE_INFO_KEY_PREFIX = "vehicle:info:";
    private static final long CACHE_TTL = 10; // 缓存10分钟

    @Override
    public int addVehicle(VehicleInfo vehicleInfo) {
        Random random = new Random();
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(random.nextInt(10));
        long l = snowflakeIdGenerator.nextId();
        vehicleInfo.setVid(Long.toHexString(l));
        int res = vehicleInfoMapper.insert(vehicleInfo);
        if (res > 0) {
            String key = VEHICLE_INFO_KEY_PREFIX + vehicleInfo.getCarId();
            redisTemplate.opsForValue().set(key, vehicleInfo, CACHE_TTL, TimeUnit.MINUTES);
        }
        return res;
    }

    @Override
    public List<VehicleInfo> listVehicles() {
        return vehicleInfoMapper.selectAll();
    }

    @Override
    public VehicleInfo getVehicle(Integer id) {
        String key = VEHICLE_INFO_KEY_PREFIX + id;
        VehicleInfo info = (VehicleInfo) redisTemplate.opsForValue().get(key);
        if (info != null) {
            return info;
        }
        info = vehicleInfoMapper.selectById(id);
        if (info != null) {
            redisTemplate.opsForValue().set(key, info, CACHE_TTL, TimeUnit.MINUTES);
        }
        return info;
    }

    @Override
    public int updateVehicle(VehicleInfo vehicleInfo) {
        int res = vehicleInfoMapper.update(vehicleInfo);
        if (res > 0) {
            String key = VEHICLE_INFO_KEY_PREFIX + vehicleInfo.getCarId();
            redisTemplate.opsForValue().set(key, vehicleInfo, CACHE_TTL, TimeUnit.MINUTES);
        }
        return res;
    }

    @Override
    public int deleteVehicle(Integer id) {
        int res = vehicleInfoMapper.delete(id);
        if (res > 0) {
            String key = VEHICLE_INFO_KEY_PREFIX + id;
            redisTemplate.delete(key);
        }
        return res;
    }
} 