package com.jscm.yxtyg.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jscm.yxtyg.entity.ReviewMeeting;
import com.jscm.yxtyg.mapper.DashboardMapper;
import com.jscm.yxtyg.mapper.ReviewMeetingMapper;
import com.jscm.yxtyg.service.DashboardService;
import com.jscm.yxtyg.vo.DashboardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 看板服务实现类
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    /** 需求提单达标标准 */
    private static final int DEMAND_STANDARD = 6;
    /** 评审参与达标标准 */
    private static final int REVIEW_STANDARD = 2;
    /** 培训上报达标标准 */
    private static final int TRAINING_STANDARD = 8;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private ReviewMeetingMapper reviewMeetingMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public DashboardVO getOverview(String month) {
        log.info("获取看板数据，月份：{}", month);

        // 默认当前月
        if (!StringUtils.hasText(month)) {
            month = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        }

        DashboardVO vo = new DashboardVO();
        vo.setMonth(month);

        // 获取所有地市
        List<String> allCities = dashboardMapper.getAllCities();
        log.debug("获取到{}个地市", allCities.size());

        // 统计需求提单
        DashboardVO.ItemStatistics demandStats = buildDemandStatistics(month, allCities);
        
        // 统计评审参与
        DashboardVO.ItemStatistics reviewStats = buildReviewStatistics(month, allCities);
        
        // 统计培训上报
        DashboardVO.ItemStatistics trainingStats = buildTrainingStatistics(month, allCities);

        DashboardVO.Statistics statistics = new DashboardVO.Statistics();
        statistics.setDemand(demandStats);
        statistics.setReview(reviewStats);
        statistics.setTraining(trainingStats);
        vo.setStatistics(statistics);

        log.info("看板数据获取完成，月份：{}", month);
        return vo;
    }

    /**
     * 构建需求提单统计
     */
    private DashboardVO.ItemStatistics buildDemandStatistics(String month, List<String> allCities) {
        List<Map<String, Object>> demandData = dashboardMapper.countDemandByCity(month);
        Map<String, Integer> cityCountMap = convertToCityCountMap(demandData);
        
        return buildItemStatistics(cityCountMap, allCities, DEMAND_STANDARD);
    }

    /**
     * 构建评审参与统计
     */
    private DashboardVO.ItemStatistics buildReviewStatistics(String month, List<String> allCities) {
        // 将月份格式从yyyyMM转为yyyy-MM
        String reportMonth = month.substring(0, 4) + "-" + month.substring(4);
        
        // 查询该月所有评审会议
        List<ReviewMeeting> meetings = reviewMeetingMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReviewMeeting>()
                .likeRight(ReviewMeeting::getMeetingTime, reportMonth)
        );

        // 统计各地市参与次数
        Map<String, Integer> cityCountMap = new HashMap<>();
        for (ReviewMeeting meeting : meetings) {
            List<String> participants = parseParticipants(meeting.getParticipants());
            for (String city : participants) {
                cityCountMap.merge(city, 1, Integer::sum);
            }
        }

        return buildItemStatistics(cityCountMap, allCities, REVIEW_STANDARD);
    }

    /**
     * 构建培训上报统计
     */
    private DashboardVO.ItemStatistics buildTrainingStatistics(String month, List<String> allCities) {
        // 将月份格式从yyyyMM转为yyyy-MM
        String reportMonth = month.substring(0, 4) + "-" + month.substring(4);
        
        List<Map<String, Object>> trainingData = dashboardMapper.countTrainingByCity(reportMonth);
        Map<String, Integer> cityCountMap = convertToCityCountMap(trainingData);
        
        return buildItemStatistics(cityCountMap, allCities, TRAINING_STANDARD);
    }

    /**
     * 解析参与者JSON
     * 支持两种格式：
     * 1. 对象数组：[{"name":"xxx","city":"镇江"}]
     * 2. 字符串数组：["镇江", "南京"]
     */
    private List<String> parseParticipants(String participantsJson) {
        if (!StringUtils.hasText(participantsJson)) {
            return Collections.emptyList();
        }
        try {
            String trimmed = participantsJson.trim();
            if (trimmed.startsWith("[{\"")) {
                // 对象数组格式，提取city字段
                List<Map<String, Object>> participantList = objectMapper.readValue(
                    participantsJson, new TypeReference<List<Map<String, Object>>>() {});
                return participantList.stream()
                    .map(p -> (String) p.get("city"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            } else {
                // 字符串数组格式
                return objectMapper.readValue(participantsJson, new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.warn("解析参与者JSON失败：{}", participantsJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 将查询结果转换为地市数量Map
     */
    private Map<String, Integer> convertToCityCountMap(List<Map<String, Object>> dataList) {
        Map<String, Integer> map = new HashMap<>();
        for (Map<String, Object> item : dataList) {
            String city = (String) item.get("city");
            Number count = (Number) item.get("count");
            if (city != null && count != null) {
                map.put(city, count.intValue());
            }
        }
        return map;
    }

    /**
     * 构建单项统计数据
     */
    private DashboardVO.ItemStatistics buildItemStatistics(
            Map<String, Integer> cityCountMap, 
            List<String> allCities, 
            int standard) {
        
        DashboardVO.ItemStatistics stats = new DashboardVO.ItemStatistics();
        stats.setStandard(standard);

        // 构建所有地市的统计列表
        List<DashboardVO.CityCount> allCityStats = allCities.stream()
            .map(city -> {
                DashboardVO.CityCount cc = new DashboardVO.CityCount();
                cc.setCity(city);
                int count = cityCountMap.getOrDefault(city, 0);
                cc.setCount(count);
                cc.setReached(count >= standard);
                return cc;
            })
            .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
            .collect(Collectors.toList());

        // 统计达标数量
        int reachedCount = (int) allCityStats.stream().filter(DashboardVO.CityCount::getReached).count();
        stats.setReachedCount(reachedCount);
        stats.setTotalCount(allCities.size());

        // 未达标地市列表
        List<DashboardVO.CityCount> unreachedCities = allCityStats.stream()
            .filter(cc -> !cc.getReached())
            .collect(Collectors.toList());
        stats.setUnreachedCities(unreachedCities);

        // 排行榜（全部地市）
        stats.setRanking(allCityStats);

        return stats;
    }
}
