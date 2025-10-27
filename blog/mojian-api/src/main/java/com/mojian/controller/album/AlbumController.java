package com.mojian.controller.album;

import com.mojian.annotation.AccessLimit;
import com.mojian.common.Result;
import com.mojian.entity.SysAlbum;
import com.mojian.entity.SysPhoto;
import com.mojian.service.AlbumService;
import com.mojian.utils.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author: quequnlong
 * @date: 2025/2/7
 * @description:
 */
@RestController
@RequestMapping("/api/album")
@RequiredArgsConstructor
@Api(tags = "门户-相册管理")
@Slf4j
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping("/list")
    @ApiOperation(value = "获取相册列表")
    public Result<List<SysAlbum>> getAlbumList() {
        return Result.success(albumService.getAlbumList());
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "获取相册详情")
    public Result<SysAlbum> detail(@PathVariable Long id) {
        return Result.success(albumService.detail(id));
    }

    @GetMapping("/photos/{albumId}")
    @ApiOperation(value = "获取照片列表")
    public Result<List<SysPhoto>> getPhotos(@PathVariable Long albumId) {
        return Result.success(albumService.getPhotos(albumId));
    }

    @AccessLimit(count = 5, time = 30)
    @GetMapping("/verify/{id}")
    @ApiOperation(value = "验证相册的密码")
    public Result<Boolean> verify(@PathVariable Long id,String password) {
        return albumService.verify(id,password) ? Result.success(true) : Result.error("密码错误");
    }

    @GetMapping("/download")
    @ApiOperation(value = "下载文件")
    public void download(Long albumId, HttpServletResponse response) {
        try{
            byte[] download = albumService.download(albumId);
            response.setHeader("Content-Disposition", "attachment;filename=" + DateUtil.dateTimeNow(DateUtil.YYYYMMDDHHMMSS) + ".zip");
            // 设置响应头
            response.setContentType("application/octet-stream");
            // 设置内容长度（可选但推荐）
            response.setContentLength(download.length);
//            response.setCo
            ServletOutputStream outputStream = response.getOutputStream();
            // 写入响应
            outputStream.write(download);
            outputStream.flush();
            outputStream.close();
        }catch (IOException e){
            log.error("文件下载异常,{}", e.getMessage());
        }
    }
}
