package com.jscm.yxtyg.service;

import com.jscm.yxtyg.vo.RecommendResultVO;

/**
 * 智能推荐服务
 */
public interface RecommendService {

    RecommendResultVO recommend(String content);
}
