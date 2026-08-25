package com.mojian.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mojian.common.Constants;
import com.mojian.common.RedisConstants;
import com.mojian.dto.user.SysUserAddAndUpdateDto;
import com.mojian.mapper.SysDeptUserMapper;
import com.mojian.mapper.SysRoleMapper;
import com.mojian.utils.DateUtil;
import com.mojian.utils.PageUtil;
import com.mojian.utils.QrCodeUtils;
import com.mojian.entity.SysUser;
import com.mojian.exception.ServiceException;
import com.mojian.mapper.SysUserMapper;
import com.mojian.service.SysUserService;
import com.mojian.utils.RedisUtil;
import com.mojian.vo.user.OnlineUserVo;
import com.mojian.vo.user.SysUserVo;
import com.mojian.vo.user.SysUserProfileVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mojian.dto.user.UpdatePwdDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysRoleMapper roleMapper;
    private final RedisUtil redisUtil;
    private final SysUserMapper sysUserMapper;
    private final FileStorageService fileStorageService;
    private final SysDeptUserMapper deptUserMapper;

    @Override
    public IPage<SysUserVo> listUsers(SysUser sysUser) {
        // 部门信息已由 selectUserPage 在 SQL 层聚合填充（deptIds/deptNames）
        return baseMapper.selectUserPage(PageUtil.getPage(),sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysUserAddAndUpdateDto SysUserAddAndUpdateDto) {
        // 检查用户名是否已存在
        SysUser user = SysUserAddAndUpdateDto.getUser();
        if (baseMapper.selectByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt()));
        save(user);

        //保存部门信息
        List<Long> deptIds = SysUserAddAndUpdateDto.getDeptIds();
        if (deptIds != null && !deptIds.isEmpty()) {
            deptUserMapper.insertDeptUser(user.getId(), deptIds.stream().distinct().collect(Collectors.toList()));
        }

        //保存角色信息
        if(SysUserAddAndUpdateDto.getRoleIds().isEmpty()) return;
        roleMapper.addRoleUser(user.getId(), SysUserAddAndUpdateDto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserAddAndUpdateDto user) {
        // 检查用户是否存在
        if (getById(user.getUser().getId()) == null) {
            throw new RuntimeException("用户不存在");
        }
        updateById(user.getUser());

        //修改部门 先删除再新增（空列表也执行删除，用于清除原有部门）
        deptUserMapper.deleteByUserIds(Collections.singletonList(user.getUser().getId()));
        List<Long> deptIds = user.getDeptIds();
        if (deptIds != null && !deptIds.isEmpty()) {
            deptUserMapper.insertDeptUser(user.getUser().getId(), deptIds.stream().distinct().collect(Collectors.toList()));
        }

        //修改角色 先删除角色再新增
        if(user.getRoleIds().isEmpty()) return;
        roleMapper.deleteRoleByUserId(Collections.singletonList(user.getUser().getId()));
        roleMapper.addRoleUser(user.getUser().getId(), user.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Integer> ids) {
        removeBatchByIds(ids);
        roleMapper.deleteRoleByUserId(ids);
        if (ids != null && !ids.isEmpty()) {
            deptUserMapper.deleteByUserIds(ids);
        }
    }


    @Override
    public void updatePwd(UpdatePwdDTO updatePwdDTO) {

        SysUser user = this.getById(StpUtil.getLoginIdAsInt());
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        if(!StpUtil.hasRole(Constants.ADMIN) && user.getId() != StpUtil.getLoginIdAsLong()) {
            throw new ServiceException("只能修改自己的密码！");
        }

        if (!BCrypt.checkpw(updatePwdDTO.getOldPassword(), user.getPassword())) {
            throw new ServiceException("旧密码错误");
        }

        user.setPassword(BCrypt.hashpw(updatePwdDTO.getNewPassword(),BCrypt.gensalt()));
        this.updateById(user);
    }

    @Override
    public SysUserProfileVo profile() {

        SysUser sysUser = baseMapper.selectById(StpUtil.getLoginIdAsInt());
        sysUser.setPassword(null);
        //获取角色
        List<String> roles = roleMapper.selectRolesByUserId(sysUser.getId());

        return SysUserProfileVo.builder().sysUser(sysUser).roles(roles).build();
    }

    @Override
    public void updateProfile(SysUser user) {
        baseMapper.updateById(user);
    }

    @Override
    public Boolean verifyPassword(String password) {
        SysUser user = baseMapper.selectById(StpUtil.getLoginIdAsInt());
        return BCrypt.checkpw(password, user.getPassword());
    }

    @Override
    public Boolean resetPassword(SysUser user) {
        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt()));
        baseMapper.updateById(user);
        return true;
    }

    @Override
    public IPage<OnlineUserVo> getOnlineUserList(String username) {
        Integer pageNum = PageUtil.getPageQuery().getPageNum();
        Integer pageSize = PageUtil.getPageQuery().getPageSize();

        // 返回数据对象
        Collection<String> keys = redisUtil.keys(RedisConstants.LOGIN_TOKEN.concat( "*"));

        List<OnlineUserVo> totalList = new ArrayList<>();
        for (String key : keys) {
            Object userObj = redisUtil.get(key);
            OnlineUserVo onlineUser = JSONUtil.toBean(userObj.toString(), OnlineUserVo.class);
            if (StringUtils.isNotBlank(username)) {
                if (onlineUser.getUsername().contains(username)) {
                    totalList.add(onlineUser);
                }
                continue;
            }
            totalList.add(onlineUser);
        }

        //根据时间排序
        totalList.sort((o1, o2) -> o2.getLastLoginTime().compareTo(o1.getLastLoginTime()));

        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = totalList.size() - fromIndex > pageSize ? fromIndex + pageSize : totalList.size();
        List<OnlineUserVo> records = totalList.subList(fromIndex, toIndex);

        IPage<OnlineUserVo> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(totalList.size());
        return page;
    }

    @Override
    public String generateQr(Integer userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        try {
            // 1. 组装二维码纯文本内容并生成 PNG（地址优先用中文，缺失时回退地区编码）
            String address = StringUtils.isNotBlank(user.getAreaZh()) ? user.getAreaZh() : user.getAreaCode();
            String content = "用户名称：" + (user.getNickname() == null ? "" : user.getNickname())
                    + "\n账号：" + (user.getUsername() == null ? "" : user.getUsername())
                    + "\n电话：" + (user.getMobile() == null ? "" : user.getMobile())
                    + "\n地址：" + (address == null ? "" : address);
            byte[] pngBytes = QrCodeUtils.generatePng(content, 300);
            // 2. 写入临时文件
            File qrFile = File.createTempFile("qr_" + userId + "_", ".png");
            FileUtil.writeBytes(pngBytes, qrFile);
            // 3. 上传到文件存储
            String path = DateUtil.parseDateToStr(DateUtil.YYYYMMDD, DateUtil.getNowDate()) + "/qr/";
            String defaultPlatform = fileStorageService.getProperties().getDefaultPlatform();
            FileInfo fileInfo = fileStorageService.of(qrFile)
                    .setPlatform(defaultPlatform)
                    .setPath(path)
                    .setSaveFilename(RandomUtil.randomNumbers(2) + "_" + userId + "_qr.png")
                    .putAttr("source", "qr")
                    .upload();
            FileUtil.del(qrFile);
            if (fileInfo == null) {
                throw new ServiceException("二维码上传失败");
            }
            // 4. 更新用户 qr_img
            SysUser update = new SysUser();
            update.setId(userId);
            update.setQrImg(fileInfo.getUrl());
            this.updateById(update);
            return fileInfo.getUrl();
        } catch (IOException e) {
            log.error("生成二维码失败", e);
            throw new ServiceException("生成二维码失败");
        }
    }
}
