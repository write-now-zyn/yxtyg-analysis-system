package com.jscm.yxtyg.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {

    private Long id;
    private String title;
    private String content;
    private String bizType;
    private Long bizId;
    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
