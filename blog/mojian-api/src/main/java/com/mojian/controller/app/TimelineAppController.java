package com.mojian.controller.app;

import com.mojian.common.Result;
import com.mojian.controller.BaseAppController;
import com.mojian.service.TagService;
import com.mojian.service.TimelineService;
import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.article.TimeLineVo;
import com.mojian.vo.tag.TagListVo;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xxj
 * @title timelineAppController
 * @date 2025/9/24 21:42
 * @description TODO
 */
@RestController()
@RequestMapping("/timeline")
@RequiredArgsConstructor
@Api(tags = "APP-时间线管理")
public class TimelineAppController extends BaseAppController {

    private final TimelineService timelineService;

    @GetMapping("/list")
    public Result<List<TimeLineVo>> getTagsApi() {
        return Result.success(timelineService.getTimelineList());
    }

    @GetMapping("/queryTimelineArticles")
    public Result<List<ArticleListVo>> getTimelineArticles(String date) {
        return Result.success(timelineService.getTimelineArticles(date));
    }

}
