package com.jscm.yxtyg.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 体验官VO
 */
@Data
public class ExperimenterVO {

    private Long id;

    private String city;

    private String name;

    private String phone;

    private String email;

    private String role;

    private String remark;

    private Integer isContact;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
