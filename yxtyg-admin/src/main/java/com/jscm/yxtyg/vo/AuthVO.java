package com.jscm.yxtyg.vo;

import com.jscm.yxtyg.security.CurrentUser;
import lombok.Data;

import java.util.List;

@Data
public class AuthVO {

    private String token;
    private CurrentUser user;
    private List<String> permissions;
}
