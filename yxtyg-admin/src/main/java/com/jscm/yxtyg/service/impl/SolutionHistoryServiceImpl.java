package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.entity.SolutionHistory;
import com.jscm.yxtyg.mapper.SolutionHistoryMapper;
import com.jscm.yxtyg.service.SolutionHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 解决方案历史服务实现类
 */
@Slf4j
@Service
public class SolutionHistoryServiceImpl extends ServiceImpl<SolutionHistoryMapper, SolutionHistory> implements SolutionHistoryService {

    @Override
    public List<SolutionHistory> listByWorkOrderId(Long workOrderId) {
        LambdaQueryWrapper<SolutionHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SolutionHistory::getWorkOrderId, workOrderId);
        wrapper.orderByAsc(SolutionHistory::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public void deleteByWorkOrderId(Long workOrderId) {
        LambdaQueryWrapper<SolutionHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SolutionHistory::getWorkOrderId, workOrderId);
        this.remove(wrapper);
        log.debug("删除工单{}的解决方案历史", workOrderId);
    }

    @Override
    public void saveBatch(List<SolutionHistory> list) {
        if (list != null && !list.isEmpty()) {
            this.saveBatch(list, list.size());
            log.debug("批量保存解决方案历史{}条", list.size());
        }
    }
}
