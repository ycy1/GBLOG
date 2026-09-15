package com.mojian.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mojian.entity.SysOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mojian.entity.SysPhoto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单主表 Mapper接口
 */
@Mapper
public interface SysOrderMapper extends BaseMapper<SysOrder> {

    IPage<SysOrder> selectOrderPage(@Param("page") Page<SysOrder> page, @Param("query")SysOrder query);
} 