package com.mojian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.vo.comment.CommentListVo;
import com.mojian.entity.SysComment;

public interface CommentService {

    /**
     * 获取评论列表
     * @param businessId 业务ID（文章/动态ID）
     * @param sortType 排序方式
     * @param commentType 评论类型 1-文章 2-动态
     * @return
     */
    IPage<CommentListVo> getComments(Integer businessId, String sortType, Integer commentType);

    /**
     * 新增评论
     * @param sysComment
     * @return
     */
    void add(SysComment sysComment);

    /**
     * 点赞/取消点赞评论
     * @param commentId 评论ID
     * @param commentType 评论类型 1-文章 2-动态
     * @return
     */
    Boolean likeComment(Integer commentId, Integer commentType);
}
