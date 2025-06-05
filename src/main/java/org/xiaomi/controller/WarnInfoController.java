package org.xiaomi.controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xiaomi.dto.WarnInfoDTO;
import org.xiaomi.dto.WarnInfoVO;
import org.xiaomi.entity.WarnInfo;
import org.xiaomi.entity.Result;
import org.xiaomi.service.WarnInfoService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class WarnInfoController {
    @Autowired
    private WarnInfoService warnInfoService;
    
    @PostMapping("/warn")
    public Result<List<WarnInfoVO>> addWarnInfo(@RequestBody List<WarnInfoDTO> warnInfoDTOList) {
        log.info("批量新增报警信息：{}", warnInfoDTOList);
        int successCount = 0;
        List<WarnInfoVO> warnInfoVOList = new ArrayList<>();
        for (WarnInfoDTO warnInfoDTO : warnInfoDTOList) {
            int res = warnInfoService.addWarnInfo(warnInfoDTO,  warnInfoVOList);
            if (res > 0) {
                successCount++;
            }
        }
        if (successCount > 0) {
            return Result.success(warnInfoVOList);
        } else {
            return Result.error("新增失败");
        }
    }

    @GetMapping("/list/{carId}")
    public Result<List<WarnInfo>> getWarnsByCarId(@PathVariable Integer carId) {
        return Result.success(warnInfoService.getWarnsByCarId(carId));
    }

    @GetMapping("/all")
    public Result<List<WarnInfo>> getAllWarns() {
        return Result.success(warnInfoService.getAllWarns());
    }
}