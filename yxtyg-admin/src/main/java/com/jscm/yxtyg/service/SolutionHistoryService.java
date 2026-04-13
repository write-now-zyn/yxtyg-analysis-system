package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.entity.SolutionHistory;

import java.util.List;

/**
 * 解决方案历史服务接口
 */
public interface SolutionHistoryService extends IService<SolutionHistory> {

    /**
     * 根据工单ID查询解决方案历史列表
     *
     * @param workOrderId 工单ID
     * @return 解决方案历史列表
     */
    List<SolutionHistory> listByWorkOrderId(Long workOrderId);

    /**
     * 根据工单ID删除解决方案历史
     *
     * @param workOrderId 工单ID
     */
    void deleteByWorkOrderId(Long workOrderId);

    /**
     * 批量保存解决方案历史
     *
     * @param list 解决方案历史列表
     */
    void saveBatch(List<SolutionHistory> list);
}
