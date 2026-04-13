package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.entity.Experimenter;
import com.jscm.yxtyg.entity.ExperimenterLog;
import com.jscm.yxtyg.mapper.ExperimenterLogMapper;
import com.jscm.yxtyg.service.ExperimenterLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 体验官操作日志服务实现类
 */
@Slf4j
@Service
public class ExperimenterLogServiceImpl extends ServiceImpl<ExperimenterLogMapper, ExperimenterLog> implements ExperimenterLogService {

    @Override
    @Async
    public void logAdd(Experimenter experimenter, String operator) {
        log.info("记录新增日志：{} - {}", experimenter.getCity(), experimenter.getName());
        
        ExperimenterLog logEntity = new ExperimenterLog();
        logEntity.setOperationType("新增");
        logEntity.setOperator(operator);
        logEntity.setOperationTime(LocalDateTime.now());
        logEntity.setExperimenterId(experimenter.getId());
        logEntity.setCity(experimenter.getCity());
        logEntity.setName(experimenter.getName());
        logEntity.setPhone(experimenter.getPhone());
        logEntity.setEmail(experimenter.getEmail());
        logEntity.setRole(experimenter.getRole());
        logEntity.setRemark(experimenter.getRemark());
        logEntity.setIsContact(experimenter.getIsContact());
        logEntity.setDetail(String.format("新增体验官：%s（%s，%s）", experimenter.getName(), experimenter.getCity(), experimenter.getPhone()));
        
        this.save(logEntity);
    }

    @Override
    @Async
    public void logUpdate(Experimenter experimenter, String operator) {
        log.info("记录编辑日志：{} - {}", experimenter.getCity(), experimenter.getName());
        
        ExperimenterLog logEntity = new ExperimenterLog();
        logEntity.setOperationType("编辑");
        logEntity.setOperator(operator);
        logEntity.setOperationTime(LocalDateTime.now());
        logEntity.setExperimenterId(experimenter.getId());
        logEntity.setCity(experimenter.getCity());
        logEntity.setName(experimenter.getName());
        logEntity.setPhone(experimenter.getPhone());
        logEntity.setEmail(experimenter.getEmail());
        logEntity.setRole(experimenter.getRole());
        logEntity.setRemark(experimenter.getRemark());
        logEntity.setIsContact(experimenter.getIsContact());
        logEntity.setDetail(String.format("编辑体验官：%s（%s，%s）", experimenter.getName(), experimenter.getCity(), experimenter.getPhone()));
        
        this.save(logEntity);
    }

    @Override
    @Async
    public void logDelete(Experimenter experimenter, String operator) {
        log.info("记录删除日志：{} - {}", experimenter.getCity(), experimenter.getName());
        
        ExperimenterLog logEntity = new ExperimenterLog();
        logEntity.setOperationType("删除");
        logEntity.setOperator(operator);
        logEntity.setOperationTime(LocalDateTime.now());
        logEntity.setExperimenterId(experimenter.getId());
        logEntity.setCity(experimenter.getCity());
        logEntity.setName(experimenter.getName());
        logEntity.setPhone(experimenter.getPhone());
        logEntity.setEmail(experimenter.getEmail());
        logEntity.setRole(experimenter.getRole());
        logEntity.setRemark(experimenter.getRemark());
        logEntity.setIsContact(experimenter.getIsContact());
        logEntity.setDetail(String.format("删除体验官：%s（%s，%s）", experimenter.getName(), experimenter.getCity(), experimenter.getPhone()));
        
        this.save(logEntity);
    }

    @Override
    @Async
    public void logBatchAdd(List<Experimenter> experimenters, String operator) {
        log.info("记录批量新增日志：共{}条", experimenters.size());
        
        for (Experimenter experimenter : experimenters) {
            ExperimenterLog logEntity = new ExperimenterLog();
            logEntity.setOperationType("批量新增");
            logEntity.setOperator(operator);
            logEntity.setOperationTime(LocalDateTime.now());
            logEntity.setExperimenterId(experimenter.getId());
            logEntity.setCity(experimenter.getCity());
            logEntity.setName(experimenter.getName());
            logEntity.setPhone(experimenter.getPhone());
            logEntity.setEmail(experimenter.getEmail());
            logEntity.setRole(experimenter.getRole());
            logEntity.setRemark(experimenter.getRemark());
            logEntity.setIsContact(experimenter.getIsContact());
            logEntity.setDetail(String.format("批量新增体验官：%s（%s，%s）", experimenter.getName(), experimenter.getCity(), experimenter.getPhone()));
            
            this.save(logEntity);
        }
    }

    @Override
    @Async
    public void logBatchDelete(List<Experimenter> experimenters, String operator) {
        log.info("记录批量删除日志：共{}条", experimenters.size());
        
        for (Experimenter experimenter : experimenters) {
            ExperimenterLog logEntity = new ExperimenterLog();
            logEntity.setOperationType("批量删除");
            logEntity.setOperator(operator);
            logEntity.setOperationTime(LocalDateTime.now());
            logEntity.setExperimenterId(experimenter.getId());
            logEntity.setCity(experimenter.getCity());
            logEntity.setName(experimenter.getName());
            logEntity.setPhone(experimenter.getPhone());
            logEntity.setEmail(experimenter.getEmail());
            logEntity.setRole(experimenter.getRole());
            logEntity.setRemark(experimenter.getRemark());
            logEntity.setIsContact(experimenter.getIsContact());
            logEntity.setDetail(String.format("批量删除体验官：%s（%s，%s）", experimenter.getName(), experimenter.getCity(), experimenter.getPhone()));
            
            this.save(logEntity);
        }
    }

}
