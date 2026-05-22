package com.jscm.yxtyg.dto;

import lombok.Data;

@Data
public class UserQueryDTO {

    private String keyword;
    private String roleCode;
    private Integer status;
    private Long current = 1L;
    private Long size = 10L;
}
