package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.DemandWorkloadDTO;
import com.jscm.yxtyg.dto.DemandWorkloadQueryDTO;
import com.jscm.yxtyg.dto.FinalWorkloadDTO;
import com.jscm.yxtyg.dto.ReminderDTO;
import com.jscm.yxtyg.entity.DemandWorkload;
import com.jscm.yxtyg.vo.DemandWorkloadStatisticsVO;
import com.jscm.yxtyg.vo.DemandWorkloadVO;
import com.jscm.yxtyg.vo.ImportResultVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface DemandWorkloadService extends IService<DemandWorkload> {

    PageResult<DemandWorkloadVO> queryPage(DemandWorkloadQueryDTO queryDTO);

    DemandWorkloadVO getDetail(Long id);

    DemandWorkloadVO create(DemandWorkloadDTO dto);

    DemandWorkloadVO update(Long id, DemandWorkloadDTO dto);

    void deleteDemand(Long id);

    DemandWorkloadVO submitFinal(Long id, FinalWorkloadDTO dto);

    DemandWorkloadVO confirm(Long id);

    void remind(Long id, ReminderDTO dto);

    DemandWorkloadStatisticsVO statistics(DemandWorkloadQueryDTO queryDTO);

    ImportResultVO importExcel(MultipartFile file);

    void downloadTemplate(HttpServletResponse response);

    void exportExcel(DemandWorkloadQueryDTO queryDTO, HttpServletResponse response);
}
