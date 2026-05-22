package com.jscm.yxtyg.dto;

import lombok.Data;

@Data
public class DemandWorkloadQueryDTO {

    private String keyword;
    private String status;
    private Long productManagerId;
    private String systemName;
    private Long current = 1L;
    private Long size = 10L;
}
