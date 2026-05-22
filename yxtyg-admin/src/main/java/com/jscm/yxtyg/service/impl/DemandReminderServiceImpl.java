package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.entity.DemandReminder;
import com.jscm.yxtyg.entity.SysUser;
import com.jscm.yxtyg.mapper.DemandReminderMapper;
import com.jscm.yxtyg.service.DemandReminderService;
import com.jscm.yxtyg.service.SysUserService;
import com.jscm.yxtyg.vo.DemandReminderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DemandReminderServiceImpl extends ServiceImpl<DemandReminderMapper, DemandReminder> implements DemandReminderService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public List<DemandReminderVO> listByDemandId(Long demandId) {
        List<DemandReminder> reminders = this.list(new LambdaQueryWrapper<DemandReminder>()
                .eq(DemandReminder::getDemandId, demandId)
                .orderByDesc(DemandReminder::getCreatedAt));
        Map<Long, String> userNameMap = sysUserService.list().stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getName, (a, b) -> a));
        return reminders.stream().map(item -> {
            DemandReminderVO vo = new DemandReminderVO();
            BeanUtils.copyProperties(item, vo);
            vo.setReminderByName(userNameMap.get(item.getReminderBy()));
            vo.setReminderToName(userNameMap.get(item.getReminderTo()));
            return vo;
        }).collect(Collectors.toList());
    }
}
