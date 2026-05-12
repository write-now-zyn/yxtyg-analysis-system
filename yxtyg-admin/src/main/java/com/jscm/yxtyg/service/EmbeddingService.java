package com.jscm.yxtyg.service;

import java.util.List;

/**
 * Embedding 服务
 */
public interface EmbeddingService {

    List<Double> embed(String content);

    List<List<Double>> embedBatch(List<String> contents);
}
