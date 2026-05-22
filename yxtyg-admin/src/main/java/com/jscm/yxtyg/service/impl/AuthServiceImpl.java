package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.dto.LoginDTO;
import com.jscm.yxtyg.entity.SysSession;
import com.jscm.yxtyg.entity.SysUser;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.SysSessionMapper;
import com.jscm.yxtyg.security.CurrentUser;
import com.jscm.yxtyg.service.AuthService;
import com.jscm.yxtyg.service.SysRoleService;
import com.jscm.yxtyg.service.SysUserService;
import com.jscm.yxtyg.vo.AuthVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl extends ServiceImpl<SysSessionMapper, SysSession> implements AuthService {

    private static final int SESSION_DAYS = 7;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthVO login(LoginDTO loginDTO) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()), false);
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "用户已被禁用");
        }

        SysSession session = new SysSession();
        session.setToken(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(user.getId());
        session.setExpiresAt(LocalDateTime.now().plusDays(SESSION_DAYS));
        session.setCreatedAt(LocalDateTime.now());
        this.save(session);

        AuthVO authVO = new AuthVO();
        authVO.setToken(session.getToken());
        authVO.setUser(toCurrentUser(user));
        authVO.setPermissions(sysRoleService.listPermissionCodes(user.getRoleCode()));
        return authVO;
    }

    @Override
    public void logout(String token) {
        if (token != null) {
            this.remove(new LambdaQueryWrapper<SysSession>().eq(SysSession::getToken, token));
        }
    }

    @Override
    public CurrentUser getUserByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        SysSession session = this.getOne(new LambdaQueryWrapper<SysSession>().eq(SysSession::getToken, token), false);
        if (session == null || session.getExpiresAt() == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        SysUser user = sysUserService.getById(session.getUserId());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return null;
        }
        return toCurrentUser(user);
    }

    private CurrentUser toCurrentUser(SysUser user) {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(user.getId());
        currentUser.setUsername(user.getUsername());
        currentUser.setName(user.getName());
        currentUser.setRoleCode(user.getRoleCode());
        currentUser.setRoleName(sysRoleService.getRoleName(user.getRoleCode()));
        return currentUser;
    }
}
