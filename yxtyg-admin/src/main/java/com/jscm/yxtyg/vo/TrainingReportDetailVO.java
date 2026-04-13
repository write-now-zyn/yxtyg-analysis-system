package com.jscm.yxtyg.vo;

import com.jscm.yxtyg.dto.TrainingRecordDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 转培上报详情VO
 */
@Data
public class TrainingReportDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 地市 */
    private String city;

    /** 培训月份 */
    private String reportMonth;

    /** 上报时间 */
    private LocalDate reportTime;

    /** 上报人 */
    private String reporter;

    /** 培训记录条数 */
    private Integer recordCount;

    /** 培训人数 */
    private Integer trainingPersonCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 培训记录列表 */
    private List<TrainingRecordDTO> records;

}
