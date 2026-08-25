package com.mojian.service;

import com.mojian.entity.SysMoment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.vo.moment.MomentPageVo;

/**
 * 说说 服务接口
 */
public interface SysMomentService extends IService<SysMoment> {
    /**
     * 查询说说分页列表
     */
    IPage<MomentPageVo> selectPage(SysMoment sysMoment);

    Object add(SysMoment sysMoment);

}
