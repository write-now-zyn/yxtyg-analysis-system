package com.jscm.yxtyg.vo;

import lombok.Data;

/**
 * 向量统计
 */
@Data
public class VectorStatsVO {

    private Long vectorizedCount;

    private Long vectorizableCount;

    private Long totalWorkOrderCount;

    private String lastSyncTime;
}
