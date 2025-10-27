package com.mojian.controller.app;

import com.mojian.common.Result;
import com.mojian.controller.BaseAppController;
import com.mojian.dto.user.LoginUserInfo;
import com.mojian.service.TagService;
import com.mojian.vo.tag.TagListVo;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/tag")
@RequiredArgsConstructor
@Api(tags = "APP-标签管理")
public class TagAppController extends BaseAppController {

    private final TagService tagService;

    @GetMapping("/list")
    public Result<List<TagListVo>> getTagsApi() {
        return Result.success(tagService.getTagsApi());
    }


}
