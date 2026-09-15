package com.mojian.wx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mojian.entity.SysOrder;
import com.mojian.mapper.SysOrderMapper;
import com.mojian.wx.service.OrderWxService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author xxj
 * @title SysOrderWxServiceImpl
 * @date 2026/9/14 11:36
 * @description TODO
 */
@Service
@AllArgsConstructor
@Slf4j
public class OrderWxServiceImpl extends ServiceImpl<SysOrderMapper, SysOrder> implements OrderWxService {
    @Override
    public SysOrder createOrder(SysOrder sysOrder) {
        baseMapper.insert(sysOrder);
        return sysOrder;
    }
}
