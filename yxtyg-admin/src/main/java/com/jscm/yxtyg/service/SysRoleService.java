package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.dto.RolePermissionDTO;
import com.jscm.yxtyg.entity.SysRole;
import com.jscm.yxtyg.vo.PermissionVO;
import com.jscm.yxtyg.vo.RoleVO;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    List<RoleVO> listRoles();

    List<PermissionVO> listPermissions();

    List<String> listPermissionCodes(String roleCode);

    void updatePermissions(String roleCode, RolePermissionDTO dto);

    String getRoleName(String roleCode);
}
