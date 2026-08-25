package com.mojian.mapper;

import com.mojian.entity.SysAlbum;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 相册 Mapper接口
 */
@Mapper
public interface SysAlbumMapper extends BaseMapper<SysAlbum> {

    List<SysAlbum> getAlbumList();

    /**
     * 分页查询相册（LEFT JOIN 统计照片数，筛选条件全部走 SQL）
     */
    IPage<SysAlbum> selectAlbumPage(Page<SysAlbum> page, @Param("query") SysAlbum query);

}
