package com.jscm.yxtyg.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.jscm.yxtyg.dto.WorkOrderExcelDTO;
import com.jscm.yxtyg.entity.SolutionHistory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 工单Excel解析工具类
 * 独立的解析类，便于后续修改列映射
 */
@Slf4j
@Component
public class WorkOrderExcelParser {

    /**
     * 解析结果封装类
     */
    @Data
    public static class ParseResult {
        /** 解析成功的数据列表 */
        private List<WorkOrderExcelDTO> successList = new ArrayList<>();
        /** 解析失败的行号列表 */
        private List<Integer> failRows = new ArrayList<>();
        /** 失败原因列表 */
        private List<String> failMessages = new ArrayList<>();
    }

    /**
     * 解析Excel文件
     *
     * @param file Excel文件
     * @return 解析结果
     */
    public ParseResult parse(MultipartFile file) {
        log.info("开始解析工单Excel文件：{}", file.getOriginalFilename());
        
        ParseResult result = new ParseResult();
        
        try {
            EasyExcel.read(file.getInputStream(), WorkOrderExcelDTO.class, new AnalysisEventListener<WorkOrderExcelDTO>() {
                private int rowIndex = 0;

                @Override
                public void invoke(WorkOrderExcelDTO data, AnalysisContext context) {
                    rowIndex++;
                    // 跳过空行（问题编号为空）
                    if (data.getOrderNo() == null || data.getOrderNo().trim().isEmpty()) {
                        log.debug("第{}行问题编号为空，跳过", rowIndex);
                        return;
                    }
                    result.getSuccessList().add(data);
                    log.debug("第{}行解析成功，问题编号：{}", rowIndex, data.getOrderNo());
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel解析完成，共解析{}行数据", result.getSuccessList().size());
                }
            }).sheet().doRead();
        } catch (IOException e) {
            log.error("解析Excel文件失败", e);
            result.getFailMessages().add("文件解析失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 解析解决方案历史字符串
     * 格式：处理人处理意见: 意见内容|处理人处理意见: 意见内容
     * 
     * @param solutionHistoryStr 解决方案历史原始字符串
     * @param orderNo 问题编号（用于关联）
     * @return 解决方案历史列表
     */
    public List<SolutionHistory> parseSolutionHistory(String solutionHistoryStr, String orderNo) {
        List<SolutionHistory> list = new ArrayList<>();
        
        if (solutionHistoryStr == null || solutionHistoryStr.trim().isEmpty()) {
            return list;
        }
        
        // 按竖线分割多条记录
        String[] items = solutionHistoryStr.split("\\|");
        int sortOrder = 0;
        
        for (String item : items) {
            if (item == null || item.trim().isEmpty()) {
                continue;
            }
            
            // 解析格式：处理人处理意见: 意见内容
            // 处理人姓名后面带"处理意见"后缀，需要去掉
            int colonIndex = item.indexOf("处理意见:");
            if (colonIndex == -1) {
                colonIndex = item.indexOf("处理意见：");
            }
            
            SolutionHistory history = new SolutionHistory();
            history.setOrderNo(orderNo);
            history.setSortOrder(sortOrder++);
            
            if (colonIndex != -1) {
                // 提取处理人姓名（去掉"处理意见"后缀）
                String handlerName = item.substring(0, colonIndex).trim();
                // 如果名字以"处理意见"结尾，去掉这个后缀
                if (handlerName.endsWith("处理意见")) {
                    handlerName = handlerName.substring(0, handlerName.length() - 4).trim();
                }
                history.setHandlerName(handlerName);
                
                // 提取意见内容
                String opinionContent = item.substring(colonIndex + 5).trim();
                history.setOpinionContent(opinionContent);
            } else {
                // 格式不符合预期，尝试用冒号分割
                int idx = item.indexOf(":");
                if (idx == -1) {
                    idx = item.indexOf("：");
                }
                if (idx != -1) {
                    history.setHandlerName(item.substring(0, idx).trim());
                    history.setOpinionContent(item.substring(idx + 1).trim());
                } else {
                    // 无法解析，将整条作为意见内容
                    history.setOpinionContent(item.trim());
                }
            }
            
            list.add(history);
        }
        
        return list;
    }
}
