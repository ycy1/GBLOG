package com.mojian.dto.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mojian.utils.DateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: quequnlong
 * @date: 2025/1/2
 * @description:
 */
@Data
@ApiModel(value = "评论信息")
public class SysCommentQueryDto {

    @ApiModelProperty(value = "关联的文章ID，表明该评论所属的文章")
    private Long articleId;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "回复人昵称")
    private String replyNickname;

    @ApiModelProperty(value = "文章标题")
    private String articleTitle;

    @ApiModelProperty(value = "评论内容")
    private String content;

    @ApiModelProperty(value = "评论时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS,timezone="GMT+8")
    private LocalDateTime createTime;
}
