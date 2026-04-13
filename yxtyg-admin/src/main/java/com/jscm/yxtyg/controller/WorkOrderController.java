package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.WorkOrderQueryDTO;
import com.jscm.yxtyg.service.WorkOrderService;
import com.jscm.yxtyg.vo.WorkOrderDetailVO;
import com.jscm.yxtyg.vo.WorkOrderListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 工单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/workorder")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 分页查询工单列表
     */
    @GetMapping("/list")
    public Result<PageResult<WorkOrderListVO>> list(WorkOrderQueryDTO queryDTO) {
        log.info("查询工单列表，参数：{}", queryDTO);
        PageResult<WorkOrderListVO> result = workOrderService.queryPage(queryDTO);
        return Result.success(result);
    }

    /**
     * 获取工单详情
     */
    @GetMapping("/detail/{id}")
    public Result<WorkOrderDetailVO> detail(@PathVariable Long id) {
        log.info("获取工单详情，ID：{}", id);
        WorkOrderDetailVO detail = workOrderService.getDetail(id);
        if (detail == null) {
            return Result.error("工单不存在");
        }
        return Result.success(detail);
    }

    /**
     * 导入工单Excel
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        log.info("导入工单Excel，文件名：{}", file.getOriginalFilename());
        Map<String, Object> result = workOrderService.importExcel(file);
        if (Boolean.FALSE.equals(result.get("success"))) {
            return Result.error((String) result.get("message"));
        }
        return Result.success(result);
    }

    /**
     * 删除工单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除工单，ID：{}", id);
        workOrderService.deleteWorkOrder(id);
        return Result.success();
    }

    /**
     * 批量删除工单
     */
    @PostMapping("/batchDelete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        log.info("批量删除工单，共{}条", ids.size());
        workOrderService.batchDeleteWorkOrder(ids);
        return Result.success();
    }
}
