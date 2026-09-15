package com.mojian.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mojian.entity.SysArticlePayRule;
import com.mojian.exception.ServiceException;
import com.mojian.mapper.SysArticlePayRuleMapper;
import com.mojian.service.SysArticlePayRuleService;
import com.mojian.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章收费规则 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysArticlePayRuleServiceImpl extends ServiceImpl<SysArticlePayRuleMapper, SysArticlePayRule>
        implements SysArticlePayRuleService {

    /**
     * 查询文章收费规则分页列表
     */
    @Override
    public IPage<SysArticlePayRule> selectPage(SysArticlePayRule sysArticlePayRule) {
        return baseMapper.selectPageList(PageUtil.getPage(), sysArticlePayRule);
    }

    /**
     * 新增文章收费规则
     */
    @Override
    public boolean insert(SysArticlePayRule sysArticlePayRule) {
        checkTitle(sysArticlePayRule);
        return save(sysArticlePayRule);
    }

    /**
     * 修改文章收费规则
     */
    @Override
    public boolean update(SysArticlePayRule sysArticlePayRule) {
        checkTitle(sysArticlePayRule);
        return updateById(sysArticlePayRule);
    }

    /**
     * 规则名称必填
     */
    private void checkTitle(SysArticlePayRule sysArticlePayRule) {
        if (StringUtils.isBlank(sysArticlePayRule.getTitle())) {
            throw new ServiceException("规则名称不能为空");
        }
    }

    /**
     * 批量删除文章收费规则
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }
}
