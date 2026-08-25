package com.mojian.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.annotation.AccessLimit;
import com.mojian.common.Result;
import com.mojian.controller.BaseAppController;
import com.mojian.service.MomentService;
import com.mojian.vo.moment.MomentPageVo;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author: quequnlong
 * @date: 2025/2/5
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/moment")
@Api(tags = "APP-说说管理")
public class MomentAppController extends BaseAppController {

    private final MomentService momentService;

    @GetMapping("/list")
    @Operation(description = "说说列表")
    public Result<IPage<MomentPageVo>> getMomentList() {
        return Result.success(momentService.getMomentList());
    }

    @GetMapping("/detail/{id}")
    @Operation(description = "说说详情")
    public Result<MomentPageVo> getMomentDetail(@PathVariable Long id) {
        return Result.success(momentService.getMomentDetail(id));
    }

    @SaCheckLogin
    @AccessLimit(time = 3, count = 1)
    @GetMapping("/like/{id}")
    @Operation(description = "点赞/取消点赞说说")
    public Result<Boolean> likeMoment(@PathVariable Long id) {
        return Result.success(momentService.likeMoment(id));
    }
}
