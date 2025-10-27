package com.mojian.service.impl;

import com.mojian.mapper.SysArticleMapper;
import com.mojian.mapper.SysTagMapper;
import com.mojian.service.TimelineService;
import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.article.TimeLineVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xxj
 * @title TimelineServicelmpl
 * @date 2025/9/24 21:50
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class TimelineServicelmpl implements TimelineService {

    private final SysArticleMapper sysArticleMapper;

    @Override
    public List<TimeLineVo> getTimelineList() {
        return sysArticleMapper.getTimelineList();
    }

    @Override
    public List<ArticleListVo> getTimelineArticles(String date) {
        return sysArticleMapper.getTimelineArticles(date);
    }
}
