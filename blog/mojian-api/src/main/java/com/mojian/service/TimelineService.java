package com.mojian.service;

import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.article.TimeLineVo;

import java.util.List;

/**
 * @author xxj
 * @title TimelineService
 * @date 2025/9/24 21:44
 * @description TODO
 */
public interface TimelineService {

    List<TimeLineVo> getTimelineList();

    List<ArticleListVo> getTimelineArticles(String date);
}
