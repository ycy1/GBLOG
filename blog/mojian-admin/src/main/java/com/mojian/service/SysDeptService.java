package com.mojian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mojian.entity.SysDept;
import com.mojian.vo.user.SysUserVo;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    /**
     * 获取部门树
     */
    List<SysDept> listDeptTree(SysDept dept);

    /**
     * 新增部门
     */
    void addDept(SysDept dept);

    /**
     * 修改部门
     */
    void updateDept(SysDept dept);

    /**
     * 删除部门
     */
    void deleteDept(List<Long> ids);

    /**
     * 分页查询部门下的人员
     */
    IPage<SysUserVo> getDeptUserPage(Long deptId);

    /**
     * 添加部门人员
     */
    void addDeptUsers(Long deptId, List<Integer> userIds);
}
