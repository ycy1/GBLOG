package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.entity.SysNotifications;
import com.mojian.service.CommentService;
import com.mojian.utils.NotificationsUtil;
import com.mojian.utils.SensitiveUtil;
import com.mojian.vo.comment.CommentListVo;
import com.mojian.entity.SysComment;
import com.mojian.mapper.SysCommentMapper;
import com.mojian.utils.IpUtil;
import com.mojian.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final SysCommentMapper sysCommentMapper;

    private final NotificationsUtil notificationsUtil;

    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(10);

    @Override
    public IPage<CommentListVo> getComments(Integer businessId, String sortType, Integer commentType) {
        Object loginIdObj = StpUtil.getLoginIdDefaultNull();
        Long userId = loginIdObj != null ? Long.valueOf(loginIdObj.toString()) : null;
        IPage<CommentListVo> page = sysCommentMapper.getComments(PageUtil.getPage(), businessId, sortType, userId, commentType);
        //递归获取所有子评论
        page.getRecords().forEach(commentListVo -> {
            fillChildren(commentListVo, userId);
        });
        return page;
    }

    @Override
    public void add(SysComment sysComment) {
        String ip = IpUtil.getIp();
        sysComment.setIp(ip);
        sysComment.setIpSource(IpUtil.getIp2region(ip));
        sysComment.setUserId(StpUtil.getLoginIdAsLong());
        sysComment.setContent(SensitiveUtil.filter(sysComment.getContent()));

        if (sysComment.getCommentType() == null) {
            sysComment.setCommentType(1); // 默认文章评论
        }

        sysCommentMapper.insert(sysComment);

        //发送通知事件
        Long fromUserId = StpUtil.getLoginIdAsLong();
        String businessType = sysComment.getCommentType() != null && sysComment.getCommentType() == 2 ? "moment" : "article";
        String title = sysComment.getReplyUserId() != null ? "评论回复通知" : "新评论通知";
        Long replyUserId = sysComment.getReplyUserId();
        Long businessId = sysComment.getBusinessId();
        String content = sysComment.getContent();
        taskExecutor.submit(() -> {
            try {
                SysNotifications notifications = SysNotifications.builder()
                        .title(title)
                        .message(content)
                        .businessId(businessId)
                        .businessType(businessType)
                        // 回复评论 → 通知被回复人；顶级评论（replyUserId 为 null）→ 留空，由 publish 解析内容作者
                        .noticePush(replyUserId != null ? NotificationsUtil.buildNoticePush(replyUserId) : null)
                        .fromUserId(fromUserId)
                        .build();
                notificationsUtil.publish(notifications);
            } catch (Exception e) {
                log.error("发送通知失败", e);
            }
        });
    }

    @Override
    public Boolean likeComment(Integer commentId, Integer commentType) {
        int userId = StpUtil.getLoginIdAsInt();
        Boolean isLike = sysCommentMapper.getUserIsLikeComment(commentId, userId);
        if (Boolean.TRUE.equals(isLike)) {
            sysCommentMapper.unLikeComment(commentId, userId);
            sysCommentMapper.updateLikeCount(commentId, -1);
        } else {
            sysCommentMapper.likeComment(commentId, userId, commentType != null ? commentType : 1);
            sysCommentMapper.updateLikeCount(commentId, 1);
            // 查找评论作者并发送通知
            SysComment comment = sysCommentMapper.selectById(commentId);
            if (comment != null && !comment.getUserId().equals((long) userId)) {
                String businessType = comment.getCommentType() != null && comment.getCommentType() == 2 ? "moment" : "article";
                Long commentUserId = comment.getUserId();
                Long commentBusinessId = comment.getBusinessId();
                String commentContent = comment.getContent();
                long fromUserId = userId;
                taskExecutor.submit(() -> {
                    try {
                        SysNotifications notifications = SysNotifications.builder()
                                .title("评论点赞通知")
                                .message(commentContent)
                                .businessId(commentBusinessId)
                                .businessType(businessType)
                                .noticePush(NotificationsUtil.buildNoticePush(commentUserId))
                                .fromUserId(fromUserId)
                                .build();
                        notificationsUtil.publish(notifications);
                    } catch (Exception e) {
                        log.error("发送通知失败", e);
                    }
                });
            }
        }
        return !Boolean.TRUE.equals(isLike);
    }

    /**
     * 递归填充子评论
     */
    private void fillChildren(CommentListVo comment, Long userId) {
        List<CommentListVo> children = sysCommentMapper.getChildrenComment(comment.getId(), userId);
        if (children != null && !children.isEmpty()) {
            comment.setChildren(children);
            children.forEach(child -> fillChildren(child, userId));
        }
    }
}
