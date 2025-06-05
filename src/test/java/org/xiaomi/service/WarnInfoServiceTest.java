package org.xiaomi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.xiaomi.dto.WarnInfoDTO;
import org.xiaomi.dto.WarnInfoVO;
import org.xiaomi.dto.Signal;
import org.xiaomi.entity.SignalRule;
import org.xiaomi.entity.VehicleInfo;
import org.xiaomi.entity.WarnInfo;
import org.xiaomi.mapper.SignalRuleMapper;
import org.xiaomi.mapper.VehicleInfoMapper;
import org.xiaomi.mapper.WarnInfoMapper;
import org.xiaomi.service.impl.WarnInfoServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarnInfoServiceTest {

    @Mock
    private WarnInfoMapper warnInfoMapper;

    @Mock
    private SignalRuleMapper signalRuleMapper;

    @Mock
    private VehicleInfoMapper vehicleInfoMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private WarnInfoServiceImpl warnInfoService;

    private WarnInfoDTO warnInfoDTO;
    private VehicleInfo vehicleInfo;
    private SignalRule signalRule;
    private List<WarnInfoVO> warnInfoVOList;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        warnInfoDTO = new WarnInfoDTO();
        warnInfoDTO.setCarId(1);
        Signal signal = new Signal();
        signal.setMx(12.5);
        signal.setMi(11.5);
        signal.setIx(5.0);
        signal.setIi(4.0);
        warnInfoDTO.setSignal(signal);

        vehicleInfo = new VehicleInfo();
        vehicleInfo.setCarId(1);
        vehicleInfo.setBatteryType("Li-ion");

        signalRule = new SignalRule();
        signalRule.setId(1);
        signalRule.setWarnName("电压差报警");
        signalRule.setWarningRule("1.0<=(Mx - Mi)<2.0 报警等级：1;2.0<=(Mx - Mi) 报警等级：2");

        warnInfoVOList = new ArrayList<>();
    }

    @Test
    void addWarnInfo_Success() {
        when(vehicleInfoMapper.selectById(1)).thenReturn(vehicleInfo);
        when(signalRuleMapper.selectAll(null, "Li-ion")).thenReturn(Arrays.asList(signalRule));
        when(warnInfoMapper.insert(any(WarnInfo.class))).thenReturn(1);

        int result = warnInfoService.addWarnInfo(warnInfoDTO, warnInfoVOList);

        assertEquals(1, result);
        verify(warnInfoMapper).insert(any(WarnInfo.class));
        verify(redisTemplate, times(2)).delete(anyString());
        assertFalse(warnInfoVOList.isEmpty());
    }

    @Test
    void addWarnInfo_NoVehicle() {
        when(vehicleInfoMapper.selectById(1)).thenReturn(null);

        int result = warnInfoService.addWarnInfo(warnInfoDTO, warnInfoVOList);

        assertEquals(0, result);
        verify(warnInfoMapper, never()).insert(any(WarnInfo.class));
        verify(redisTemplate, never()).delete(anyString());
        assertTrue(warnInfoVOList.isEmpty());
    }

    @Test
    void getWarnsByCarId_FromCache() {
        List<WarnInfo> expectedWarns = Arrays.asList(new WarnInfo());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(expectedWarns);

        List<WarnInfo> result = warnInfoService.getWarnsByCarId(1);

        assertNotNull(result);
        assertEquals(expectedWarns.size(), result.size());
        verify(valueOperations).get(anyString());
        verify(warnInfoMapper, never()).selectByCarId(anyInt());
    }

    @Test
    void getWarnsByCarId_FromDatabase() {
        List<WarnInfo> expectedWarns = Arrays.asList(new WarnInfo());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(warnInfoMapper.selectByCarId(1)).thenReturn(expectedWarns);

        List<WarnInfo> result = warnInfoService.getWarnsByCarId(1);

        assertNotNull(result);
        assertEquals(expectedWarns.size(), result.size());
        verify(valueOperations).get(anyString());
        verify(warnInfoMapper).selectByCarId(1);
        verify(valueOperations).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void getAllWarns_FromCache() {
        List<WarnInfo> expectedWarns = Arrays.asList(new WarnInfo());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(expectedWarns);

        List<WarnInfo> result = warnInfoService.getAllWarns();

        assertNotNull(result);
        assertEquals(expectedWarns.size(), result.size());
        verify(valueOperations).get(anyString());
        verify(warnInfoMapper, never()).selectAll();
    }

    @Test
    void getAllWarns_FromDatabase() {
        List<WarnInfo> expectedWarns = Arrays.asList(new WarnInfo());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(warnInfoMapper.selectAll()).thenReturn(expectedWarns);

        List<WarnInfo> result = warnInfoService.getAllWarns();

        assertNotNull(result);
        assertEquals(expectedWarns.size(), result.size());
        verify(valueOperations).get(anyString());
        verify(warnInfoMapper).selectAll();
        verify(valueOperations).set(anyString(), anyList(), anyLong(), any());
    }
} 