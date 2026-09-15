package com.mojian.service;

import com.google.gson.JsonObject;

/**
 * @author xxj
 * @title WxOutService
 * @date 2026/1/11 16:00
 * @description TODO
 */
public interface WxOutService {

    /**
     * 添加草稿
     * @param appid
     * @param json 草稿信息 title content thumbMediaId
     */
    String addDraft(String appid, JsonObject json) throws Exception;

    /**
     * 获取图片url
     * @param appid
     * @param json 图片信息 url name title introduction
     */
    JsonObject getImageUrl(String appid, JsonObject json) throws Exception;

}
