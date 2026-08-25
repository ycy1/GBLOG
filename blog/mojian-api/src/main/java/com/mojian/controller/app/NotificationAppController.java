package com.mojian.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Result;
import com.mojian.controller.BaseAppController;
import com.mojian.entity.SysNotifications;
import com.mojian.service.NotificationsService;
import com.mojian.vo.notifications.NotificationsListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author quequnlong
 * @date 2025/7/20
 * @description APP-消息通知管理
 */
@SaCheckLogin
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
@Api(tags = "APP-消息通知管理")
public class NotificationAppController extends BaseAppController {

    private final NotificationsService notificationsService;

    @GetMapping("/page")
    @ApiOperation(value = "分页查询消息通知")
    public Result<IPage<NotificationsListVo>> page(SysNotifications notifications) {
        return Result.success(notificationsService.page(notifications));
    }

    @GetMapping("/read/{id}")
    @ApiOperation(value = "已读消息")
    public Result<Void> doRead(@PathVariable Long id) {
        notificationsService.doRead(id);
        return Result.success();
    }

    @GetMapping("/read/all")
    @ApiOperation(value = "全部已读")
    public Result<Void> allRead() {
        notificationsService.allRead();
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除消息")
    public Result<Void> delete(@PathVariable Long id) {
        notificationsService.delete(id);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "获取消息详情")
    public Result<NotificationsListVo> getDetail(@PathVariable Long id) {
        return Result.success(notificationsService.getById(id));
    }

    @GetMapping("/unread/num")
    @ApiOperation(value = "获取未读消息数量")
    public Result<Map<String, Integer>> getUnReadNum() {
        return Result.success(notificationsService.getUnReadNum());
    }

    @GetMapping("/is-unread")
    @ApiOperation(value = "获取是否有未读消息")
    public Result<Boolean> getMyIsUnread() {
        return Result.success(notificationsService.getMyIsUnread());
    }
}
