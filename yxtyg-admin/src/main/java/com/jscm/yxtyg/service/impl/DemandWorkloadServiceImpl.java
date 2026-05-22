package com.jscm.yxtyg.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.DemandWorkloadDTO;
import com.jscm.yxtyg.dto.DemandWorkloadExcelDTO;
import com.jscm.yxtyg.dto.DemandWorkloadQueryDTO;
import com.jscm.yxtyg.dto.FinalWorkloadDTO;
import com.jscm.yxtyg.dto.ReminderDTO;
import com.jscm.yxtyg.entity.DemandReminder;
import com.jscm.yxtyg.entity.DemandWorkload;
import com.jscm.yxtyg.entity.SysUser;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.DemandWorkloadMapper;
import com.jscm.yxtyg.security.AuthContext;
import com.jscm.yxtyg.security.CurrentUser;
import com.jscm.yxtyg.security.RoleConstants;
import com.jscm.yxtyg.service.DemandReminderService;
import com.jscm.yxtyg.service.DemandWorkloadService;
import com.jscm.yxtyg.service.NotificationService;
import com.jscm.yxtyg.service.SysUserService;
import com.jscm.yxtyg.vo.DemandWorkloadStatisticsVO;
import com.jscm.yxtyg.vo.DemandWorkloadVO;
import com.jscm.yxtyg.vo.ImportResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DemandWorkloadServiceImpl extends ServiceImpl<DemandWorkloadMapper, DemandWorkload> implements DemandWorkloadService {

    private static final String STATUS_PENDING = "待填写";
    private static final String STATUS_FILLED = "已填写";
    private static final String STATUS_CONFIRMED = "已核定";
    private static final List<String> VALID_STATUS = Arrays.asList(STATUS_PENDING, STATUS_FILLED, STATUS_CONFIRMED);

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private DemandReminderService demandReminderService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public PageResult<DemandWorkloadVO> queryPage(DemandWorkloadQueryDTO queryDTO) {
        LambdaQueryWrapper<DemandWorkload> wrapper = buildQueryWrapper(queryDTO);
        wrapper.orderByDesc(DemandWorkload::getUpdatedAt);
        Page<DemandWorkload> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        List<DemandWorkloadVO> records = page.getRecords().stream().map(item -> toVO(item, false)).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public DemandWorkloadVO getDetail(Long id) {
        DemandWorkload workload = getVisibleDemand(id);
        return toVO(workload, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandWorkloadVO create(DemandWorkloadDTO dto) {
        requireDevOrSystem();
        if (this.getOne(new LambdaQueryWrapper<DemandWorkload>().eq(DemandWorkload::getDemandNo, dto.getDemandNo()), false) != null) {
            throw new BusinessException("需求编号已存在");
        }
        DemandWorkload workload = new DemandWorkload();
        fillFromDTO(workload, dto);
        workload.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : STATUS_PENDING);
        validateStatus(workload.getStatus());
        workload.setCreatedAt(LocalDateTime.now());
        workload.setUpdatedAt(LocalDateTime.now());
        applyReduction(workload);
        this.save(workload);
        notificationService.createNotification(workload.getProductManagerId(), "新需求待填写",
                "需求「" + workload.getDemandName() + "」需要填写最终核定工作量", "DEMAND_WORKLOAD", workload.getId());
        return toVO(workload, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandWorkloadVO update(Long id, DemandWorkloadDTO dto) {
        requireDevOrSystem();
        DemandWorkload workload = this.getById(id);
        if (workload == null) {
            throw new BusinessException("需求不存在");
        }
        DemandWorkload duplicated = this.getOne(new LambdaQueryWrapper<DemandWorkload>()
                .eq(DemandWorkload::getDemandNo, dto.getDemandNo())
                .ne(DemandWorkload::getId, id), false);
        if (duplicated != null) {
            throw new BusinessException("需求编号已存在");
        }
        fillFromDTO(workload, dto);
        if (StringUtils.hasText(dto.getStatus())) {
            validateStatus(dto.getStatus());
            workload.setStatus(dto.getStatus());
        }
        workload.setUpdatedAt(LocalDateTime.now());
        applyReduction(workload);
        this.updateById(workload);
        return toVO(workload, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemand(Long id) {
        requireDevOrSystem();
        demandReminderService.remove(new LambdaQueryWrapper<DemandReminder>().eq(DemandReminder::getDemandId, id));
        notificationService.remove(new LambdaQueryWrapper<com.jscm.yxtyg.entity.Notification>()
                .and(w -> w.eq(com.jscm.yxtyg.entity.Notification::getBizType, "DEMAND_WORKLOAD")
                        .or().eq(com.jscm.yxtyg.entity.Notification::getBizType, "DEMAND_REMINDER"))
                .eq(com.jscm.yxtyg.entity.Notification::getBizId, id));
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandWorkloadVO submitFinal(Long id, FinalWorkloadDTO dto) {
        DemandWorkload workload = getVisibleDemand(id);
        CurrentUser user = AuthContext.get();
        if (RoleConstants.PRODUCT_MANAGER.equals(user.getRoleCode())
                && !workload.getProductManagerId().equals(user.getId())) {
            throw new BusinessException(403, "只能填写本人负责的需求");
        }
        if (STATUS_CONFIRMED.equals(workload.getStatus())) {
            throw new BusinessException("已核定需求不能再次填写");
        }
        workload.setFinalWorkload(dto.getFinalWorkload());
        workload.setRemark(dto.getRemark());
        workload.setStatus(STATUS_FILLED);
        workload.setUpdatedAt(LocalDateTime.now());
        applyReduction(workload);
        this.updateById(workload);
        notifyManagers(workload, "需求已填写", "需求「" + workload.getDemandName() + "」已提交最终核定工作量");
        return toVO(workload, true);
    }

    @Override
    public DemandWorkloadVO confirm(Long id) {
        requireDevOrSystem();
        DemandWorkload workload = this.getById(id);
        if (workload == null) {
            throw new BusinessException("需求不存在");
        }
        if (workload.getFinalWorkload() == null) {
            throw new BusinessException("请先填写最终核定工作量");
        }
        workload.setStatus(STATUS_CONFIRMED);
        workload.setUpdatedAt(LocalDateTime.now());
        applyReduction(workload);
        this.updateById(workload);
        notificationService.createNotification(workload.getProductManagerId(), "需求已核定",
                "需求「" + workload.getDemandName() + "」已完成核定", "DEMAND_WORKLOAD", workload.getId());
        return toVO(workload, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remind(Long id, ReminderDTO dto) {
        requireDevOrSystem();
        DemandWorkload workload = this.getById(id);
        if (workload == null) {
            throw new BusinessException("需求不存在");
        }
        DemandReminder reminder = new DemandReminder();
        reminder.setDemandId(id);
        reminder.setReminderBy(AuthContext.currentUserId());
        reminder.setReminderTo(workload.getProductManagerId());
        reminder.setReminderContent(StringUtils.hasText(dto.getContent()) ? dto.getContent() : "请尽快填写最终核定工作量");
        reminder.setCreatedAt(LocalDateTime.now());
        demandReminderService.save(reminder);
        notificationService.createNotification(workload.getProductManagerId(), "需求催办",
                "需求「" + workload.getDemandName() + "」：" + reminder.getReminderContent(),
                "DEMAND_REMINDER", id);
    }

    @Override
    public DemandWorkloadStatisticsVO statistics(DemandWorkloadQueryDTO queryDTO) {
        List<DemandWorkload> list = this.list(buildQueryWrapper(queryDTO));
        DemandWorkloadStatisticsVO vo = new DemandWorkloadStatisticsVO();
        vo.setTotal((long) list.size());
        vo.setPendingCount(list.stream().filter(item -> STATUS_PENDING.equals(item.getStatus())).count());
        vo.setFilledCount(list.stream().filter(item -> STATUS_FILLED.equals(item.getStatus())).count());
        vo.setConfirmedCount(list.stream().filter(item -> STATUS_CONFIRMED.equals(item.getStatus())).count());
        vo.setInitialWorkloadTotal(sum(list.stream().map(DemandWorkload::getInitialWorkload).collect(Collectors.toList())));
        vo.setFinalWorkloadTotal(sum(list.stream().map(DemandWorkload::getFinalWorkload).collect(Collectors.toList())));
        vo.setReductionWorkloadTotal(sum(list.stream().map(DemandWorkload::getReductionWorkload).collect(Collectors.toList())));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importExcel(MultipartFile file) {
        requireDevOrSystem();
        ImportResultVO result = new ImportResultVO();
        List<DemandWorkloadExcelDTO> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).head(DemandWorkloadExcelDTO.class).sheet().doReadSync();
        } catch (IOException e) {
            throw new BusinessException("Excel读取失败：" + e.getMessage());
        }
        int rowNumber = 2;
        for (DemandWorkloadExcelDTO row : rows) {
            try {
                DemandWorkload workload = toEntity(row, rowNumber);
                DemandWorkload existing = this.getOne(new LambdaQueryWrapper<DemandWorkload>()
                        .eq(DemandWorkload::getDemandNo, workload.getDemandNo()), false);
                if (existing == null) {
                    workload.setCreatedAt(LocalDateTime.now());
                    workload.setUpdatedAt(LocalDateTime.now());
                    applyReduction(workload);
                    this.save(workload);
                    result.setInsertCount(result.getInsertCount() + 1);
                    notificationService.createNotification(workload.getProductManagerId(), "新需求待填写",
                            "需求「" + workload.getDemandName() + "」需要填写最终核定工作量", "DEMAND_WORKLOAD", workload.getId());
                } else {
                    workload.setId(existing.getId());
                    workload.setCreatedAt(existing.getCreatedAt());
                    workload.setUpdatedAt(LocalDateTime.now());
                    applyReduction(workload);
                    this.updateById(workload);
                    result.setUpdateCount(result.getUpdateCount() + 1);
                }
            } catch (Exception e) {
                result.setFailCount(result.getFailCount() + 1);
                result.getFailMessages().add("第" + rowNumber + "行：" + e.getMessage());
            }
            rowNumber++;
        }
        result.setSuccess(result.getFailCount() == 0);
        return result;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        writeExcel(response, "工作量核定导入模板.xlsx", Collections.singletonList(new DemandWorkloadExcelDTO()));
    }

    @Override
    public void exportExcel(DemandWorkloadQueryDTO queryDTO, HttpServletResponse response) {
        List<DemandWorkloadExcelDTO> rows = this.list(buildQueryWrapper(queryDTO)).stream().map(item -> {
            DemandWorkloadExcelDTO dto = new DemandWorkloadExcelDTO();
            dto.setDemandNo(item.getDemandNo());
            dto.setDemandName(item.getDemandName());
            dto.setDemandDescription(item.getDemandDescription());
            SysUser manager = sysUserService.getById(item.getProductManagerId());
            dto.setProductManagerUsername(manager == null ? null : manager.getUsername());
            dto.setProductManagerName(item.getProductManagerName());
            dto.setSystemName(item.getSystemName());
            dto.setInitialWorkload(item.getInitialWorkload());
            dto.setInitialAmount(item.getInitialAmount());
            dto.setFinalWorkload(item.getFinalWorkload());
            dto.setStatus(item.getStatus());
            dto.setRemark(item.getRemark());
            return dto;
        }).collect(Collectors.toList());
        writeExcel(response, "工作量核定报表.xlsx", rows);
    }

    private LambdaQueryWrapper<DemandWorkload> buildQueryWrapper(DemandWorkloadQueryDTO queryDTO) {
        LambdaQueryWrapper<DemandWorkload> wrapper = new LambdaQueryWrapper<>();
        CurrentUser user = AuthContext.get();
        if (user != null && RoleConstants.PRODUCT_MANAGER.equals(user.getRoleCode())) {
            wrapper.eq(DemandWorkload::getProductManagerId, user.getId());
        } else if (queryDTO.getProductManagerId() != null) {
            wrapper.eq(DemandWorkload::getProductManagerId, queryDTO.getProductManagerId());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(DemandWorkload::getDemandNo, queryDTO.getKeyword())
                    .or().like(DemandWorkload::getDemandName, queryDTO.getKeyword())
                    .or().like(DemandWorkload::getDemandDescription, queryDTO.getKeyword()));
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(DemandWorkload::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getSystemName())) {
            wrapper.like(DemandWorkload::getSystemName, queryDTO.getSystemName());
        }
        return wrapper;
    }

    private DemandWorkload getVisibleDemand(Long id) {
        DemandWorkload workload = this.getById(id);
        if (workload == null) {
            throw new BusinessException("需求不存在");
        }
        CurrentUser user = AuthContext.get();
        if (user != null && RoleConstants.PRODUCT_MANAGER.equals(user.getRoleCode())
                && !workload.getProductManagerId().equals(user.getId())) {
            throw new BusinessException(403, "只能查看本人负责的需求");
        }
        return workload;
    }

    private void fillFromDTO(DemandWorkload workload, DemandWorkloadDTO dto) {
        SysUser manager = sysUserService.getById(dto.getProductManagerId());
        if (manager == null || !RoleConstants.PRODUCT_MANAGER.equals(manager.getRoleCode())) {
            throw new BusinessException("产品经理不存在");
        }
        workload.setDemandNo(dto.getDemandNo());
        workload.setDemandName(dto.getDemandName());
        workload.setDemandDescription(dto.getDemandDescription());
        workload.setProductManagerId(manager.getId());
        workload.setProductManagerName(manager.getName());
        workload.setSystemName(dto.getSystemName());
        workload.setInitialWorkload(dto.getInitialWorkload());
        workload.setInitialAmount(dto.getInitialAmount());
        workload.setFinalWorkload(dto.getFinalWorkload());
        workload.setRemark(dto.getRemark());
    }

    private DemandWorkload toEntity(DemandWorkloadExcelDTO row, int rowNumber) {
        List<String> errors = new ArrayList<>();
        if (!StringUtils.hasText(row.getDemandNo())) errors.add("需求编号不能为空");
        if (!StringUtils.hasText(row.getDemandName())) errors.add("需求名称不能为空");
        if (!StringUtils.hasText(row.getProductManagerUsername())) errors.add("产品经理用户名不能为空");
        if (!StringUtils.hasText(row.getSystemName())) errors.add("归属系统不能为空");
        if (row.getInitialWorkload() == null) errors.add("初核工作量不能为空");
        if (!errors.isEmpty()) {
            throw new BusinessException(String.join("；", errors));
        }
        SysUser manager = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, row.getProductManagerUsername())
                .eq(SysUser::getRoleCode, RoleConstants.PRODUCT_MANAGER)
                .eq(SysUser::getStatus, 1), false);
        if (manager == null) {
            throw new BusinessException("产品经理用户不存在或已禁用：" + row.getProductManagerUsername());
        }
        DemandWorkload workload = new DemandWorkload();
        workload.setDemandNo(row.getDemandNo());
        workload.setDemandName(row.getDemandName());
        workload.setDemandDescription(row.getDemandDescription());
        workload.setProductManagerId(manager.getId());
        workload.setProductManagerName(manager.getName());
        workload.setSystemName(row.getSystemName());
        workload.setInitialWorkload(row.getInitialWorkload());
        workload.setInitialAmount(row.getInitialAmount());
        workload.setFinalWorkload(row.getFinalWorkload());
        workload.setStatus(StringUtils.hasText(row.getStatus()) ? row.getStatus() : STATUS_PENDING);
        validateStatus(workload.getStatus());
        workload.setRemark(row.getRemark());
        return workload;
    }

    private DemandWorkloadVO toVO(DemandWorkload workload, boolean includeReminders) {
        DemandWorkloadVO vo = new DemandWorkloadVO();
        BeanUtils.copyProperties(workload, vo);
        if (includeReminders) {
            vo.setReminders(demandReminderService.listByDemandId(workload.getId()));
        }
        return vo;
    }

    private void applyReduction(DemandWorkload workload) {
        if (workload.getInitialWorkload() != null && workload.getFinalWorkload() != null) {
            workload.setReductionWorkload(workload.getInitialWorkload().subtract(workload.getFinalWorkload()));
        } else {
            workload.setReductionWorkload(BigDecimal.ZERO);
        }
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().filter(value -> value != null).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateStatus(String status) {
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException("需求状态只能是待填写、已填写、已核定");
        }
    }

    private void requireDevOrSystem() {
        CurrentUser user = AuthContext.get();
        if (user == null || (!RoleConstants.DEV_ADMIN.equals(user.getRoleCode()) && !RoleConstants.SYSTEM_ADMIN.equals(user.getRoleCode()))) {
            throw new BusinessException(403, "无权操作");
        }
    }

    private void notifyManagers(DemandWorkload workload, String title, String content) {
        sysUserService.list(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getRoleCode, RoleConstants.DEV_ADMIN, RoleConstants.SYSTEM_ADMIN)
                        .eq(SysUser::getStatus, 1))
                .forEach(user -> notificationService.createNotification(user.getId(), title, content, "DEMAND_WORKLOAD", workload.getId()));
    }

    private void writeExcel(HttpServletResponse response, String fileName, List<DemandWorkloadExcelDTO> rows) {
        try {
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encodedName);
            EasyExcel.write(response.getOutputStream(), DemandWorkloadExcelDTO.class).sheet("工作量核定").doWrite(rows);
        } catch (IOException e) {
            throw new BusinessException("Excel写出失败：" + e.getMessage());
        }
    }
}
