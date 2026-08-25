package com.mojian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.vo.moment.MomentPageVo;

/**
 * @author: quequnlong
 * @date: 2025/2/5
 * @description:
 */
public interface MomentService {
    IPage<MomentPageVo> getMomentList();

    MomentPageVo getMomentDetail(Long id);

    Boolean likeMoment(Long momentId);

    /**
     * 获取我的动态列表
     */
    IPage<MomentPageVo> getMyMomentList();

    /**
     * 删除我的动态
     */
    void deleteMyMoment(Long id);
}
