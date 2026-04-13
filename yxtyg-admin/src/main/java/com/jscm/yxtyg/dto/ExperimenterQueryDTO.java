package com.jscm.yxtyg.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 体验官查询DTO
 */
@Data
public class ExperimenterQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 地市 */
    private String city;

    /** 姓名 */
    private String name;

    /** 电话 */
    private String phone;

    /** 是否接口人 */
    private Integer isContact;

    /** 当前页 */
    private Long current = 1L;

    /** 每页大小 */
    private Long size = 10L;

}
