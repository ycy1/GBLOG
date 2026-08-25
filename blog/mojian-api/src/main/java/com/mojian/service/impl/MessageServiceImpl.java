package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mojian.entity.SysNotifications;
import com.mojian.service.MessageService;
import com.mojian.entity.SysMessage;
import com.mojian.mapper.SysMessageMapper;
import com.mojian.utils.IpUtil;
import com.mojian.utils.NotificationsUtil;
import com.mojian.utils.SensitiveUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final SysMessageMapper messageMapper;

    private final NotificationsUtil notificationsUtil;

    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(10);

    @Override
    public List<SysMessage> getMessageList() {
        return messageMapper.selectList(null);
    }

    @Override
    public Boolean add(SysMessage sysMessage) {
        String ip = IpUtil.getIp();
        sysMessage.setIp(ip);
        sysMessage.setSource(IpUtil.getIp2region(ip));
        sysMessage.setContent(SensitiveUtil.filter(sysMessage.getContent()));
        messageMapper.insert(sysMessage);

        String content = sysMessage.getContent();
        Long fromUserId = StpUtil.getLoginIdDefaultNull() != null ? StpUtil.getLoginIdAsLong() : null;
        taskExecutor.submit(() -> {
            try {
                SysNotifications notifications = SysNotifications.builder()
                        .title("新留言通知")
                        .message(content)
                        .businessId(sysMessage.getId())
                        .businessType("note")
                        .noticePush(NotificationsUtil.buildNoticePush(1L))
                        .fromUserId(fromUserId)
                        .build();
                notificationsUtil.publish(notifications);
            } catch (Exception e) {
                log.error("发送通知失败", e);
            }
        });
        return true;
    }
}
