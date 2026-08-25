package com.mojian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mojian.entity.SysPhoto;
import com.mojian.vo.photo.PhotoTagVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: quequnlong
 * @date: 2025/2/7
 * @description:
 */
@Mapper
public interface SysPhotoMapper extends BaseMapper<SysPhoto> {

    void move(@Param("ids") List<Long> ids, @Param("albumId") Long albumId);

    /**
     * 批量添加照片标签关联
     */
    void addPhotoTagRelations(@Param("photoId") Long photoId, @Param("tagIds") List<Integer> tagIds);

    /**
     * 按照片删除标签关联
     */
    void deletePhotoTagsByPhotoIds(@Param("photoIds") List<Long> photoIds);

    /**
     * 按 (照片 × 标签) 精确删除关联（批量追加标签前清掉待添加对，保留原有标签）
     */
    void deletePhotoTagRelations(@Param("photoIds") List<Long> photoIds, @Param("tagIds") List<Integer> tagIds);

    /**
     * 批量添加照片-标签关联（photoIds × tagIds 交叉）
     */
    void addPhotoTagRelationsBatch(@Param("photoIds") List<Long> photoIds, @Param("tagIds") List<Integer> tagIds);

    /**
     * 批量查询照片的标签（JOIN sys_tag）
     */
    List<PhotoTagVo> selectPhotoTagsByPhotoIds(@Param("photoIds") List<Long> photoIds);

    /**
     * 分页查询照片（筛选条件全部走 SQL）
     */
    IPage<SysPhoto> selectPhotoPage(Page<SysPhoto> page, @Param("query") SysPhoto query);

    /**
     * 查询照片列表（筛选条件全部走 SQL）
     */
    List<SysPhoto> selectPhotoList(@Param("query") SysPhoto query);
}
