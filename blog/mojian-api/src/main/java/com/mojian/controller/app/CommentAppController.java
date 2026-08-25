package com.mojian.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.annotation.AccessLimit;
import com.mojian.common.Result;
import com.mojian.controller.BaseAppController;
import com.mojian.entity.SysComment;
import com.mojian.service.CommentService;
import com.mojian.vo.comment.CommentListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
@Api(tags = "APP-评论管理")
public class CommentAppController extends BaseAppController {

    private final CommentService commentService;

    @GetMapping("/list")
    @ApiOperation(value = "获取评论列表")
    public Result<IPage<CommentListVo>> getComments(Integer businessId, String sortType,
                                                    @RequestParam(required = false) Integer commentType) {
        return Result.success(commentService.getComments(businessId, sortType, commentType));
    }

    @SaCheckLogin
    @PostMapping("/add")
    @ApiOperation(value = "添加评论")
    public Result<Void> add(@RequestBody SysComment sysComment) {
        commentService.add(sysComment);
        return Result.success();
    }

    @SaCheckLogin
    @AccessLimit(time = 3, count = 1)
    @PostMapping("/like/{id}")
    @ApiOperation(value = "点赞/取消点赞评论")
    public Result<Boolean> likeComment(@PathVariable Integer id,
                                       @RequestParam(required = false) Integer commentType) {
        return Result.success(commentService.likeComment(id, commentType));
    }
}
