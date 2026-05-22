package com.jscm.yxtyg.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoleVO {

    private String code;
    private String name;
    private String description;
    private List<String> permissions;
}
