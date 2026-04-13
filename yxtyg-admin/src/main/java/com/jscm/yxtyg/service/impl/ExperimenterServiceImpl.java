package com.jscm.yxtyg.service.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.ExperimenterExcelDTO;
import com.jscm.yxtyg.dto.ExperimenterQueryDTO;
import com.jscm.yxtyg.entity.Experimenter;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.ExperimenterMapper;
import com.jscm.yxtyg.service.ExperimenterLogService;
import com.jscm.yxtyg.service.ExperimenterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 体验官服务实现类
 */
@Slf4j
@Service
public class ExperimenterServiceImpl extends ServiceImpl<ExperimenterMapper, Experimenter> implements ExperimenterService {

    /** 中文名字正则：只能包含中文汉字 */
    private static final Pattern CHINESE_NAME_PATTERN = Pattern.compile("^[\u4e00-\u9fa5]+$");
    
    /** 手机号正则：11位数字，以1开头 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    /** 邮箱正则 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Autowired
    private ExperimenterLogService experimenterLogService;

    @Override
    public PageResult<Experimenter> queryPage(ExperimenterQueryDTO queryDTO) {
        log.debug("查询体验官列表，条件：{}", queryDTO);
        
        LambdaQueryWrapper<Experimenter> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getCity())) {
            wrapper.eq(Experimenter::getCity, queryDTO.getCity());
        }
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Experimenter::getName, queryDTO.getName());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(Experimenter::getPhone, queryDTO.getPhone());
        }
        if (queryDTO.getIsContact() != null) {
            wrapper.eq(Experimenter::getIsContact, queryDTO.getIsContact());
        }
        
        // 排序优先级：是接口人 > 同地市 > 姓名
        wrapper.orderByDesc(Experimenter::getIsContact)
               .orderByAsc(Experimenter::getCity)
               .orderByAsc(Experimenter::getName);
        
        Page<Experimenter> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Experimenter> result = this.page(page, wrapper);
        
        log.info("查询体验官列表完成，共{}条记录", result.getTotal());
        return PageResult.of(result);
    }

    @Override
    public List<Experimenter> listAll() {
        log.debug("获取所有体验官列表");
        
        LambdaQueryWrapper<Experimenter> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Experimenter::getCity, Experimenter::getName);
        List<Experimenter> list = this.list(wrapper);
        
        log.info("获取所有体验官完成，共{}条记录", list.size());
        return list;
    }

    @Override
    public Experimenter getByNameAndCity(String name, String city) {
        log.debug("根据姓名[{}]和地市[{}]查询体验官", name, city);
        
        LambdaQueryWrapper<Experimenter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Experimenter::getName, name);
        wrapper.eq(Experimenter::getCity, city);
        Experimenter experimenter = this.getOne(wrapper);
        
        if (experimenter != null) {
            log.debug("找到体验官：{}", experimenter);
        } else {
            log.debug("未找到匹配的体验官");
        }
        return experimenter;
    }

    @Override
    public Experimenter getByName(String name) {
        log.debug("根据姓名[{}]查询体验官", name);
        
        LambdaQueryWrapper<Experimenter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Experimenter::getName, name);
        wrapper.last("LIMIT 1");
        Experimenter experimenter = this.getOne(wrapper);
        
        if (experimenter != null) {
            log.debug("找到体验官：{}", experimenter);
        } else {
            log.debug("未找到匹配的体验官");
        }
        return experimenter;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveExperimenter(Experimenter experimenter, String operator) {
        log.info("保存体验官，ID：{}，姓名：{}，手机号：{}，操作人：{}", experimenter.getId(), experimenter.getName(), experimenter.getPhone(), operator);
        
        // 验证姓名：必须是中文
        if (StringUtils.hasText(experimenter.getName())) {
            if (!CHINESE_NAME_PATTERN.matcher(experimenter.getName()).matches()) {
                log.warn("姓名格式不正确：{}", experimenter.getName());
                throw new BusinessException(400, "姓名必须是中文汉字");
            }
        }
        
        // 验证手机号：必须是11位手机号
        if (StringUtils.hasText(experimenter.getPhone())) {
            if (!PHONE_PATTERN.matcher(experimenter.getPhone()).matches()) {
                log.warn("手机号格式不正确：{}", experimenter.getPhone());
                throw new BusinessException(400, "手机号必须是11位有效手机号");
            }
        }
        
        // 验证邮箱格式
        if (StringUtils.hasText(experimenter.getEmail())) {
            if (!EMAIL_PATTERN.matcher(experimenter.getEmail()).matches()) {
                log.warn("邮箱格式不正确：{}", experimenter.getEmail());
                throw new BusinessException(400, "邮箱格式不正确");
            }
        }
        
        // 手机号判重
        if (StringUtils.hasText(experimenter.getPhone())) {
            LambdaQueryWrapper<Experimenter> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(Experimenter::getPhone, experimenter.getPhone());
            // 编辑时排除自身
            if (experimenter.getId() != null) {
                phoneWrapper.ne(Experimenter::getId, experimenter.getId());
            }
            Experimenter existByPhone = this.getOne(phoneWrapper);
            if (existByPhone != null) {
                log.warn("手机号已存在：{}，地市：{}，姓名：{}", experimenter.getPhone(), existByPhone.getCity(), existByPhone.getName());
                throw new BusinessException(400, String.format("该手机号已存在，地市：%s，姓名：%s", existByPhone.getCity(), existByPhone.getName()));
            }
        }
        
        // 如果设置为接口人，先清除该地市其他人的接口人标识
        if (experimenter.getIsContact() != null && experimenter.getIsContact() == 1 && StringUtils.hasText(experimenter.getCity())) {
            clearOtherContact(experimenter.getCity(), experimenter.getId());
        }
        
        // 保存或更新
        boolean isNew = experimenter.getId() == null;
        if (isNew) {
            experimenter.setCreateTime(LocalDateTime.now());
            experimenter.setUpdateTime(LocalDateTime.now());
            this.save(experimenter);
            log.info("新增体验官成功，ID：{}", experimenter.getId());
        } else {
            experimenter.setUpdateTime(LocalDateTime.now());
            this.updateById(experimenter);
            log.info("更新体验官成功，ID：{}", experimenter.getId());
        }
        
        // 记录日志
        if (isNew) {
            experimenterLogService.logAdd(experimenter, operator);
        } else {
            experimenterLogService.logUpdate(experimenter, operator);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExperimenter(Long id, String operator) {
        log.info("删除体验官，ID：{}，操作人：{}", id, operator);
        
        Experimenter experimenter = this.getById(id);
        if (experimenter == null) {
            throw new BusinessException(400, "体验官不存在");
        }
        
        // 记录删除日志
        experimenterLogService.logDelete(experimenter, operator);
        
        // 删除
        this.removeById(id);
        log.info("删除体验官成功，ID：{}，姓名：{}", id, experimenter.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids, String operator) {
        log.info("批量删除体验官，共{}条，操作人：{}", ids.size(), operator);
        
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "删除ID列表不能为空");
        }
        
        // 查询要删除的记录
        List<Experimenter> experimenters = this.listByIds(ids);
        if (experimenters.isEmpty()) {
            throw new BusinessException(400, "未找到要删除的体验官");
        }
        
        // 记录批量删除日志
        experimenterLogService.logBatchDelete(experimenters, operator);
        
        // 批量删除
        this.removeByIds(ids);
        log.info("批量删除体验官成功，共{}条", experimenters.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setContact(Long id, Integer isContact) {
        log.info("设置接口人，ID：{}，是否接口人：{}", id, isContact);
        
        Experimenter experimenter = this.getById(id);
        if (experimenter == null) {
            throw new BusinessException(400, "体验官不存在");
        }
        
        // 如果设置为接口人，先清除该地市其他人的接口人标识
        if (isContact == 1 && StringUtils.hasText(experimenter.getCity())) {
            clearOtherContact(experimenter.getCity(), id);
        }
        
        // 更新当前人员
        Experimenter update = new Experimenter();
        update.setId(id);
        update.setIsContact(isContact);
        update.setUpdateTime(LocalDateTime.now());
        this.updateById(update);
        
        log.info("设置接口人成功，ID：{}，地市：{}，是否接口人：{}", id, experimenter.getCity(), isContact);
    }
    
    /**
     * 清除某地市其他人的接口人标识
     */
    private void clearOtherContact(String city, Long excludeId) {
        log.debug("清除地市[{}]其他人的接口人标识，排除ID：{}", city, excludeId);
        
        LambdaUpdateWrapper<Experimenter> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Experimenter::getCity, city)
                .eq(Experimenter::getIsContact, 1)
                .set(Experimenter::getIsContact, 0)
                .set(Experimenter::getUpdateTime, LocalDateTime.now());
        
        if (excludeId != null) {
            updateWrapper.ne(Experimenter::getId, excludeId);
        }
        
        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importExperimenters(List<ExperimenterExcelDTO> list, String operator) {
        log.info("开始批量导入体验官，共{}条数据，操作人：{}", list.size(), operator);
        
        if (list == null || list.isEmpty()) {
            throw new BusinessException(400, "导入数据不能为空");
        }
        
        int successCount = 0;
        int failCount = 0;
        List<String> errorMessages = new ArrayList<>();
        List<Experimenter> successList = new ArrayList<>();
        
        for (int i = 0; i < list.size(); i++) {
            ExperimenterExcelDTO dto = list.get(i);
            int rowNum = i + 2; // Excel行号从2开始（第1行是标题）
            
            try {
                // 校验必填字段
                if (!StringUtils.hasText(dto.getCity())) {
                    errorMessages.add(String.format("第%d行：地市不能为空", rowNum));
                    failCount++;
                    continue;
                }
                if (!StringUtils.hasText(dto.getName())) {
                    errorMessages.add(String.format("第%d行：姓名不能为空", rowNum));
                    failCount++;
                    continue;
                }
                if (!StringUtils.hasText(dto.getPhone())) {
                    errorMessages.add(String.format("第%d行：电话不能为空", rowNum));
                    failCount++;
                    continue;
                }
                
                // 校验姓名格式
                if (!CHINESE_NAME_PATTERN.matcher(dto.getName()).matches()) {
                    errorMessages.add(String.format("第%d行：姓名[%s]必须是中文汉字", rowNum, dto.getName()));
                    failCount++;
                    continue;
                }
                
                // 校验手机号格式
                if (!PHONE_PATTERN.matcher(dto.getPhone()).matches()) {
                    errorMessages.add(String.format("第%d行：手机号[%s]格式不正确，必须是11位有效手机号", rowNum, dto.getPhone()));
                    failCount++;
                    continue;
                }
                
                // 校验邮箱格式
                if (StringUtils.hasText(dto.getEmail()) && !EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
                    errorMessages.add(String.format("第%d行：邮箱[%s]格式不正确", rowNum, dto.getEmail()));
                    failCount++;
                    continue;
                }
                
                // 检查手机号是否已存在
                LambdaQueryWrapper<Experimenter> phoneWrapper = new LambdaQueryWrapper<>();
                phoneWrapper.eq(Experimenter::getPhone, dto.getPhone());
                Experimenter existByPhone = this.getOne(phoneWrapper);
                if (existByPhone != null) {
                    errorMessages.add(String.format("第%d行：手机号[%s]已存在，地市：%s，姓名：%s", 
                            rowNum, dto.getPhone(), existByPhone.getCity(), existByPhone.getName()));
                    failCount++;
                    continue;
                }
                
                // 保存体验官
                Experimenter experimenter = new Experimenter();
                experimenter.setCity(dto.getCity());
                experimenter.setName(dto.getName());
                experimenter.setPhone(dto.getPhone());
                experimenter.setEmail(dto.getEmail());
                experimenter.setRole(dto.getRole());
                experimenter.setRemark(dto.getRemark());
                experimenter.setIsContact(0);
                experimenter.setCreateTime(LocalDateTime.now());
                experimenter.setUpdateTime(LocalDateTime.now());
                this.save(experimenter);
                successList.add(experimenter);
                successCount++;
                
            } catch (Exception e) {
                log.error("导入第{}行数据失败", rowNum, e);
                errorMessages.add(String.format("第%d行：导入失败，%s", rowNum, e.getMessage()));
                failCount++;
            }
        }
        
        // 记录批量新增日志
        if (!successList.isEmpty()) {
            experimenterLogService.logBatchAdd(successList, operator);
        }
        
        log.info("批量导入完成，成功{}条，失败{}条", successCount, failCount);
        
        // 返回导入结果
        StringBuilder result = new StringBuilder();
        result.append(String.format("导入完成：成功%d条，失败%d条", successCount, failCount));
        if (!errorMessages.isEmpty()) {
            result.append("。失败详情：").append(String.join("；", errorMessages));
        }
        return result.toString();
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        log.info("开始下载体验官导入模板");
        
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("体验官导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 创建空数据列表，只输出标题行
            List<ExperimenterExcelDTO> emptyList = new ArrayList<>();
            
            EasyExcel.write(response.getOutputStream(), ExperimenterExcelDTO.class)
                    .sheet("体验官导入模板")
                    .doWrite(emptyList);
            
            log.info("下载体验官导入模板成功");
        } catch (IOException e) {
            log.error("下载体验官导入模板失败", e);
            throw new RuntimeException("下载模板失败", e);
        }
    }

    @Override
    public void exportExcel(ExperimenterQueryDTO queryDTO, HttpServletResponse response) {
        log.info("开始导出体验官Excel，条件：{}", queryDTO);
        
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("体验官列表", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 查询数据
            queryDTO.setCurrent(1L);
            queryDTO.setSize(Long.MAX_VALUE);
            PageResult<Experimenter> pageResult = this.queryPage(queryDTO);
            
            // 转换为导出DTO
            List<ExperimenterExcelDTO> exportList = new ArrayList<>();
            for (Experimenter experimenter : pageResult.getRecords()) {
                ExperimenterExcelDTO dto = new ExperimenterExcelDTO();
                dto.setCity(experimenter.getCity());
                dto.setName(experimenter.getName());
                dto.setPhone(experimenter.getPhone());
                dto.setEmail(experimenter.getEmail());
                dto.setRole(experimenter.getRole());
                dto.setRemark(experimenter.getRemark());
                exportList.add(dto);
            }
            
            EasyExcel.write(response.getOutputStream(), ExperimenterExcelDTO.class)
                    .sheet("体验官列表")
                    .doWrite(exportList);
            
            log.info("导出体验官Excel成功，共{}条记录", exportList.size());
        } catch (IOException e) {
            log.error("导出体验官Excel失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

}
