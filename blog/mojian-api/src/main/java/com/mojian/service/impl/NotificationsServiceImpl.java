package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.entity.SysNotifications;
import com.mojian.exception.ServiceException;
import com.mojian.mapper.SysNotificationsMapper;
import com.mojian.service.NotificationsService;
import com.mojian.utils.PageUtil;
import com.mojian.vo.notifications.NotificationsListVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author: quequnlong
 * @date: 2025/3/21
 * @description:
 */
@Service
@RequiredArgsConstructor
public class NotificationsServiceImpl implements NotificationsService {

    private final SysNotificationsMapper baseMapper;


    public IPage<NotificationsListVo> page(SysNotifications notifications) {
        // 可见性/已读均由 SQL 层根据 notice_push 与 sys_notifications_receiver 计算
        return baseMapper.selectNotificationsPage(PageUtil.getPage(), notifications, StpUtil.getLoginIdAsLong());
    }

    @Override
    public void doRead(Long id) {
        // 先校验主消息存在且未删除
        SysNotifications notifications = baseMapper.selectById(id);
        if (notifications == null || (notifications.getDelFlag() != null && notifications.getDelFlag() == 1)) {
            throw new ServiceException("消息通知不存在");
        }
        // 存在已读记录则修改 is_read=1，否则插入（幂等 upsert）
        baseMapper.doRead(StpUtil.getLoginIdAsLong(), id);
    }

    @Override
    public void allRead() {
        baseMapper.allRead(StpUtil.getLoginIdAsLong());
    }

    @Override
    public void delete(Long id) {
        // 先校验主消息存在且未删除
        SysNotifications notifications = baseMapper.selectById(id);
        if (notifications == null || (notifications.getDelFlag() != null && notifications.getDelFlag() == 1)) {
            throw new ServiceException("消息不存在或无权删除");
        }
        // 接收人删除：receiver 记录置 is_deleted=1（仅对当前用户隐藏，删除对自己可见的消息）
        baseMapper.deleteByUser(StpUtil.getLoginIdAsLong(), id);
    }

    @Override
    public Map<String, Integer> getUnReadNum() {
        return baseMapper.getUnReadNum(StpUtil.getLoginIdAsLong());
    }

    @Override
    public NotificationsListVo getById(Long id) {
        return baseMapper.selectNotificationsById(id, StpUtil.getLoginIdAsLong());
    }

    @Override
    public Boolean getMyIsUnread() {
        return baseMapper.countMyUnread(StpUtil.getLoginIdAsLong()) > 0;
    }

}
