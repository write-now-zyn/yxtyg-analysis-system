package com.jscm.yxtyg.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DemandReminderVO {

    private Long id;
    private Long demandId;
    private String reminderContent;
    private Long reminderBy;
    private String reminderByName;
    private Long reminderTo;
    private String reminderToName;
    private LocalDateTime createdAt;
}
