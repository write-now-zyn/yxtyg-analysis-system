package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.UserDTO;
import com.jscm.yxtyg.dto.UserQueryDTO;
import com.jscm.yxtyg.entity.SysUser;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.SysUserMapper;
import com.jscm.yxtyg.security.RoleConstants;
import com.jscm.yxtyg.service.SysRoleService;
import com.jscm.yxtyg.service.SysUserService;
import com.jscm.yxtyg.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SysRoleService sysRoleService;

    @Override
    public PageResult<UserVO> queryPage(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<SysUser> wrapper = buildWrapper(queryDTO);
        wrapper.orderByAsc(SysUser::getRoleCode).orderByDesc(SysUser::getUpdatedAt);
        Page<SysUser> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        List<UserVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public List<UserVO> listProductManagers() {
        return this.list(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRoleCode, RoleConstants.PRODUCT_MANAGER)
                        .eq(SysUser::getStatus, 1)
                        .orderByAsc(SysUser::getName))
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public UserVO createUser(UserDTO dto) {
        SysUser existing = this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()), false);
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("新建用户必须设置密码");
        }
        SysUser user = new SysUser();
        fillUser(user, dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        this.save(user);
        return toVO(user);
    }

    @Override
    public UserVO updateUser(Long id, UserDTO dto) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser existing = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername())
                .ne(SysUser::getId, id), false);
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        fillUser(user, dto);
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
        return toVO(user);
    }

    @Override
    public void disableUser(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(0);
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
    }

    @Override
    public UserVO toVO(SysUser user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRoleName(sysRoleService.getRoleName(user.getRoleCode()));
        return vo;
    }

    private LambdaQueryWrapper<SysUser> buildWrapper(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, queryDTO.getKeyword())
                    .or().like(SysUser::getName, queryDTO.getKeyword())
                    .or().like(SysUser::getPhone, queryDTO.getKeyword()));
        }
        if (StringUtils.hasText(queryDTO.getRoleCode())) {
            wrapper.eq(SysUser::getRoleCode, queryDTO.getRoleCode());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }
        return wrapper;
    }

    private void fillUser(SysUser user, UserDTO dto) {
        if (sysRoleService.getOne(new LambdaQueryWrapper<com.jscm.yxtyg.entity.SysRole>()
                .eq(com.jscm.yxtyg.entity.SysRole::getCode, dto.getRoleCode()), false) == null) {
            throw new BusinessException("角色不存在");
        }
        user.setUsername(dto.getUsername());
        user.setName(dto.getName());
        user.setRoleCode(dto.getRoleCode());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus());
    }
}
