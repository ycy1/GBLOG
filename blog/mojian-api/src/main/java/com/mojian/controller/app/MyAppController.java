package com.mojian.controller.app;

import com.mojian.common.Result;
import com.mojian.controller.BaseAppController;
import com.mojian.entity.SysFriend;
import com.mojian.service.FriendService;
import com.mojian.service.MyIndexService;
import com.mojian.vo.dashboard.IndexVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
