package com.jscm.yxtyg.vo;

import lombok.Data;

/**
 * 向量同步结果
 */
@Data
public class VectorSyncResultVO {

    private Integer addedCount;

    private Integer updatedCount;

    private Integer removedCount;

    private Integer skippedCount;

    private Integer totalCount;

    private String lastSyncTime;
}
