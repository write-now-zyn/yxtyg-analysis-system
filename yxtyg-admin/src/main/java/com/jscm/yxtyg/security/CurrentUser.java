package com.jscm.yxtyg.security;

import lombok.Data;

import java.io.Serializable;

/**
 * Authenticated user snapshot for the current request.
 */
@Data
public class CurrentUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String name;
    private String roleCode;
    private String roleName;
}
