package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.dto.RolePermissionDTO;
import com.jscm.yxtyg.entity.SysPermission;
import com.jscm.yxtyg.entity.SysRole;
import com.jscm.yxtyg.entity.SysRolePermission;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.SysPermissionMapper;
import com.jscm.yxtyg.mapper.SysRoleMapper;
import com.jscm.yxtyg.mapper.SysRolePermissionMapper;
import com.jscm.yxtyg.service.SysRoleService;
import com.jscm.yxtyg.vo.PermissionVO;
import com.jscm.yxtyg.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Override
    public List<RoleVO> listRoles() {
        return this.list().stream().map(role -> {
            RoleVO vo = new RoleVO();
            vo.setCode(role.getCode());
            vo.setName(role.getName());
            vo.setDescription(role.getDescription());
            vo.setPermissions(listPermissionCodes(role.getCode()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> listPermissions() {
        return permissionMapper.selectList(null).stream().map(permission -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(permission, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> listPermissionCodes(String roleCode) {
        if (roleCode == null) {
            return Collections.emptyList();
        }
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleCode, roleCode))
                .stream()
                .map(SysRolePermission::getPermissionCode)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermissions(String roleCode, RolePermissionDTO dto) {
        SysRole role = this.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode), false);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleCode, roleCode));
        if (dto.getPermissionCodes() == null) {
            return;
        }
        for (String permissionCode : dto.getPermissionCodes()) {
            SysRolePermission item = new SysRolePermission();
            item.setRoleCode(roleCode);
            item.setPermissionCode(permissionCode);
            rolePermissionMapper.insert(item);
        }
    }

    @Override
    public String getRoleName(String roleCode) {
        Map<String, String> roleNameMap = this.list().stream()
                .collect(Collectors.toMap(SysRole::getCode, SysRole::getName, (a, b) -> a));
        return roleNameMap.getOrDefault(roleCode, roleCode);
    }
}
