package com.mojian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mojian.entity.SysArticlePayRule;

import java.util.List;

/**
 * 文章收费规则 服务接口
 */
public interface SysArticlePayRuleService extends IService<SysArticlePayRule> {
    /**
     * 查询文章收费规则分页列表
     */
    IPage<SysArticlePayRule> selectPage(SysArticlePayRule sysArticlePayRule);

    /**
     * 新增文章收费规则
     */
    boolean insert(SysArticlePayRule sysArticlePayRule);

    /**
     * 修改文章收费规则
     */
    boolean update(SysArticlePayRule sysArticlePayRule);

    /**
     * 批量删除文章收费规则
     */
    boolean deleteByIds(List<Long> ids);
}
