package com.jscm.yxtyg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 体验官DTO
 */
@Data
public class ExperimenterDTO {

    private Long id;

    @NotBlank(message = "地市不能为空")
    private String city;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "电话不能为空")
    private String phone;

    private String email;

    private String role;

    private String remark;

    private Integer isContact;
}
