package com.jscm.yxtyg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 转培上报DTO
 */
@Data
public class TrainingReportDTO {

    private Long id;

    @NotBlank(message = "地市不能为空")
    private String city;

    @NotBlank(message = "上报月份不能为空")
    private String reportMonth;

    @NotNull(message = "培训记录不能为空")
    @Size(min = 8, message = "培训记录至少需要8条")
    private List<TrainingRecordDTO> records;
}
