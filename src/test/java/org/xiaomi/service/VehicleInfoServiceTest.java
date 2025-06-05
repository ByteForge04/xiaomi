package org.xiaomi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.xiaomi.entity.VehicleInfo;
import org.xiaomi.mapper.VehicleInfoMapper;
import org.xiaomi.service.impl.VehicleInfoServiceImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleInfoServiceTest {

    @Mock
    private VehicleInfoMapper vehicleInfoMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private VehicleInfoServiceImpl vehicleInfoService;

    private VehicleInfo testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = new VehicleInfo();
        testVehicle.setCarId(1);
        testVehicle.setBatteryType("Li-ion");
        testVehicle.setTotalMileage(new BigDecimal("1000.0"));
        testVehicle.setBatteryHealthPercentage(new BigDecimal("95.0"));
    }

    @Test
    void addVehicle_Success() {
        when(vehicleInfoMapper.insert(any(VehicleInfo.class))).thenReturn(1);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        int result = vehicleInfoService.addVehicle(testVehicle);

        assertEquals(1, result);
        verify(vehicleInfoMapper).insert(any(VehicleInfo.class));
        verify(valueOperations).set(anyString(), any(VehicleInfo.class), anyLong(), any());
    }

    @Test
    void listVehicles_Success() {
        List<VehicleInfo> expectedVehicles = Arrays.asList(testVehicle);
        when(vehicleInfoMapper.selectAll()).thenReturn(expectedVehicles);

        List<VehicleInfo> result = vehicleInfoService.listVehicles();

        assertNotNull(result);
        assertEquals(expectedVehicles.size(), result.size());
        verify(vehicleInfoMapper).selectAll();
    }

    @Test
    void getVehicle_FromCache() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(testVehicle);

        VehicleInfo result = vehicleInfoService.getVehicle(1);

        assertNotNull(result);
        assertEquals(testVehicle.getCarId(), result.getCarId());
        verify(valueOperations).get(anyString());
        verify(vehicleInfoMapper, never()).selectById(anyInt());
    }

    @Test
    void getVehicle_FromDatabase() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(vehicleInfoMapper.selectById(1)).thenReturn(testVehicle);

        VehicleInfo result = vehicleInfoService.getVehicle(1);

        assertNotNull(result);
        assertEquals(testVehicle.getCarId(), result.getCarId());
        verify(valueOperations).get(anyString());
        verify(vehicleInfoMapper).selectById(1);
        verify(valueOperations).set(anyString(), any(VehicleInfo.class), anyLong(), any());
    }

    @Test
    void updateVehicle_Success() {
        when(vehicleInfoMapper.update(any(VehicleInfo.class))).thenReturn(1);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        int result = vehicleInfoService.updateVehicle(testVehicle);

        assertEquals(1, result);
        verify(vehicleInfoMapper).update(any(VehicleInfo.class));
        verify(valueOperations).set(anyString(), any(VehicleInfo.class), anyLong(), any());
    }

    @Test
    void deleteVehicle_Success() {
        when(vehicleInfoMapper.delete(1)).thenReturn(1);

        int result = vehicleInfoService.deleteVehicle(1);

        assertEquals(1, result);
        verify(vehicleInfoMapper).delete(1);
        verify(redisTemplate).delete(anyString());
    }
} 