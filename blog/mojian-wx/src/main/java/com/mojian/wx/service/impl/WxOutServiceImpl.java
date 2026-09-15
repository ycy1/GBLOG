package com.mojian.wx.service.impl;

import com.google.gson.JsonObject;
import com.mojian.service.WxOutService;
import com.mojian.utils.FileUtils;
import com.mojian.wx.utils.WxUtils;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.WxMpMaterial;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialUploadResult;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

import static me.chanjar.weixin.common.api.WxConsts.MediaFileType.VIDEO;

/**
 * @author xxj
 * @title WxOutServiceImpl
 * @date 2026/1/11 16:03
 * @description TODO
 */
@Service
@AllArgsConstructor
public class WxOutServiceImpl implements WxOutService {

    private final WxMpService wxService;

    /**
     * 添加草稿
     * @param appid
     * @param json 草稿信息
     */
    @Override
    public String addDraft(String appid, JsonObject json) throws Exception {
        String title = json.get("title").getAsString();
        String content = json.get("content").getAsString();
        String thumbMediaId = json.get("thumbMediaId").getAsString();
        List<JsonObject> imageSrc = WxUtils.getImageSrc(content);
        for (JsonObject jsonObject : imageSrc) {
            String imageUrl = getImageUrl(appid, jsonObject).get("url").getAsString();
            content = content.replace(jsonObject.get("url").getAsString(),imageUrl);
        }
        return wxService.switchoverTo(appid).getDraftService().addDraft(title,content,thumbMediaId);
    }

    @Override
    public JsonObject getImageUrl(String appid, JsonObject json) throws Exception {
        JsonObject res = new JsonObject();
        String url = json.get("url").getAsString();
        String name = json.get("name").getAsString();
        String title = json.get("title").getAsString();
        String introduction = json.get("introduction").getAsString();

        File webpFile = FileUtils.urlToFile(url);
        File uncompressFile = FileUtils.uncompressFile(webpFile);
        WxMpMaterialUploadResult result = wxService.switchoverTo(appid).getMaterialService().materialFileUpload(
                VIDEO, new WxMpMaterial(name, uncompressFile, title, introduction));
        res.addProperty("url", result.getUrl());
        res.addProperty("mediaId", result.getMediaId());
        res.addProperty("fileSize", FileUtils.convertFileSize(uncompressFile.length()));
        return res;
    }


}
