package com.jscm.yxtyg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jscm.yxtyg.entity.Notification;
import com.jscm.yxtyg.vo.NotificationVO;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    void createNotification(Long userId, String title, String content, String bizType, Long bizId);

    List<NotificationVO> listCurrentUserNotifications();

    void markRead(Long id);
}
