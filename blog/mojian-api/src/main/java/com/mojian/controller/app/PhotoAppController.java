package com.mojian.controller.app;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Result;
import com.mojian.controller.BaseAppController;
import com.mojian.entity.SysAlbum;
import com.mojian.entity.SysPhoto;
import com.mojian.service.PhotoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 照片 控制器
 */
@RestController
@RequestMapping("/photo")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "APP-照片管理")
public class PhotoAppController extends BaseAppController {

    private final PhotoService photoService;


    @GetMapping("/list")
    @ApiOperation(value = "获取照片列表")
    public Result<IPage<SysPhoto>> list(SysPhoto sysPhoto) {
        return Result.success(photoService.selectPage(sysPhoto));
    }

//    @GetMapping("/{id}")
//    @ApiOperation(value = "获取照片详情")
//    public Result<SysPhoto> getInfo(@PathVariable("id") Long id) {
//        return Result.success(photoService.getById(id));
//    }


//    @DeleteMapping("/delete/{ids}")
//    @SaCheckPermission("sys:photo:delete")
//    @ApiOperation(value = "删除照片")
//    public Result<Object> remove(@PathVariable List<Long> ids) {
//        return Result.success(photoService.deleteByIds(ids));
//    }

    @GetMapping("/allAlbum")
    @ApiOperation(value = "获取所有相册列表")
    public Result<List<SysAlbum>> selectList(SysAlbum sysAlbum) {
        return Result.success(photoService.selectAlbumList(sysAlbum));
    }

//    @GetMapping("/{id}")
//    @ApiOperation(value = "获取相册详情")
//    public Result<SysAlbum> getAlbumInfo(@PathVariable("id") Long id) {
//        return Result.success(photoService.getAlbumById(id));
//    }

}
