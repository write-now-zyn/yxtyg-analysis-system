package com.jscm.yxtyg.service;

import com.jscm.yxtyg.vo.SimilarTicketVO;
import com.jscm.yxtyg.vo.VectorStatsVO;
import com.jscm.yxtyg.vo.VectorSyncResultVO;

import java.util.List;

/**
 * 向量存储服务
 */
public interface VectorStoreService {

    List<SimilarTicketVO> match(String content, Integer limit);

    List<SimilarTicketVO> searchByEmbedding(List<Double> embedding, Integer limit);

    VectorStatsVO stats();

    VectorSyncResultVO syncIncremental();

    void syncWorkOrderById(Long workOrderId);

    void removeByOrderNo(String orderNo);
}
