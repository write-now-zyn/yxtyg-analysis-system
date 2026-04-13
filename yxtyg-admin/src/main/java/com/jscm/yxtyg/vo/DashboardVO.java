package com.jscm.yxtyg.vo;

import lombok.Data;

import java.util.List;

/**
 * 整体看板VO
 */
@Data
public class DashboardVO {

    /** 当前查询月份 */
    private String month;

    /** 统计数据 */
    private Statistics statistics;

    /**
     * 统计数据
     */
    @Data
    public static class Statistics {
        /** 需求提单统计 */
        private ItemStatistics demand;
        /** 评审参与统计 */
        private ItemStatistics review;
        /** 培训上报统计 */
        private ItemStatistics training;
    }

    /**
     * 单项统计
     */
    @Data
    public static class ItemStatistics {
        /** 达标标准值 */
        private Integer standard;
        /** 已达标地市数量 */
        private Integer reachedCount;
        /** 总地市数量 */
        private Integer totalCount;
        /** 未达标地市列表 */
        private List<CityCount> unreachedCities;
        /** 排行榜（按数量降序） */
        private List<CityCount> ranking;
    }

    /**
     * 地市统计
     */
    @Data
    public static class CityCount {
        /** 地市名称 */
        private String city;
        /** 数量 */
        private Integer count;
        /** 是否达标 */
        private Boolean reached;
    }
}
