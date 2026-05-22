package com.jscm.yxtyg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recipientUserId;
    private String title;
    private String content;
    private String bizType;
    private Long bizId;
    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
