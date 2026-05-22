package com.jscm.yxtyg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jscm.yxtyg.entity.Notification;
import com.jscm.yxtyg.exception.BusinessException;
import com.jscm.yxtyg.mapper.NotificationMapper;
import com.jscm.yxtyg.security.AuthContext;
import com.jscm.yxtyg.service.NotificationService;
import com.jscm.yxtyg.vo.NotificationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public void createNotification(Long userId, String title, String content, String bizType, Long bizId) {
        if (userId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setRecipientUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBizType(bizType);
        notification.setBizId(bizId);
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        this.save(notification);
    }

    @Override
    public List<NotificationVO> listCurrentUserNotifications() {
        Long userId = AuthContext.currentUserId();
        return this.list(new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getRecipientUserId, userId)
                        .orderByAsc(Notification::getIsRead)
                        .orderByDesc(Notification::getCreatedAt)
                        .last("LIMIT 50"))
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public void markRead(Long id) {
        Notification notification = this.getById(id);
        if (notification == null || !notification.getRecipientUserId().equals(AuthContext.currentUserId())) {
            throw new BusinessException("通知不存在");
        }
        notification.setIsRead(1);
        notification.setReadAt(LocalDateTime.now());
        this.updateById(notification);
    }

    private NotificationVO toVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);
        return vo;
    }
}
