package com.mojian.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mojian.entity.SysTag;
import com.mojian.service.FileDetailService;
import com.mojian.utils.FileUtils;
import com.mojian.vo.photo.PhotoTagVo;
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
        IPage<SysPhoto> page = baseMapper.selectPhotoPage(PageUtil.getPage(), sysPhoto);
        fillTags(page.getRecords());
        return page;
    }

    /**
     * 查询照片列表
     */
    @Override
    public List<SysPhoto> selectList(SysPhoto sysPhoto) {
        List<SysPhoto> list = baseMapper.selectPhotoList(sysPhoto);
        fillTags(list);
        return list;
    }

    /**
     * 新增照片
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insert(SysPhoto sysPhoto) {
        boolean result = save(sysPhoto);
        if (result) {
            savePhotoTags(sysPhoto);
        }
        return result;
    }

    /**
     * 修改照片
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysPhoto sysPhoto) {
        boolean result = updateById(sysPhoto);
        if (result) {
            // 重新维护标签关联
            baseMapper.deletePhotoTagsByPhotoIds(Collections.singletonList(sysPhoto.getId()));
            savePhotoTags(sysPhoto);
        }
        return result;
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
        // 删除照片标签关联
        baseMapper.deletePhotoTagsByPhotoIds(ids);
        // 删除数据库数据
        return removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object move(List<Long> ids, Long albumId) {
        baseMapper.move(ids, albumId);
        return Boolean.TRUE;
    }

    /**
     * 批量给照片添加标签（保留原有标签，去重后追加）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setBatchTags(List<Long> ids, List<Integer> tagIds) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        if (tagIds == null || tagIds.isEmpty()) {
            return true;
        }
        List<Integer> distinctTagIds = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (distinctTagIds.isEmpty()) {
            return true;
        }
        // 只移除这批照片上「待添加」的标签对，再批量写入，实现幂等合并（不触碰原有其他标签）
        baseMapper.deletePhotoTagRelations(ids, distinctTagIds);
        baseMapper.addPhotoTagRelationsBatch(ids, distinctTagIds);
        return true;
    }

    /**
     * 保存照片标签关联
     */
    private void savePhotoTags(SysPhoto sysPhoto) {
        List<SysTag> tags = sysPhoto.getTags();
        if (tags == null || tags.isEmpty()) {
            return;
        }
        List<Integer> tagIds = tags.stream()
                .map(SysTag::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!tagIds.isEmpty()) {
            baseMapper.addPhotoTagRelations(sysPhoto.getId(), tagIds);
        }
    }

    /**
     * 批量填充照片标签
     */
    private void fillTags(List<SysPhoto> photos) {
        if (photos == null || photos.isEmpty()) {
            return;
        }
        List<Long> photoIds = photos.stream().map(SysPhoto::getId).collect(Collectors.toList());
        List<PhotoTagVo> tagVos = baseMapper.selectPhotoTagsByPhotoIds(photoIds);
        if (tagVos == null || tagVos.isEmpty()) {
            return;
        }
        Map<Long, List<SysTag>> tagMap = tagVos.stream()
                .collect(Collectors.groupingBy(PhotoTagVo::getPhotoId,
                        Collectors.mapping(vo -> {
                            SysTag tag = new SysTag();
                            tag.setId(vo.getId());
                            tag.setName(vo.getName());
                            tag.setType("photo");
                            return tag;
                        }, Collectors.toList())));
        photos.forEach(p -> p.setTags(tagMap.get(p.getId())));
    }
}
