package com.jscm.yxtyg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 看板统计Mapper
 */
@Mapper
public interface DashboardMapper {

    /**
     * 统计某月各地市需求提单数量
     * @param month 月份（如202603）
     * @return 地市统计列表
     */
    List<Map<String, Object>> countDemandByCity(@Param("month") String month);

    /**
     * 获取所有地市列表
     * @return 地市列表
     */
    List<String> getAllCities();

    /**
     * 统计某月各地市培训上报次数
     * @param month 月份（如2026-03）
     * @return 地市统计列表
     */
    List<Map<String, Object>> countTrainingByCity(@Param("month") String month);
}
