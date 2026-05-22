package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.common.Result;
import com.jscm.yxtyg.security.PermissionConstants;
import com.jscm.yxtyg.security.RequirePermissions;
import com.jscm.yxtyg.service.NotificationService;
import com.jscm.yxtyg.vo.NotificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequirePermissions(PermissionConstants.NOTIFICATION_VIEW)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result<List<NotificationVO>> list() {
        return Result.success(notificationService.listCurrentUserNotifications());
    }

    @PostMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }
}
