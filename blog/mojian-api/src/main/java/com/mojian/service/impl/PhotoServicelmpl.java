package com.mojian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.entity.SysAlbum;
import com.mojian.entity.SysPhoto;
import com.mojian.mapper.SysAlbumMapper;
import com.mojian.mapper.SysPhotoMapper;
import com.mojian.service.PhotoService;
import com.mojian.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xxj
 * @title PhotoServicelmpl
 * @date 2025/9/24 23:01
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class PhotoServicelmpl implements PhotoService {

    private final SysPhotoMapper sysPhotoMapper;
    private final SysAlbumMapper sysAlbumMapper;

    @Override
    public IPage<SysPhoto> selectPage(SysPhoto sysPhoto) {
        return sysPhotoMapper.selectPage(PageUtil.getPage(), new LambdaQueryWrapper<>(sysPhoto));
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        return false;
    }

    @Override
    public List<SysAlbum> selectAlbumList(SysAlbum sysAlbum) {
        return sysAlbumMapper.getAlbumList();
    }

    @Override
    public SysAlbum getAlbumById(Long id) {
        return null;
    }
}
