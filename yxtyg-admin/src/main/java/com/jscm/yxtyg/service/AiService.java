package com.jscm.yxtyg.service;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 */
public interface AiService {

    /**
     * 解析与会人员文本，提取姓名和地市
     * @param text 输入文本
     * @return 解析结果列表，每个元素包含name和city
     */
    List<Map<String, String>> parseParticipants(String text);

    /**
     * 解析培训记录文本，提取培训信息
     * @param text 输入文本（表格格式）
     * @return 解析结果列表，每个元素包含city, organizer, trainingContent, trainingTime, coverageCount
     */
    List<Map<String, String>> parseTrainingRecords(String text);
}
