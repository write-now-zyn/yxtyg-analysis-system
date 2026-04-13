package com.jscm.yxtyg.util;

import com.jscm.yxtyg.dto.WorkOrderExcelDTO;
import com.jscm.yxtyg.entity.SolutionHistory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * 工单Excel解析工具类测试
 */
public class WorkOrderExcelParserTest {

    @Test
    public void testParseExcel() throws Exception {
        // 读取测试Excel文件
        File file = new File("src/main/resources/tmp/工单模板.xlsx");
        if (!file.exists()) {
            System.out.println("测试文件不存在：" + file.getAbsolutePath());
            return;
        }

        FileInputStream input = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", file.getName(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", input);

        WorkOrderExcelParser parser = new WorkOrderExcelParser();
        WorkOrderExcelParser.ParseResult result = parser.parse(multipartFile);

        System.out.println("=== Excel解析测试结果 ===");
        System.out.println("解析成功数量：" + result.getSuccessList().size());
        System.out.println("解析失败数量：" + result.getFailRows().size());
        
        for (int i = 0; i < Math.min(3, result.getSuccessList().size()); i++) {
            WorkOrderExcelDTO dto = result.getSuccessList().get(i);
            System.out.println("\n--- 第" + (i + 1) + "条数据 ---");
            System.out.println("问题编号：" + dto.getOrderNo());
            System.out.println("处理单位：" + dto.getHandleUnit());
            System.out.println("处理人：" + dto.getHandlerName());
            System.out.println("地市：" + dto.getCity());
            System.out.println("创建时间：" + dto.getCreateTimeStr());
            System.out.println("发起人：" + dto.getInitiatorName());
            System.out.println("状态：" + dto.getStatus());
            System.out.println("工单内容：" + (dto.getContent() != null && dto.getContent().length() > 50 
                    ? dto.getContent().substring(0, 50) + "..." : dto.getContent()));
            System.out.println("解决方案历史：" + dto.getSolutionHistoryStr());
            
            // 测试解析解决方案历史
            if (dto.getSolutionHistoryStr() != null && !dto.getSolutionHistoryStr().isEmpty()) {
                List<SolutionHistory> historyList = parser.parseSolutionHistory(dto.getSolutionHistoryStr(), dto.getOrderNo());
                System.out.println("  解析出的解决方案历史(" + historyList.size() + "条)：");
                for (SolutionHistory h : historyList) {
                    System.out.println("    - " + h.getHandlerName() + "：" + h.getOpinionContent());
                }
            }
        }
        
        input.close();
    }

    @Test
    public void testParseSolutionHistory() {
        WorkOrderExcelParser parser = new WorkOrderExcelParser();
        
        // 测试用例1：标准格式
        String test1 = "小信处理意见: 问题已解决|郭康处理意见: 请海爷评估";
        System.out.println("=== 测试解决方案历史解析 ===");
        System.out.println("原始字符串：" + test1);
        List<SolutionHistory> result1 = parser.parseSolutionHistory(test1, "Q001");
        System.out.println("解析结果：");
        for (SolutionHistory h : result1) {
            System.out.println("  处理人：" + h.getHandlerName() + "，意见：" + h.getOpinionContent());
        }
        
        // 测试用例2：包含中文冒号
        String test2 = "张三处理意见：已反馈|李四处理意见：等待处理";
        System.out.println("\n原始字符串：" + test2);
        List<SolutionHistory> result2 = parser.parseSolutionHistory(test2, "Q002");
        System.out.println("解析结果：");
        for (SolutionHistory h : result2) {
            System.out.println("  处理人：" + h.getHandlerName() + "，意见：" + h.getOpinionContent());
        }
        
        // 测试用例3：空字符串
        String test3 = "";
        List<SolutionHistory> result3 = parser.parseSolutionHistory(test3, "Q003");
        System.out.println("\n空字符串测试，结果数量：" + result3.size());
    }
}
