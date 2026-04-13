package com.jscm.yxtyg.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 转培上报保存VO
 */
@Data
public class TrainingReportSaveVO {

    /**
     * 上报记录ID（更新时传入）
     */
    private Long id;

    /**
     * 地市
     */
    @NotBlank(message = "地市不能为空")
    private String city;

    /**
     * 上报月份
     */
    @NotBlank(message = "上报月份不能为空")
    private String reportMonth;

    /**
     * 培训记录列表
     */
    @Size(min = 8, message = "培训记录至少需要8条")
    private List<TrainingRecordSaveVO> records;
}
