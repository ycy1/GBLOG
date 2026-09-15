package com.mojian.controller.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Result;
import com.mojian.entity.SysArticlePayRule;
import com.mojian.service.SysArticlePayRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章收费规则 控制器
 */
@RestController
@RequestMapping("/sys/articlePayRule")
@RequiredArgsConstructor
@Api(tags = "文章收费规则管理")
public class SysArticlePayRuleController {

    private final SysArticlePayRuleService sysArticlePayRuleService;

    @GetMapping("/list")
    @ApiOperation(value = "获取文章收费规则列表")
    public Result<IPage<SysArticlePayRule>> list(SysArticlePayRule sysArticlePayRule) {
        return Result.success(sysArticlePayRuleService.selectPage(sysArticlePayRule));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取文章收费规则详情")
    public Result<SysArticlePayRule> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysArticlePayRuleService.getById(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加文章收费规则")
    public Result<Object> add(@RequestBody SysArticlePayRule sysArticlePayRule) {
        return Result.success(sysArticlePayRuleService.insert(sysArticlePayRule));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改文章收费规则")
    public Result<Object> edit(@RequestBody SysArticlePayRule sysArticlePayRule) {
        return Result.success(sysArticlePayRuleService.update(sysArticlePayRule));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除文章收费规则")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysArticlePayRuleService.deleteByIds(ids));
    }
}
