package com.mojian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mojian.entity.SysMoment;
import com.mojian.vo.moment.MomentPageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: quequnlong
 * @date: 2025/2/5
 * @description:
 */
@Mapper
public interface SysMomentMapper extends BaseMapper<SysMoment> {

    IPage<MomentPageVo> selectPage(IPage<SysMoment> page, @Param("userId") Long userId);

    /**
     * 我的动态分页查询
     */
    IPage<MomentPageVo> selectMyMomentPage(IPage<SysMoment> page, @Param("userId") Long userId);

    MomentPageVo selectDetail(@Param("id") Long id, @Param("userId") Long userId);

    void likeMoment(@Param("momentId") Long momentId, @Param("userId") Long userId);

    void unLikeMoment(@Param("momentId") Long momentId, @Param("userId") Long userId);

    Boolean getUserIsLikeMoment(@Param("momentId") Long momentId, @Param("userId") Long userId);

    void updateLikeCount(@Param("momentId") Long momentId, @Param("delta") int delta);

}
