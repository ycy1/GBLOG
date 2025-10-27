package com.mojian.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojian.entity.SysAlbum;
import com.mojian.entity.SysPhoto;
import com.mojian.exception.ServiceException;
import com.mojian.mapper.SysAlbumMapper;
import com.mojian.mapper.SysPhotoMapper;
import com.mojian.service.AlbumService;
import com.mojian.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.Downloader;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: quequnlong
 * @date: 2025/2/7
 * @description:
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService {

    private final SysAlbumMapper baseMapper;

    private final SysPhotoMapper photoMapper;

    private final FileStorageService fileStorageService;

    @Override
    public List<SysAlbum> getAlbumList() {
        return baseMapper.getAlbumList();
    }

    @Override
    public List<SysPhoto> getPhotos(Long albumId) {
        return photoMapper.selectList(new LambdaQueryWrapper<SysPhoto>()
                .eq(SysPhoto::getAlbumId, albumId)
                .orderByAsc(SysPhoto::getSort)
                .orderByDesc(SysPhoto::getRecordTime));
    }

    @Override
    public Boolean verify(Long id, String password) {
        SysAlbum album = baseMapper.selectById(id);
        if (album == null) {
            throw new ServiceException("相册不存在!");
        }
        return BCrypt.checkpw(password, album.getPassword());
    }

    @Override
    public SysAlbum detail(Long id) {
        SysAlbum sysAlbum = baseMapper.selectById(id);
        if (sysAlbum == null) {
            throw new ServiceException("相册不存在!");
        }
        sysAlbum.setPassword(null);
        return sysAlbum;
    }

    @Override
    public byte[] download(Long albumId) {
        byte[] bytes = new byte[0];
        try {
            Map<String, byte[]> dataMap = new HashMap<>();
            List<SysPhoto> sysPhotos = this.getPhotos(albumId);
            for (SysPhoto sysPhoto : sysPhotos) {
                String url = sysPhoto.getUrl();
                boolean exists = fileStorageService.exists(url);
                if (!exists) {
                    throw new ServiceException("文件不存在");
                }
                FileInfo fileInfoByUrl = fileStorageService.getFileInfoByUrl(url);
                Downloader download = fileStorageService.download(url);
                dataMap.put(fileInfoByUrl.getFilename(), download.bytes());
            }
            bytes = FileUtils.compressMultiple(dataMap);

        } catch (Exception e) {
            log.error("文件下载异常,{}", e.getMessage());
        }
        return bytes;
    }
}
