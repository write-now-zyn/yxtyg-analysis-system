package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.common.PageResult;
import com.jscm.yxtyg.dto.UserDTO;
import com.jscm.yxtyg.dto.UserQueryDTO;
import com.jscm.yxtyg.entity.SysUser;
import com.jscm.yxtyg.vo.UserVO;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    PageResult<UserVO> queryPage(UserQueryDTO queryDTO);

    List<UserVO> listProductManagers();

    UserVO createUser(UserDTO dto);

    UserVO updateUser(Long id, UserDTO dto);

    void disableUser(Long id);

    UserVO toVO(SysUser user);
}
