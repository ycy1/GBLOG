package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.entity.SysMoment;
import com.mojian.entity.SysNotifications;
import com.mojian.mapper.SysMomentMapper;
import com.mojian.service.MomentService;
import com.mojian.utils.NotificationsUtil;
import com.mojian.utils.PageUtil;
import com.mojian.vo.moment.MomentPageVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomentServiceImpl implements MomentService {

    private final SysMomentMapper baseMapper;

    private final NotificationsUtil notificationsUtil;

    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(10);

    @Override
    public IPage<MomentPageVo> getMomentList() {
        Object loginIdObj = StpUtil.getLoginIdDefaultNull();
        Long userId = loginIdObj != null ? Long.valueOf(loginIdObj.toString()) : null;
        return baseMapper.selectPage(PageUtil.getPage(), userId);
    }

    @Override
    public MomentPageVo getMomentDetail(Long id) {
        Object loginIdObj = StpUtil.getLoginIdDefaultNull();
        Long userId = loginIdObj != null ? Long.valueOf(loginIdObj.toString()) : null;
        return baseMapper.selectDetail(id, userId);
    }

    @Override
    public Boolean likeMoment(Long momentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean isLike = baseMapper.getUserIsLikeMoment(momentId, userId);
        if (Boolean.TRUE.equals(isLike)) {
            baseMapper.unLikeMoment(momentId, userId);
            baseMapper.updateLikeCount(momentId, -1);
        } else {
            baseMapper.likeMoment(momentId, userId);
            baseMapper.updateLikeCount(momentId, 1);
            //发送通知事件
            SysMoment moment = baseMapper.selectById(momentId);
            if (moment == null) return !Boolean.TRUE.equals(isLike);
            long loginIdAsLong = StpUtil.getLoginIdAsLong();
            taskExecutor.submit(() -> {
                try {
                    SysNotifications notifications = SysNotifications.builder()
                            .title("动态点赞通知")
                            .businessId(momentId)
                            .businessType("moment")
                            .noticePush(NotificationsUtil.buildNoticePush(moment.getUserId()))
                            .fromUserId(loginIdAsLong)
                            .build();
                    notificationsUtil.publish(notifications);
                } catch (Exception e) {
                    log.error("发送通知失败", e);
                }
            });
        }
        return !Boolean.TRUE.equals(isLike);
    }

    @Override
    public IPage<MomentPageVo> getMyMomentList() {
        return baseMapper.selectMyMomentPage(PageUtil.getPage(), StpUtil.getLoginIdAsLong());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMyMoment(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysMoment moment = baseMapper.selectById(id);
        if (moment == null || !userId.equals(moment.getUserId())) {
            throw new RuntimeException("无权删除该动态");
        }
        baseMapper.deleteById(id);
    }
}
