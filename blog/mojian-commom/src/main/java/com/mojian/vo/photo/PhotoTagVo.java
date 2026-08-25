package com.mojian.vo.photo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 照片标签视图对象（照片-标签关联查询结果）
 */
@Data
@ApiModel(value = "照片标签视图对象")
public class PhotoTagVo {

    @ApiModelProperty(value = "照片id")
    private Long photoId;

    @ApiModelProperty(value = "标签id")
    private Integer id;

    @ApiModelProperty(value = "标签名称")
    private String name;
}
