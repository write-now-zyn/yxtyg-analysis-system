package com.jscm.yxtyg.controller;

import com.alibaba.excel.EasyExcel;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.dto.ExperimenterExcelDTO;
import com.jscm.yxtyg.dto.ExperimenterQueryDTO;
import com.jscm.yxtyg.entity.Experimenter;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.service.ExperimenterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 体验官控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/experimenter")
@RequirePermissions(PermissionConstants.DATA_MANAGE)
public class ExperimenterController {

    @Autowired
    private ExperimenterService experimenterService;

    /**
     * 获取操作人（暂时从请求头获取，后续可对接登录系统）
     */
    private String getOperator(HttpServletRequest request) {
        String operator = request.getHeader("X-User-Name");
        return operator != null ? operator : "系统管理员";
    }

    /**
     * 分页查询体验官
     */
    @GetMapping("/list")
    public Result<PageResult<Experimenter>> list(ExperimenterQueryDTO queryDTO) {
        log.info("收到查询体验官列表请求，条件：{}", queryDTO);
        return Result.success(experimenterService.queryPage(queryDTO));
    }

    /**
     * 获取所有体验官（下拉选择用）
     */
    @GetMapping("/all")
    public Result<List<Experimenter>> all() {
        log.info("收到获取所有体验官请求");
        return Result.success(experimenterService.listAll());
    }

    /**
     * 根据ID获取体验官
     */
    @GetMapping("/{id}")
    public Result<Experimenter> getById(@PathVariable Long id) {
        log.info("收到获取体验官详情请求，ID：{}", id);
        return Result.success(experimenterService.getById(id));
    }

    /**
     * 新增体验官
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Experimenter experimenter, HttpServletRequest request) {
        log.info("收到新增体验官请求：{}", experimenter);
        if (experimenter.getIsContact() == null) {
            experimenter.setIsContact(0);
        }
        experimenterService.saveExperimenter(experimenter, getOperator(request));
        log.info("新增体验官成功，ID：{}", experimenter.getId());
        return Result.success();
    }

    /**
     * 更新体验官
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Experimenter experimenter, HttpServletRequest request) {
        log.info("收到更新体验官请求：{}", experimenter);
        experimenterService.saveExperimenter(experimenter, getOperator(request));
        log.info("更新体验官成功，ID：{}", experimenter.getId());
        return Result.success();
    }

    /**
     * 删除体验官
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        log.info("收到删除体验官请求，ID：{}", id);
        experimenterService.deleteExperimenter(id, getOperator(request));
        log.info("删除体验官成功，ID：{}", id);
        return Result.success();
    }

    /**
     * 批量删除体验官
     */
    @DeleteMapping("/batchDelete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids, HttpServletRequest request) {
        log.info("收到批量删除体验官请求，共{}条", ids.size());
        experimenterService.batchDelete(ids, getOperator(request));
        log.info("批量删除体验官成功");
        return Result.success();
    }

    /**
     * 导出Excel（带样式）
     */
    @GetMapping("/export")
    public void export(ExperimenterQueryDTO queryDTO, HttpServletResponse response) {
        log.info("收到导出体验官Excel请求，条件：{}", queryDTO);
        experimenterService.exportExcel(queryDTO, response);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        log.info("收到下载体验官导入模板请求");
        experimenterService.downloadTemplate(response);
    }

    /**
     * 批量导入体验官
     */
    @PostMapping("/import")
    public Result<String> importExcel(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        log.info("收到批量导入体验官请求，文件名：{}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }
        
        try {
            List<ExperimenterExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(ExperimenterExcelDTO.class)
                    .sheet()
                    .doReadSync();
            
            String result = experimenterService.importExperimenters(list, getOperator(request));
            log.info("批量导入体验官完成：{}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量导入体验官失败", e);
            return Result.error(500, "导入失败：" + e.getMessage());
        }
    }

    /**
     * 设置/取消接口人
     */
    @PutMapping("/setContact/{id}")
    public Result<Void> setContact(@PathVariable Long id, @RequestParam Integer isContact) {
        log.info("收到设置接口人请求，ID：{}，是否接口人：{}", id, isContact);
        experimenterService.setContact(id, isContact);
        log.info("设置接口人成功，ID：{}，是否接口人：{}", id, isContact);
        return Result.success();
    }

}
