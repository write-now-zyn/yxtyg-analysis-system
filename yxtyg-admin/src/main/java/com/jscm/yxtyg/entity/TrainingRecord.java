package com.jscm.yxtyg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 培训记录明细实体类
 */
@Data
@TableName("t_training_record")
public class TrainingRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联上报记录ID */
    private Long reportId;

    /** 培训时间 */
    private LocalDateTime trainingTime;

    /** 组织人员 */
    private String organizer;

    /** 培训主体 */
    private String trainingSubject;

    /** 覆盖人数 */
    private Integer coverageCount;

    /** 培训内容 */
    private String trainingContent;

    /** 培训截图(二进制) */
    private byte[] screenshot;

    /** 图片文件名 */
    private String screenshotName;

    /** 图片类型 */
    private String screenshotType;

    /** 创建时间 */
    private LocalDateTime createTime;

}
