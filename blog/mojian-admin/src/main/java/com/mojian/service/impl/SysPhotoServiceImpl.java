package com.mojian.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojian.exception.ServiceException;
import com.mojian.service.FileDetailService;
import com.mojian.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.Downloader;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import com.mojian.mapper.SysPhotoMapper;
import com.mojian.entity.SysPhoto;
import com.mojian.service.SysPhotoService;
import com.mojian.utils.PageUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;

/**
 * 照片 服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysPhotoServiceImpl extends ServiceImpl<SysPhotoMapper, SysPhoto> implements SysPhotoService {

    private final FileStorageService fileStorageService;
    /**
     * 查询照片分页列表
     */
    @Override
    public IPage<SysPhoto> selectPage(SysPhoto sysPhoto) {
        LambdaQueryWrapper<SysPhoto> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(sysPhoto.getId() != null, SysPhoto::getId, sysPhoto.getId());
        wrapper.eq(sysPhoto.getAlbumId() != null, SysPhoto::getAlbumId, sysPhoto.getAlbumId());
        wrapper.eq(sysPhoto.getDescription() != null, SysPhoto::getDescription, sysPhoto.getDescription());
        wrapper.eq(sysPhoto.getUrl() != null, SysPhoto::getUrl, sysPhoto.getUrl());
        wrapper.eq(sysPhoto.getRecordTime() != null, SysPhoto::getRecordTime, sysPhoto.getRecordTime());
        wrapper.eq(sysPhoto.getSort() != null, SysPhoto::getSort, sysPhoto.getSort());
        wrapper.eq(sysPhoto.getCreateTime() != null, SysPhoto::getCreateTime, sysPhoto.getCreateTime());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询照片列表
     */
    @Override
    public List<SysPhoto> selectList(SysPhoto sysPhoto) {
        LambdaQueryWrapper<SysPhoto> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(sysPhoto.getId() != null, SysPhoto::getId, sysPhoto.getId());
        wrapper.eq(sysPhoto.getAlbumId() != null, SysPhoto::getAlbumId, sysPhoto.getAlbumId());
        wrapper.eq(sysPhoto.getDescription() != null, SysPhoto::getDescription, sysPhoto.getDescription());
        wrapper.eq(sysPhoto.getUrl() != null, SysPhoto::getUrl, sysPhoto.getUrl());
        wrapper.eq(sysPhoto.getRecordTime() != null, SysPhoto::getRecordTime, sysPhoto.getRecordTime());
        wrapper.eq(sysPhoto.getSort() != null, SysPhoto::getSort, sysPhoto.getSort());
        wrapper.eq(sysPhoto.getCreateTime() != null, SysPhoto::getCreateTime, sysPhoto.getCreateTime());
        return list(wrapper);
    }

    /**
     * 新增照片
     */
    @Override
    public boolean insert(SysPhoto sysPhoto) {
        return save(sysPhoto);
    }

    /**
     * 修改照片
     */
    @Override
    public boolean update(SysPhoto sysPhoto) {
        return updateById(sysPhoto);
    }

    /**
     * 批量删除照片
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        List<SysPhoto> sysPhotos = baseMapper.selectBatchIds(ids);
        for (SysPhoto sysPhoto : sysPhotos) {
            try {
                // 删除fastdfs文件
                fileStorageService.delete(sysPhoto.getUrl());
            } catch (Exception e) {
                log.error("文件删除异常,{}", e.getMessage());
            }
        }
        // 删除数据库数据
        return removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object move(List<Long> ids, Long albumId) {
        baseMapper.move(ids, albumId);
        return Boolean.TRUE;
    }
}
