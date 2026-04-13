package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.ExperimenterExcelDTO;
import com.jscm.yxtyg.dto.ExperimenterQueryDTO;
import com.jscm.yxtyg.entity.Experimenter;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 体验官服务接口
 */
public interface ExperimenterService extends IService<Experimenter> {

    /**
     * 分页查询体验官
     */
    PageResult<Experimenter> queryPage(ExperimenterQueryDTO queryDTO);

    /**
     * 获取所有体验官（下拉选择用）
     */
    List<Experimenter> listAll();

    /**
     * 根据姓名和地市查询体验官
     */
    Experimenter getByNameAndCity(String name, String city);

    /**
     * 根据姓名模糊查询体验官
     */
    Experimenter getByName(String name);

    /**
     * 保存体验官（新增/编辑），包含手机号判重和接口人唯一性校验
     * @param experimenter 体验官信息
     * @param operator 操作人
     */
    void saveExperimenter(Experimenter experimenter, String operator);

    /**
     * 删除体验官
     * @param id 体验官ID
     * @param operator 操作人
     */
    void deleteExperimenter(Long id, String operator);

    /**
     * 批量删除体验官
     * @param ids 体验官ID列表
     * @param operator 操作人
     */
    void batchDelete(List<Long> ids, String operator);

    /**
     * 设置接口人（一个地市只能有一个接口人）
     */
    void setContact(Long id, Integer isContact);

    /**
     * 批量导入体验官
     * @param list 导入数据列表
     * @param operator 操作人
     * @return 导入结果信息
     */
    String importExperimenters(List<ExperimenterExcelDTO> list, String operator);

    /**
     * 下载导入模板
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导出体验官Excel（带样式）
     */
    void exportExcel(ExperimenterQueryDTO queryDTO, HttpServletResponse response);

}
