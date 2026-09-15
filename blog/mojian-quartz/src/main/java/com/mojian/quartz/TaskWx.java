package com.mojian.quartz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojian.entity.SysFileCenter;
import com.mojian.mapper.SysFileCenterMapper;
import com.mojian.wx.service.WxPayService;
import com.mojian.wx.service.WxRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component("wxTask")
@RequiredArgsConstructor
public class TaskWx {

    private final WxPayService wxPayService;
    private final WxRefundService wxRefundService;

    /**
     * 微信支付超时关单任务
     */
    public void payTimeout() {

    }


    /**
     * 微信退款超时关单任务
     */
    public void refundTimeout() {

    }
}
