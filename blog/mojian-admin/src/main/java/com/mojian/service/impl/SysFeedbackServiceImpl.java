package com.mojian.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.mojian.common.Constants;
import com.mojian.dto.message.SysFeedbackQueryDto;
import com.mojian.entity.SysNotifications;
import com.mojian.utils.NotificationsUtil;
import com.mojian.vo.feedback.SysFeedbackVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.mojian.mapper.SysFeedbackMapper;
import com.mojian.entity.SysFeedback;
import com.mojian.service.SysFeedbackService;
import com.mojian.utils.PageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysFeedbackServiceImpl extends ServiceImpl<SysFeedbackMapper, SysFeedback> implements SysFeedbackService {

    private final NotificationsUtil notificationsUtil;

    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(10);

    /**
     * 查询反馈表分页列表
     */
    @Override
    public IPage<SysFeedbackVo> selectPage(SysFeedbackQueryDto feedbackQueryDto) {
        //如果是门户端的则只能看自己的反馈
        if (!Constants.ADMIN.equals(feedbackQueryDto.getSource())) {
            feedbackQueryDto.setUserId(StpUtil.getLoginIdAsLong());
        }
        return baseMapper.page(PageUtil.getPage(), feedbackQueryDto);
    }

    /**
     * 新增反馈表
     */
    @Override
    public boolean insert(SysFeedback sysFeedback) {
        sysFeedback.setUserId(StpUtil.getLoginIdAsLong());
        boolean result = save(sysFeedback);
        if (result) {
            String content = sysFeedback.getContent();
            Long fromUserId = StpUtil.getLoginIdAsLong();
            taskExecutor.submit(() -> {
                try {
                    SysNotifications notifications = SysNotifications.builder()
                            .title("新反馈通知")
                            .message(content)
                            .businessId(sysFeedback.getId())
                            .businessType("feedback")
                            .noticePush(NotificationsUtil.buildNoticePush(1L))
                            .fromUserId(fromUserId)
                            .build();
                    notificationsUtil.publish(notifications);
                } catch (Exception e) {
                    log.error("发送通知失败", e);
                }
            });
        }
        return result;
    }

    /**
     * 修改反馈表
     */
    @Override
    public boolean update(SysFeedback sysFeedback) {
        return updateById(sysFeedback);
    }
}
