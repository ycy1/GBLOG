package com.mojian.vo.user;

import com.mojian.entity.SysUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "用户分页视图对象")
public class SysUserVo extends SysUser {

    @ApiModelProperty(value = "角色id集合")
    private List<String> roles;

    /** 冗余字段   **/
    @ApiModelProperty(value = "权限列表")
    private List<String> permissions;

}
