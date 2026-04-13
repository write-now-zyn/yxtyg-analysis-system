package com.jscm.yxtyg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 转培上报记录实体类
 */
@Data
@TableName("t_training_report")
public class TrainingReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 地市 */
    private String city;

    /** 培训月份 YYYY-MM */
    private String reportMonth;

    /** 上报时间 */
    private LocalDate reportTime;

    /** 上报人 */
    private String reporter;

    /** 培训记录条数 */
    private Integer recordCount;

    /** 培训人数（覆盖人数合计） */
    private Integer trainingPersonCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
