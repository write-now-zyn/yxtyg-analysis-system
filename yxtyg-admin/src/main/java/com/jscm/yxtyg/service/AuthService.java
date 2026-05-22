package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.dto.LoginDTO;
import com.jscm.yxtyg.entity.SysSession;
import com.jscm.yxtyg.security.CurrentUser;
import com.jscm.yxtyg.vo.AuthVO;

public interface AuthService extends IService<SysSession> {

    AuthVO login(LoginDTO loginDTO);

    void logout(String token);

    CurrentUser getUserByToken(String token);
}
