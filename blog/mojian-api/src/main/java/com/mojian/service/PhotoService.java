package com.mojian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mojian.entity.SysAlbum;
import com.mojian.entity.SysPhoto;

import java.util.List;

/**
 * 照片 服务接口
 */
public interface PhotoService {
    /**
     * 查询照片分页列表
     */
    IPage<SysPhoto> selectPage(SysPhoto sysPhoto);

    /**
     * 批量删除照片
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 查询相册列表
     */
    List<SysAlbum> selectAlbumList(SysAlbum sysAlbum);

    /**
     * 根据id查询相册
     */
    SysAlbum getAlbumById(Long id);


}
