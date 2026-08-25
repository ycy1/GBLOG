package com.mojian.utils;

import com.mojian.entity.SysArticle;
import com.mojian.entity.SysMoment;
import com.mojian.entity.SysNotifications;
import com.mojian.mapper.SysArticleMapper;
import com.mojian.mapper.SysMomentMapper;
import com.mojian.mapper.SysNotificationsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author: quequnlong
 * @date: 2025/3/24
 * @description:
 */
@Slf4j
//@Component
@Service("notificationsUtil")
@RequiredArgsConstructor
public class NotificationsUtil {

    private final EmailUtil emailUtil;

    private final SysArticleMapper sysArticleMapper;

    private final SysMomentMapper sysMomentMapper;

    private final SysNotificationsMapper baseMapper;


    public void publish(SysNotifications sysNotifications) {
        log.info("发布通知：businessType={}, businessId={}, title={}",
                sysNotifications.getBusinessType(), sysNotifications.getBusinessId(), sysNotifications.getTitle());
        try{
            switch (sysNotifications.getBusinessType()) {
                case "article":
                case "moment":
                    // 文章/动态相关通知：未显式指定推送对象时，解析内容作者作为接收人
                    if (sysNotifications.getNoticePush() == null) {
                        resolveBusinessPush(sysNotifications);
                    }
                    // 如果未设置消息内容（新点赞），自动生成
                    if (sysNotifications.getMessage() == null && sysNotifications.getBusinessId() != null) {
                        if ("article".equals(sysNotifications.getBusinessType())) {
                            SysArticle article = sysArticleMapper.selectById(sysNotifications.getBusinessId());
                            if (article != null) {
                                sysNotifications.setMessage("点赞了文章：" + article.getTitle());
                            }
                        } else if ("moment".equals(sysNotifications.getBusinessType())) {
                            SysMoment moment = sysMomentMapper.selectById(sysNotifications.getBusinessId());
                            if (moment != null) {
                                String preview = moment.getContent();
                                sysNotifications.setMessage("点赞了动态：" + (preview != null && preview.length() > 50 ? preview.substring(0, 50) + "..." : preview));
                            }
                        }
                    }
                    if (sysNotifications.getTitle() == null) {
                        sysNotifications.setTitle("点赞通知");
                    }
                    break;
                case "notice":
                    // 公告通知所有人：notice_push 保持 null（或全空数组）即全员可见
                    emailUtil.send("2039916844@qq.com", "公告通知【"+ sysNotifications.getTitle()+"】", sysNotifications.getMessage());
                    log.info("公告发布邮件发送：{}", sysNotifications.getTitle());
                    break;
                default:
                    // note、feedback 等类型，notice_push 已由调用方显式构造
                    break;
            }
            // 发送批次标识：每次发布生成 32 位 uuid（去掉连字符），重新发送时由管理端生成新值
            if (sysNotifications.getSendCode() == null || sysNotifications.getSendCode().isEmpty()) {
                sysNotifications.setSendCode(UUID.randomUUID().toString().replace("-", ""));
            }
            baseMapper.insert(sysNotifications);
        }catch (Exception e){
            log.error("通知发布失败：businessType={}, businessId={}, title={}",
                    sysNotifications.getBusinessType(), sysNotifications.getBusinessId(), sysNotifications.getTitle());
            log.error("通知发布失败:{}", e.getMessage());
        }
    }

    /**
     * 构造推送对象 notice_push JSON：{"user":[],"dept":[],"role":[]}
     * 三数组全空（或返回 null）视为全员可见（公告等广播场景）
     *
     * @param userId  定向推送的用户id（单接收人场景，如点赞/评论回复）
     * @param deptIds 定向推送的部门id集合
     * @param roleIds 定向推送的角色id集合
     * @return notice_push JSON 字符串
     */
    public static String buildNoticePush(Long userId, List<Long> deptIds, List<Long> roleIds) {
        StringBuilder sb = new StringBuilder("{\"user\":[");
        if (userId != null) {
            sb.append(userId);
        }
        sb.append("],\"dept\":[");
        appendIds(sb, deptIds);
        sb.append("],\"role\":[");
        appendIds(sb, roleIds);
        sb.append("]}");
        return sb.toString();
    }

    /**
     * 构造单用户定向的 notice_push
     */
    public static String buildNoticePush(Long userId) {
        return buildNoticePush(userId, null, null);
    }

    private static void appendIds(StringBuilder sb, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(ids.get(i));
        }
    }

    /**
     * 根据业务类型解析内容作者，构造 notice_push 作为推送对象（user 数组）
     */
    private void resolveBusinessPush(SysNotifications sysNotifications) {
        if (sysNotifications.getBusinessId() == null) return;
        // 如果已指定推送对象，则不覆盖
        if (sysNotifications.getNoticePush() != null) return;

        if ("article".equals(sysNotifications.getBusinessType())) {
            SysArticle article = sysArticleMapper.selectById(sysNotifications.getBusinessId());
            if (article != null && article.getUserId() != null) {
                sysNotifications.setNoticePush(buildNoticePush(article.getUserId()));
            }
        } else if ("moment".equals(sysNotifications.getBusinessType())) {
            SysMoment moment = sysMomentMapper.selectById(sysNotifications.getBusinessId());
            if (moment != null && moment.getUserId() != null) {
                sysNotifications.setNoticePush(buildNoticePush(moment.getUserId()));
            }
        }
    }
}
