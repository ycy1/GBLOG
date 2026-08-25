package com.mojian.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Result;
import com.mojian.controller.BaseAppController;
import com.mojian.entity.SysArticle;
import com.mojian.entity.SysFriend;
import com.mojian.service.FriendService;
import com.mojian.service.MomentService;
import com.mojian.service.MyIndexService;
import com.mojian.service.UserService;
import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.moment.MomentPageVo;
import com.mojian.vo.dashboard.IndexVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xxj
 * @title MyAppController
 * @date 2025/9/23 17:18
 * @description TODO
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/my")
@Api(tags = "APP-我的管理")
public class MyAppController extends BaseAppController {
    private final MyIndexService indexService;
    private final FriendService friendService;
    private final UserService userService;
    private final MomentService momentService;

    @GetMapping
    @ApiOperation(value = "首页")
    public Result<IndexVo> index() {
        return Result.success(indexService.index());
    }

    @GetMapping("/friends")
    @ApiOperation(value = "友情链接列表")
    public Result<List<SysFriend>> getFriendList() {
        return Result.success(friendService.getFriendList());
    }

    @GetMapping("/article")
    @ApiOperation(value = "获取我的文章")
    public Result<IPage<ArticleListVo>> selectMyArticle(SysArticle article) {
        return Result.success(userService.selectMyArticle(article));
    }

    @DeleteMapping("/article/{id}")
    @ApiOperation(value = "删除我的文章")
    public Result<Void> deleteMyArticle(@PathVariable Long id) {
        userService.deleteMyArticle(id);
        return Result.success();
    }

    @GetMapping("/like")
    @ApiOperation(value = "获取我的点赞")
    public Result<IPage<ArticleListVo>> selectMyLike() {
        return Result.success(userService.selectMyLike());
    }

    @PostMapping("/like/cancel/{id}")
    @ApiOperation(value = "取消点赞文章")
    public Result<Void> cancelMyLike(@PathVariable Long id) {
        userService.cancelMyLike(id);
        return Result.success();
    }

    @GetMapping("/moment")
    @ApiOperation(value = "获取我的动态")
    public Result<IPage<MomentPageVo>> selectMyMoment() {
        return Result.success(momentService.getMyMomentList());
    }

    @DeleteMapping("/moment/{id}")
    @ApiOperation(value = "删除我的动态")
    public Result<Void> deleteMyMoment(@PathVariable Long id) {
        momentService.deleteMyMoment(id);
        return Result.success();
    }
}
