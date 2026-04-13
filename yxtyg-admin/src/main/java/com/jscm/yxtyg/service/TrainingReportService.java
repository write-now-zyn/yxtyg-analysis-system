package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.TrainingReportQueryDTO;
import com.jscm.yxtyg.entity.TrainingReport;
import com.jscm.yxtyg.vo.TrainingReportDetailVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 转培上报服务接口
 */
public interface TrainingReportService extends IService<TrainingReport> {

    /**
     * 分页查询上报记录
     */
    PageResult<TrainingReport> queryPage(TrainingReportQueryDTO queryDTO);

    /**
     * 获取上报详情（含培训记录）
     */
    TrainingReportDetailVO getDetail(Long id);

    /**
     * 保存上报记录（含培训记录）
     */
    void saveWithRecords(TrainingReportDetailVO vo);

    /**
     * 删除上报记录（级联删除培训记录）
     */
    void deleteWithRecords(Long id);

    /**
     * 导出Excel
     */
    void exportExcel(TrainingReportQueryDTO queryDTO, HttpServletResponse response);

}
