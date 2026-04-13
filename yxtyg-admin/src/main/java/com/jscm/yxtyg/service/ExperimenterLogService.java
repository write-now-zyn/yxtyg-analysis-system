package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.entity.Experimenter;
import com.jscm.yxtyg.entity.ExperimenterLog;

import java.util.List;

/**
 * 体验官操作日志服务接口
 */
public interface ExperimenterLogService extends IService<ExperimenterLog> {

    /**
     * 记录新增日志
     */
    void logAdd(Experimenter experimenter, String operator);

    /**
     * 记录编辑日志
     */
    void logUpdate(Experimenter experimenter, String operator);

    /**
     * 记录删除日志
     */
    void logDelete(Experimenter experimenter, String operator);

    /**
     * 记录批量新增日志
     */
    void logBatchAdd(List<Experimenter> experimenters, String operator);

    /**
     * 记录批量删除日志
     */
    void logBatchDelete(List<Experimenter> experimenters, String operator);

}
