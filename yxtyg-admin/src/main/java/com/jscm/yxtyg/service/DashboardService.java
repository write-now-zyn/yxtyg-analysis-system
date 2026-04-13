package com.jscm.yxtyg.service;

import com.jscm.yxtyg.vo.DashboardVO;

/**
 * 看板服务接口
 */
public interface DashboardService {

    /**
     * 获取看板概览数据
     * @param month 月份（如202603），为空则默认当前月
     * @return 看板数据
     */
    DashboardVO getOverview(String month);
}
