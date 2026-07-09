package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mojian.dto.message.SysCommentQueryDto;
import com.mojian.entity.SysComment;
import com.mojian.mapper.SysCommentMapper;
import com.mojian.service.SysCommentService;
import com.mojian.utils.PageUtil;
import com.mojian.vo.comment.SysCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: quequnlong
 * @date: 2025/1/2
 * @description:
 */
@Service
@RequiredArgsConstructor
public class SysCommentServiceImpl extends ServiceImpl<SysCommentMapper,SysComment> implements SysCommentService {

    @Override
    public Page<SysCommentVO> selectList(SysCommentQueryDto queryDto) {
        // 查询所有评论（分页）
        Page<SysCommentVO> page = baseMapper.selectPage(PageUtil.getPage(), queryDto);
        List<SysCommentVO> allComments = page.getRecords();

        // 按 id 建立映射
        Map<Integer, SysCommentVO> idMap = allComments.stream()
                .collect(Collectors.toMap(SysCommentVO::getId, c -> c));

        // 初始化每个节点的 children 列表
        allComments.forEach(c -> c.setChildren(new ArrayList<>()));

        // 递归构建：将每个节点挂到其父节点下
        List<SysCommentVO> rootComments = new ArrayList<>();
        for (SysCommentVO comment : allComments) {
            if (comment.getParentId() == null) {
                rootComments.add(comment);
            } else {
                SysCommentVO parent = idMap.get(comment.getParentId());
                if (parent != null) {
                    parent.getChildren().add(comment);
                } else {
                    // 父评论不在本页（跨页情况），当作根评论展示
                    rootComments.add(comment);
                }
            }
        }

        page.setRecords(rootComments);
        return page;
    }

    /**
     * 编辑评论 - 原作者或管理员可编辑
     * @param commentVO 评论信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editComment(SysCommentVO commentVO) {
        Integer commentId = commentVO.getId();
        String content = commentVO.getContent();
        Long replyUserId = commentVO.getReplyUserId();
        Integer isStick = commentVO.getIsStick();
        Integer likeCount = commentVO.getLikeCount();

        // 获取当前登录用户 ID
        long loginUserId = StpUtil.getLoginIdAsLong();

        // 查询原评论
        SysComment originalComment = baseMapper.selectById(commentId);
        if (originalComment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 权限校验：只有原作者或管理员可以编辑
        if (originalComment.getUserId() != null &&
            originalComment.getUserId() != loginUserId) {
            throw new RuntimeException("无权编辑该评论");
        }

        // 更新字段
        originalComment.setContent(content);
        if (replyUserId != null) {
            originalComment.setReplyUserId(replyUserId);
        }
        originalComment.setIsStick(isStick);
        if (likeCount != null) {
            originalComment.setLikeCount(likeCount);
        }
        originalComment.setUpdateTime(LocalDateTime.now());

        // 更新数据库
        baseMapper.updateById(originalComment);
    }
}
