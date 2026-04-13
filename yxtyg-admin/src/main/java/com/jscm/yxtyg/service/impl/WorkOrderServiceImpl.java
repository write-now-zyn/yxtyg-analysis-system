package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.WorkOrderExcelDTO;
import com.jscm.yxtyg.dto.WorkOrderQueryDTO;
import com.jscm.yxtyg.entity.SolutionHistory;
import com.jscm.yxtyg.entity.WorkOrder;
import com.jscm.yxtyg.mapper.WorkOrderMapper;
import com.jscm.yxtyg.service.SolutionHistoryService;
import com.jscm.yxtyg.service.WorkOrderService;
import com.jscm.yxtyg.util.WorkOrderExcelParser;
import com.jscm.yxtyg.vo.WorkOrderDetailVO;
import com.jscm.yxtyg.vo.WorkOrderListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工单服务实现类
 */
@Slf4j
@Service
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder> implements WorkOrderService {

    @Autowired
    private SolutionHistoryService solutionHistoryService;

    @Autowired
    private WorkOrderExcelParser excelParser;

    @Override
    public PageResult<WorkOrderListVO> queryPage(WorkOrderQueryDTO queryDTO) {
        log.debug("查询工单列表，条件：{}", queryDTO);

        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();

        // 问题编号模糊查询
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            wrapper.like(WorkOrder::getOrderNo, queryDTO.getOrderNo());
        }
        // 发起人姓名模糊查询
        if (StringUtils.hasText(queryDTO.getInitiatorName())) {
            wrapper.like(WorkOrder::getInitiatorName, queryDTO.getInitiatorName());
        }
        // 地市精确查询
        if (StringUtils.hasText(queryDTO.getCity())) {
            wrapper.eq(WorkOrder::getCity, queryDTO.getCity());
        }
        // 月份筛选（问题编号前6位匹配）
        if (StringUtils.hasText(queryDTO.getMonth())) {
            wrapper.likeRight(WorkOrder::getOrderNo, queryDTO.getMonth());
        }
        // 状态精确查询
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(WorkOrder::getStatus, queryDTO.getStatus());
        }
        // 关键词模糊搜索工单内容
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(WorkOrder::getContent, queryDTO.getKeyword())
                    .or().like(WorkOrder::getOrderNo, queryDTO.getKeyword())
                    .or().like(WorkOrder::getInitiatorName, queryDTO.getKeyword()));
        }

        // 排序：优先展示非"关闭"和"已回复"的，然后按问题编号降序、地市排序
        wrapper.last("ORDER BY CASE WHEN status = '关闭' THEN 2 WHEN status = '已回复' THEN 1 ELSE 0 END, order_no DESC, city ASC");

        Page<WorkOrder> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<WorkOrder> result = this.page(page, wrapper);

        // 转换为VO
        List<WorkOrderListVO> voList = result.getRecords().stream().map(workOrder -> {
            WorkOrderListVO vo = new WorkOrderListVO();
            BeanUtils.copyProperties(workOrder, vo);
            return vo;
        }).collect(Collectors.toList());

        log.info("查询工单列表完成，共{}条记录", result.getTotal());
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public WorkOrderDetailVO getDetail(Long id) {
        log.debug("获取工单详情，ID：{}", id);

        WorkOrder workOrder = this.getById(id);
        if (workOrder == null) {
            log.warn("工单不存在，ID：{}", id);
            return null;
        }

        WorkOrderDetailVO vo = new WorkOrderDetailVO();
        BeanUtils.copyProperties(workOrder, vo);

        // 查询解决方案历史列表
        List<SolutionHistory> historyList = solutionHistoryService.listByWorkOrderId(id);
        List<WorkOrderDetailVO.SolutionHistoryVO> historyVOList = historyList.stream()
                .map(history -> {
                    WorkOrderDetailVO.SolutionHistoryVO historyVO = new WorkOrderDetailVO.SolutionHistoryVO();
                    BeanUtils.copyProperties(history, historyVO);
                    return historyVO;
                }).collect(Collectors.toList());
        vo.setSolutionHistoryList(historyVOList);

        log.debug("获取工单详情成功，问题编号：{}", workOrder.getOrderNo());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importExcel(MultipartFile file) {
        log.info("开始导入工单Excel，文件名：{}，大小：{}字节", file.getOriginalFilename(), file.getSize());

        Map<String, Object> result = new HashMap<>();
        int insertCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        List<String> failMessages = new ArrayList<>();

        // 解析Excel文件
        WorkOrderExcelParser.ParseResult parseResult = excelParser.parse(file);
        
        if (!parseResult.getFailMessages().isEmpty()) {
            result.put("success", false);
            result.put("message", parseResult.getFailMessages().get(0));
            return result;
        }

        List<WorkOrderExcelDTO> dataList = parseResult.getSuccessList();
        log.info("解析完成，共{}条数据待处理", dataList.size());

        for (WorkOrderExcelDTO dto : dataList) {
            try {
                String orderNo = dto.getOrderNo();
                String status = dto.getStatus();

                // 查询是否存在相同流水号的工单
                LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(WorkOrder::getOrderNo, orderNo);
                WorkOrder existingOrder = this.getOne(wrapper);

                if (existingOrder != null) {
                    // 规则1：相同流水号且相同状态，跳过
                    if (existingOrder.getStatus() != null && existingOrder.getStatus().equals(status)) {
                        log.debug("工单已存在且状态相同，跳过：{}, 状态：{}", orderNo, status);
                        skipCount++;
                        continue;
                    }
                    
                    // 规则2：流水号存在但状态不同，更新全部字段
                    log.debug("工单存在但状态不同，更新：{}, 原状态：{}，新状态：{}", orderNo, existingOrder.getStatus(), status);
                    updateWorkOrderFromDTO(existingOrder, dto);
                    this.updateById(existingOrder);
                    
                    // 删除旧的解决方案历史，重新解析保存
                    solutionHistoryService.deleteByWorkOrderId(existingOrder.getId());
                    List<SolutionHistory> historyList = excelParser.parseSolutionHistory(
                            dto.getSolutionHistoryStr(), orderNo);
                    if (!historyList.isEmpty()) {
                        historyList.forEach(h -> h.setWorkOrderId(existingOrder.getId()));
                        solutionHistoryService.saveBatch(historyList);
                    }
                    updateCount++;
                } else {
                    // 新增工单
                    WorkOrder newOrder = new WorkOrder();
                    updateWorkOrderFromDTO(newOrder, dto);
                    newOrder.setCreatedAt(LocalDateTime.now());
                    newOrder.setUpdatedAt(LocalDateTime.now());
                    this.save(newOrder);

                    // 解析并保存解决方案历史
                    List<SolutionHistory> historyList = excelParser.parseSolutionHistory(
                            dto.getSolutionHistoryStr(), orderNo);
                    if (!historyList.isEmpty()) {
                        historyList.forEach(h -> h.setWorkOrderId(newOrder.getId()));
                        solutionHistoryService.saveBatch(historyList);
                    }
                    insertCount++;
                    log.debug("新增工单：{}", orderNo);
                }
            } catch (Exception e) {
                log.error("处理工单失败：{}", dto.getOrderNo(), e);
                failMessages.add("处理工单失败：" + dto.getOrderNo() + "，原因：" + e.getMessage());
            }
        }

        result.put("success", true);
        result.put("insertCount", insertCount);
        result.put("updateCount", updateCount);
        result.put("skipCount", skipCount);
        result.put("failCount", failMessages.size());
        result.put("failMessages", failMessages);
        result.put("message", String.format("导入完成！新增%d条，更新%d条，跳过%d条，失败%d条", 
                insertCount, updateCount, skipCount, failMessages.size()));

        log.info("导入工单完成，新增：{}，更新：{}，跳过：{}，失败：{}", 
                insertCount, updateCount, skipCount, failMessages.size());
        return result;
    }

    /**
     * 从DTO更新工单实体
     */
    private void updateWorkOrderFromDTO(WorkOrder workOrder, WorkOrderExcelDTO dto) {
        workOrder.setOrderNo(dto.getOrderNo());
        workOrder.setDeclarePhone(dto.getDeclarePhone());
        workOrder.setHandleUnit(dto.getHandleUnit());
        workOrder.setHandlerName(dto.getHandlerName());
        workOrder.setCity(dto.getCity());
        workOrder.setCreateTimeStr(dto.getCreateTimeStr());
        workOrder.setInitiatorName(dto.getInitiatorName());
        workOrder.setHandleSatisfactionScore(dto.getHandleSatisfactionScore());
        workOrder.setDispatchSatisfactionScore(dto.getDispatchSatisfactionScore());
        workOrder.setUnsatisfiedReason(dto.getUnsatisfiedReason());
        workOrder.setStatus(dto.getStatus());
        workOrder.setSolutionHistoryStr(dto.getSolutionHistoryStr());
        workOrder.setContent(dto.getContent());
        workOrder.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkOrder(Long id) {
        log.info("删除工单，ID：{}", id);
        // 先删除关联的解决方案历史
        solutionHistoryService.deleteByWorkOrderId(id);
        // 再删除工单
        this.removeById(id);
        log.info("删除工单成功，ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteWorkOrder(List<Long> ids) {
        log.info("批量删除工单，共{}条", ids.size());
        for (Long id : ids) {
            solutionHistoryService.deleteByWorkOrderId(id);
        }
        this.removeByIds(ids);
        log.info("批量删除工单成功");
    }
}
