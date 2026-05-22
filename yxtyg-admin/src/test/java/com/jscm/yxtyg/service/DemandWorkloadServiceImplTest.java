package com.jscm.yxtyg.service;

import com.jscm.yxtyg.dto.DemandWorkloadDTO;
import com.jscm.yxtyg.dto.FinalWorkloadDTO;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.security.AuthContext;
import com.jscm.yxtyg.security.CurrentUser;
import com.jscm.yxtyg.security.RoleConstants;
import com.jscm.yxtyg.vo.DemandWorkloadVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DemandWorkloadServiceImplTest {

    @Autowired
    private DemandWorkloadService demandWorkloadService;

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void devAdminCannotSubmitFinalWorkloadForProductManager() {
        Long demandId = null;
        try {
            AuthContext.set(user(1L, RoleConstants.SYSTEM_ADMIN));
            DemandWorkloadVO created = demandWorkloadService.create(demand("REQ-TEST-PM-FILL"));
            demandId = created.getId();

            AuthContext.set(user(2L, RoleConstants.DEV_ADMIN));
            FinalWorkloadDTO dto = new FinalWorkloadDTO();
            dto.setFinalWorkload(new BigDecimal("2.00"));

            BusinessException error = assertThrows(BusinessException.class,
                    () -> demandWorkloadService.submitFinal(created.getId(), dto));
            assertEquals(403, error.getCode());
        } finally {
            if (demandId != null) {
                AuthContext.set(user(1L, RoleConstants.SYSTEM_ADMIN));
                demandWorkloadService.deleteDemand(demandId);
            }
        }
    }

    private DemandWorkloadDTO demand(String demandNo) {
        DemandWorkloadDTO dto = new DemandWorkloadDTO();
        dto.setDemandNo(demandNo);
        dto.setDemandName("产品经理填报权限测试");
        dto.setDemandDescription("仅用于权限回归测试");
        dto.setProductManagerId(3L);
        dto.setSystemName("体验官专项数据分析系统");
        dto.setInitialWorkload(new BigDecimal("3.00"));
        dto.setInitialAmount(new BigDecimal("3000.00"));
        return dto;
    }

    private CurrentUser user(Long id, String roleCode) {
        CurrentUser user = new CurrentUser();
        user.setId(id);
        user.setRoleCode(roleCode);
        return user;
    }
}
