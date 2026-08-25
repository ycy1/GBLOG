package com.mojian.service;

import com.mojian.entity.SysPhoto;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Collection;
import java.util.List;

/**
 * 照片 服务接口
 */
public interface SysPhotoService extends IService<SysPhoto> {
    /**
     * 查询照片分页列表
     */
    IPage<SysPhoto> selectPage(SysPhoto sysPhoto);

    /**
     * 查询照片列表
     */
    List<SysPhoto> selectList(SysPhoto sysPhoto);

    /**
     * 新增照片
     */
    boolean insert(SysPhoto sysPhoto);

    /**
     * 修改照片
     */
    boolean update(SysPhoto sysPhoto);

    /**
     * 批量删除照片
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 批量移动照片
     * @param ids
     * @param albumId
     * @return
     */
    Object move(List<Long> ids, Long albumId);

    /**
     * 批量给照片添加标签（保留原有标签，去重后追加）
     * @param ids 照片id列表
     * @param tagIds 标签id列表
     */
    boolean setBatchTags(List<Long> ids, List<Integer> tagIds);

}
