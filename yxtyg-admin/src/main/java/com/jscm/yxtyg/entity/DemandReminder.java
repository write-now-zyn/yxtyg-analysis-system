package com.jscm.yxtyg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_demand_reminder")
public class DemandReminder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private String reminderContent;
    private Long reminderBy;
    private Long reminderTo;
    private LocalDateTime createdAt;
}
