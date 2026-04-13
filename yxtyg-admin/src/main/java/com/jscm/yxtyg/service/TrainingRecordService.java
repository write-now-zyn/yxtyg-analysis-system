package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.entity.TrainingRecord;

import java.util.List;

/**
 * 培训记录服务接口
 */
public interface TrainingRecordService extends IService<TrainingRecord> {

    /**
     * 根据上报ID查询培训记录
     */
    List<TrainingRecord> listByReportId(Long reportId);

    /**
     * 根据上报ID删除培训记录
     */
    void deleteByReportId(Long reportId);

}
