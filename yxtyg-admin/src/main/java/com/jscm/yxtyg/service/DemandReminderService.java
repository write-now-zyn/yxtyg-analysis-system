package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.entity.DemandReminder;
import com.jscm.yxtyg.vo.DemandReminderVO;

import java.util.List;

public interface DemandReminderService extends IService<DemandReminder> {

    List<DemandReminderVO> listByDemandId(Long demandId);
}
