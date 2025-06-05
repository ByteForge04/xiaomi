package org.xiaomi.service.impl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.xiaomi.dto.WarnInfoDTO;
import org.xiaomi.dto.WarnInfoVO;
import org.xiaomi.entity.SignalRule;
import org.xiaomi.entity.VehicleInfo;
import org.xiaomi.entity.WarnInfo;
import org.xiaomi.mapper.SignalRuleMapper;
import org.xiaomi.mapper.VehicleInfoMapper;
import org.xiaomi.mapper.WarnInfoMapper;
import org.xiaomi.service.WarnInfoService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WarnInfoServiceImpl implements WarnInfoService {
    @Autowired
    private WarnInfoMapper warnInfoMapper;
    @Autowired
    private SignalRuleMapper signalRuleMapper;
    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String WARN_CACHE_KEY_PREFIX = "warn:car:";
    private static final String WARN_ALL_CACHE_KEY = "warn:all";
    private static final long CACHE_EXPIRE_TIME = 30; // 缓存过期时间（分钟）

    private int determineWarningLevel(double value, String warningRule, String ruleType) {
        // 解析规则字符串
        String[] rules = warningRule.split(";");
        for (String rule : rules) {
            rule = rule.trim();
            if (rule.isEmpty()) continue;
            
            // 根据规则类型选择不同的正则表达式
            String patternStr;
            if ("电压差报警".equals(ruleType)) {
                patternStr = "(\\d+\\.?\\d*)<=\\(Mx - Mi\\)(?:<(\\d+\\.?\\d*))?.*报警等级：(\\d+)";
            } else {
                patternStr = "(\\d+\\.?\\d*)<=\\(Ix - Ii\\)(?:<(\\d+\\.?\\d*))?.*报警等级：(\\d+)";
            }
            
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(rule);
            
            if (matcher.find()) {
                double lowerBound = Double.parseDouble(matcher.group(1));
                String upperBoundStr = matcher.group(2);
                int level = Integer.parseInt(matcher.group(3));
                
                if (upperBoundStr != null) {
                    double upperBound = Double.parseDouble(upperBoundStr);
                    if (value >= lowerBound && value < upperBound) {
                        return level;
                    }
                } else {
                    if (value >= lowerBound) {
                        return level;
                    }
                }
            }
        }
        return -1; // 没有匹配的规则
    }

    @Override
    public int addWarnInfo(WarnInfoDTO warnInfoDTO, List<WarnInfoVO> warnInfoVOList) {
        int result = addWarnInfoInternal(warnInfoDTO, warnInfoVOList);
        if (result > 0) {
            // 添加成功后清除缓存
            clearWarnCache(warnInfoDTO.getCarId());
        }
        return result;
    }

    private int addWarnInfoInternal(WarnInfoDTO warnInfoDTO, List<WarnInfoVO> warnInfoVOList) {
        log.info("添加报警信息：{}", warnInfoDTO);
        if (warnInfoDTO == null || warnInfoDTO.getSignal() == null) {
            return 0;
        }

        // 获取车辆信息
        VehicleInfo vehicleInfo = vehicleInfoMapper.selectById(warnInfoDTO.getCarId());
        if (vehicleInfo == null) {
            return 0;
        }

        // 获取该车辆类型的所有规则
        List<SignalRule> rules = signalRuleMapper.selectAll(null, vehicleInfo.getBatteryType());
        if (rules.isEmpty()) {
            return 0;
        }

        boolean hasWarning = false;
        double mx=0;
        double mi=0;
        double ix=0;
        double ii=0;
        if ( warnInfoDTO.getSignal().getMx()!=  null)mx = warnInfoDTO.getSignal().getMx();
        if ( warnInfoDTO.getSignal().getMi()!=  null)mi = warnInfoDTO.getSignal().getMi();
        if (warnInfoDTO.getSignal().getIx()!=  null)ix = warnInfoDTO.getSignal().getIx();
        if (warnInfoDTO.getSignal().getIi()!=  null)ii = warnInfoDTO.getSignal().getIi();
        
        // 遍历所有规则，找到匹配的规则
        for (SignalRule rule : rules) {
            int warningLevel = -1;
            double signalValue = 0;
            
            // 根据规则类型判断是电压差还是电流差
            if ("电压差报警".equals(rule.getWarnName())) {
                signalValue = mx - mi;
                warningLevel = determineWarningLevel(signalValue, rule.getWarningRule(), rule.getWarnName());
            } else if ("电流差报警".equals(rule.getWarnName())) {
                signalValue = ix - ii;
                warningLevel = determineWarningLevel(signalValue, rule.getWarningRule(), rule.getWarnName());
            }
            
            if (warningLevel >= 0) {
                WarnInfo warnInfo = new WarnInfo();
                warnInfo.setCarId(warnInfoDTO.getCarId());
                warnInfo.setRuleId(rule.getId());
                warnInfo.setWarnType(rule.getWarnName());
                warnInfo.setWarnMsg(rule.getWarnName() + " - 等级: " + warningLevel);
                warnInfo.setSignalValue(signalValue);
                log.info("添加报警信息：{}", warnInfo);
                warnInfoMapper.insert(warnInfo);
                warnInfoVOList.add(new WarnInfoVO(warnInfoDTO.getCarId(), vehicleInfo.getBatteryType(), rule.getWarnName(), "等级: " + warningLevel));
                hasWarning = true;
            }
        }
        return hasWarning ? 1 : 0;
    }

    @Override
    public List<WarnInfo> getWarnsByCarId(Integer carId) {
        String cacheKey = WARN_CACHE_KEY_PREFIX + carId;
        
        // 尝试从缓存获取
        List<WarnInfo> cachedWarns = (List<WarnInfo>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedWarns != null) {
            log.info("从缓存获取车辆[{}]的报警信息", carId);
            // 如果是空值缓存，返回空列表
            if (cachedWarns.isEmpty()) {
                return new ArrayList<>();
            }
            return cachedWarns;
        }

        // 缓存未命中，从数据库查询
        List<WarnInfo> warns = warnInfoMapper.selectByCarId(carId);
        
        // 将结果存入缓存，包括空值
        if (warns != null) {
            // 设置较短的过期时间，防止缓存穿透
            long expireTime = warns.isEmpty() ? 5 : CACHE_EXPIRE_TIME;
            redisTemplate.opsForValue().set(cacheKey, warns, expireTime, TimeUnit.MINUTES);
            log.info("将车辆[{}]的报警信息存入缓存，过期时间：{}分钟", carId, expireTime);
        }
        
        return warns != null ? warns : new ArrayList<>();
    }

    @Override
    public List<WarnInfo> getAllWarns() {
        // 尝试从缓存获取
        List<WarnInfo> cachedWarns = (List<WarnInfo>) redisTemplate.opsForValue().get(WARN_ALL_CACHE_KEY);
        if (cachedWarns != null) {
            log.info("从缓存获取所有报警信息");
            return cachedWarns;
        }

        // 缓存未命中，从数据库查询
        List<WarnInfo> warns = warnInfoMapper.selectAll();
        
        // 将结果存入缓存
        if (warns != null && !warns.isEmpty()) {
            redisTemplate.opsForValue().set(WARN_ALL_CACHE_KEY, warns, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            log.info("将所有报警信息存入缓存");
        }
        
        return warns;
    }

    // 在添加报警信息时清除相关缓存
    private void clearWarnCache(Integer carId) {
        // 清除特定车辆的缓存
        String carCacheKey = WARN_CACHE_KEY_PREFIX + carId;
        redisTemplate.delete(carCacheKey);
        
        // 清除所有报警信息的缓存
        redisTemplate.delete(WARN_ALL_CACHE_KEY);
        
        log.info("清除报警信息缓存 - 车辆ID: {}", carId);
    }
}
