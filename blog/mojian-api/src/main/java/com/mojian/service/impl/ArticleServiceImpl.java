package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.mojian.common.RedisConstants;
import com.mojian.entity.SysArticle;
import com.mojian.entity.SysCategory;
import com.mojian.entity.SysNotifications;
import com.mojian.mapper.SysTagMapper;
import com.mojian.service.ArticleService;
import com.mojian.utils.IpUtil;
import com.mojian.utils.NotificationsUtil;
import com.mojian.utils.RedisUtil;
import com.mojian.vo.article.ArchiveListVo;
import com.mojian.vo.article.ArticleDetailVo;
import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.article.CategoryListVo;
import com.mojian.mapper.SysArticleMapper;
import com.mojian.mapper.SysCategoryMapper;
import com.mojian.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final SysArticleMapper sysArticleMapper;

    private final SysTagMapper sysTagMapper;

    private final SysCategoryMapper sysCategoryMapper;

    private final RedisUtil redisUtil;

    @Autowired
    @Qualifier("notificationsUtil")
    private NotificationsUtil notificationsUtil;

    @Override
    public IPage<ArticleListVo> getArticleList(Integer tagId, Integer categoryId, String keyword) {
        return sysArticleMapper.getArticleListApi(PageUtil.getPage(), tagId, categoryId, keyword);
    }

    @Override
    public ArticleDetailVo getArticleDetail(Long id) {
        ArticleDetailVo detailVo = sysArticleMapper.getArticleDetail(id);
        Assert.notNull(detailVo, "文章不存在");
        // 判断是否点赞
        Object userId = StpUtil.getLoginIdDefaultNull();
        if (userId != null) {
            detailVo.setIsLike(sysArticleMapper.getUserIsLike(id, Integer.parseInt(userId.toString())));
        }

        //添加阅读量 按IP计数
        String ip = IpUtil.getIp();
        ThreadUtil.execAsync(() -> {
            Map<Object, Object> map = redisUtil.hGetAll(RedisConstants.ARTICLE_QUANTITY);
            List<String> ipList = (List<String>) map.get(id.toString());
            if (ipList != null) {
                if (!ipList.contains(ip)) {
                    ipList.add(ip);
                }
            } else {
                ipList = new ArrayList<>();
                ipList.add(ip);
            }
            map.put(id.toString(), ipList);
            redisUtil.hSetAll(RedisConstants.ARTICLE_QUANTITY, map);
        });
        return detailVo;
    }

    @Override
    public List<ArchiveListVo> getArticleArchive() {

        List<ArchiveListVo> list = new ArrayList<>();

        List<Integer> years = sysArticleMapper.getArticleArchive();
        for (Integer year : years) {
            List<ArticleListVo> articleListVos = sysArticleMapper.getArticleByYear(year);
            list.add(new ArchiveListVo(year, articleListVos));
        }
        return list;
    }

    @Override
    public List<CategoryListVo> getArticleCategories() {
        return sysCategoryMapper.getArticleCategories();
    }

    @Override
    public List<ArticleListVo> getCarouselArticle() {
        return getArticlesByCondition(SysArticle::getIsCarousel);
    }

    @Override
    public List<ArticleListVo> getRecommendArticle() {
        return getArticlesByCondition(SysArticle::getIsRecommend);
    }

    // 使用独立的线程池，避免阻塞主线程
    // ExecutorService 默认会开启与当前 Web 上下文（如 Request、Session、安全上下文）隔离的线程。
    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(10);

    @Override
    public Boolean like(Long articleId) {
        // 判断是否点赞
        int userId = StpUtil.getLoginIdAsInt();
        Boolean isLike = sysArticleMapper.getUserIsLike(articleId, userId);
        if (isLike) {
            // 点过则取消点赞
            sysArticleMapper.unLike(articleId, userId);
            sysArticleMapper.updateLikeCount(articleId, -1);
        } else {
            sysArticleMapper.like(articleId, userId);
            sysArticleMapper.updateLikeCount(articleId, 1);
            //发送通知事件
            SysArticle article = sysArticleMapper.selectById(articleId);
            if (article == null) return true;
            long loginIdAsLong = StpUtil.getLoginIdAsLong();
            taskExecutor.submit(() -> {
                try {
                    SysNotifications notifications = SysNotifications.builder()
                            .title("文章点赞通知")
                            .businessId(articleId)
                            .businessType("article")
                            .noticePush(NotificationsUtil.buildNoticePush(article.getUserId()))
                            .fromUserId(loginIdAsLong)
                            .build();
                    notificationsUtil.publish(notifications);
                } catch (Exception e) {
                    log.error("发送通知失败", e);
                }
            });
        }
            return true;
    }

    @Override
    public List<SysCategory> getCategoryAll() {
        return sysCategoryMapper.selectList(new LambdaQueryWrapper<SysCategory>()
                .orderByAsc(SysCategory::getSort));
    }

    private List<ArticleListVo> getArticlesByCondition(SFunction<SysArticle, Object> conditionField) {
        LambdaQueryWrapper<SysArticle> wrapper = new LambdaQueryWrapper<SysArticle>()
                .select(SysArticle::getId, SysArticle::getTitle, SysArticle::getCover, SysArticle::getCreateTime,SysArticle::getSummary, SysArticle::getQuantity, SysArticle::getCategoryId)
                .orderByDesc(SysArticle::getCreateTime)
                .eq(SysArticle::getStatus, 1)  // 发布状态
                .eq(conditionField, 1);

        List<SysArticle> sysArticles = sysArticleMapper.selectList(wrapper);

        if (sysArticles == null || sysArticles.isEmpty()) {
            return Collections.emptyList();
        }

        return sysArticles.stream().map(item -> ArticleListVo.builder()
                .tags(sysTagMapper.getTagByArticleId(item.getId()))
                .categoryName(sysCategoryMapper.selectById(item.getCategoryId()).getName())
                .id(item.getId())
                .cover(item.getCover())
                .title(item.getTitle())
                .createTime(item.getCreateTime())
                .summary(item.getSummary())
                .quantity(item.getQuantity())
                .build()).collect(Collectors.toList());
    }
}
