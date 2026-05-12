package com.jscm.yxtyg.vector;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 向量文件根节点
 */
@Data
public class VectorStoreDocument {

    private String version = "1.0";

    private String lastSyncTime;

    private List<WorkOrderVectorRecord> vectors = new ArrayList<>();
}
