package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.entity.TrainingRecord;
import com.jscm.yxtyg.mapper.TrainingRecordMapper;
import com.jscm.yxtyg.service.TrainingRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 培训记录服务实现类
 */
@Slf4j
@Service
public class TrainingRecordServiceImpl extends ServiceImpl<TrainingRecordMapper, TrainingRecord> implements TrainingRecordService {

    @Override
    public List<TrainingRecord> listByReportId(Long reportId) {
        log.debug("根据上报ID[{}]查询培训记录", reportId);
        
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getReportId, reportId);
        wrapper.orderByAsc(TrainingRecord::getTrainingTime);
        List<TrainingRecord> list = this.list(wrapper);
        
        log.debug("查询到{}条培训记录", list.size());
        return list;
    }

    @Override
    public void deleteByReportId(Long reportId) {
        log.info("根据上报ID[{}]删除培训记录", reportId);
        
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getReportId, reportId);
        this.remove(wrapper);
        
        log.info("删除培训记录完成，上报ID：{}", reportId);
    }

}
