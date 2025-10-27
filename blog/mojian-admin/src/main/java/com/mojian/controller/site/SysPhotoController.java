package com.mojian.controller.site;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.RandomUtil;
import com.mojian.exception.ServiceException;
import com.mojian.utils.DateUtil;
import com.mojian.utils.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.x.file.storage.core.Downloader;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.web.bind.annotation.*;
import com.mojian.entity.SysPhoto;
import com.mojian.service.SysPhotoService;
import com.mojian.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * 照片 控制器
 */
@RestController
@RequestMapping("/sys/photo")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "照片管理")
public class SysPhotoController {

    private final SysPhotoService sysPhotoService;

    private final FileStorageService fileStorageService;

    @GetMapping("/list")
    @ApiOperation(value = "获取照片列表")
    public Result<IPage<SysPhoto>> list(SysPhoto sysPhoto) {
        return Result.success(sysPhotoService.selectPage(sysPhoto));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取照片详情")
    public Result<SysPhoto> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysPhotoService.getById(id));
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:photo:add")
    @ApiOperation(value = "添加照片")
    public Result<Object> add(@RequestBody SysPhoto sysPhoto) {
        return Result.success(sysPhotoService.insert(sysPhoto));
    }

    @SaCheckLogin
    @PostMapping("/uploadImageZip")
    @ApiOperation(value = "批量上传图片")
    public Result<String> uploadImageZip(MultipartFile file, String source, Long albumId) throws IOException {
        String path = DateUtil.parseDateToStr(DateUtil.YYYYMMDD, DateUtil.getNowDate()) + "/";
        //这个source可在前端上传文件时提供，可用来区分是头像还是文章图片等
        if (StringUtils.isNotBlank(source)) {
            path = path + source + "/";
        }
        String defaultPlatform = fileStorageService.getProperties().getDefaultPlatform();
        //获取文件名和后缀
        List<MultipartFile> multipartFiles = FileUtils.unzipToFiles(file);
        for (MultipartFile multipartFile : multipartFiles) {
            File compressFile = FileUtils.compressFile(multipartFile);
            FileInfo fileInfo = fileStorageService.of(compressFile)
                    .setPlatform(defaultPlatform)
                    .setPath(path)
                    .setSaveFilename(RandomUtil.randomNumbers(2) + "_" + file.getOriginalFilename()) //随机俩个数字，避免相同文件名时文件名冲突
                    .putAttr("source",source)
                    .upload();

            if (fileInfo == null) {
                throw new ServiceException("上传文件失败");
            }
            log.info("默认存储平台：{},{}",defaultPlatform,fileInfo.getUrl());
            // 插入数据库
            sysPhotoService.insert(new SysPhoto(fileInfo.getUrl(), albumId));
        }
        return Result.success();
    }


    @PutMapping("/update")
    @SaCheckPermission("sys:photo:update")
    @ApiOperation(value = "修改照片")
    public Result<Object> edit(@RequestBody SysPhoto sysPhoto) {
        return Result.success(sysPhotoService.update(sysPhoto));
    }

    @DeleteMapping("/delete/{ids}")
    @SaCheckPermission("sys:photo:delete")
    @ApiOperation(value = "删除照片")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysPhotoService.deleteByIds(ids));
    }

    @PutMapping("/move/{ids}")
    @SaCheckPermission("sys:photo:move")
    @ApiOperation(value = "移动照片")
    public Result<Object> move(@PathVariable List<Long> ids, @RequestParam Long albumId) {
        return Result.success(sysPhotoService.move(ids,albumId));
    }
}
