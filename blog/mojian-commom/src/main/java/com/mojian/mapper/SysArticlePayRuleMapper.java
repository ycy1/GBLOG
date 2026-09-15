package com.mojian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mojian.entity.SysArticlePayRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文章收费规则 Mapper接口
 */
@Mapper
public interface SysArticlePayRuleMapper extends BaseMapper<SysArticlePayRule> {

    IPage<SysArticlePayRule> selectPageList(Page<SysArticlePayRule> page, @Param("query") SysArticlePayRule query);
}
