package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.WorkOrderQueryDTO;
import com.jscm.yxtyg.entity.WorkOrder;
import com.jscm.yxtyg.vo.WorkOrderDetailVO;
import com.jscm.yxtyg.vo.WorkOrderListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 工单服务接口
 */
public interface WorkOrderService extends IService<WorkOrder> {

    /**
     * 分页查询工单列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<WorkOrderListVO> queryPage(WorkOrderQueryDTO queryDTO);

    /**
     * 获取工单详情
     *
     * @param id 工单ID
     * @return 工单详情
     */
    WorkOrderDetailVO getDetail(Long id);

    /**
     * 导入工单Excel
     * 导入规则：
     * 1、按 orderNo 识别工单快照
     * 2、status、solutionHistoryStr、handlerName 或其他关键字段变化时更新
     * 3、满足可向量化条件时自动同步向量索引
     *
     * @param file Excel文件
     * @return 导入结果
     */
    Map<String, Object> importExcel(MultipartFile file);

    /**
     * 删除工单（同时删除关联的解决方案历史）
     *
     * @param id 工单ID
     */
    void deleteWorkOrder(Long id);

    /**
     * 批量删除工单
     *
     * @param ids 工单ID列表
     */
    void batchDeleteWorkOrder(java.util.List<Long> ids);
}
