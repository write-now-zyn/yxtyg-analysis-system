package com.jscm.yxtyg.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 与会人DTO
 */
@Data
public class ParticipantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 姓名 */
    private String name;

    /** 地市 */
    private String city;

}
