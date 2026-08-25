package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mojian.vo.moment.MomentPageVo;
import org.springframework.stereotype.Service;
import com.mojian.mapper.SysMomentMapper;
import com.mojian.entity.SysMoment;
import com.mojian.service.SysMomentService;
import com.mojian.utils.PageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

/**
 * 说说 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysMomentServiceImpl extends ServiceImpl<SysMomentMapper, SysMoment> implements SysMomentService {

    private final SysMomentMapper sysMomentMapper;  // 使用自定义的mapper
    /**
     * 查询说说分页列表
     */
    @Override
    public IPage<MomentPageVo> selectPage(SysMoment sysMoment) {
        Object loginIdObj = StpUtil.getLoginIdDefaultNull();
        Long userId = loginIdObj != null ? Long.valueOf(loginIdObj.toString()) : null;
        return sysMomentMapper.selectPage(PageUtil.getPage(), userId);
    }

    @Override
    public Object add(SysMoment sysMoment) {
        sysMoment.setUserId(StpUtil.getLoginIdAsLong());
        return save(sysMoment);
    }
}
