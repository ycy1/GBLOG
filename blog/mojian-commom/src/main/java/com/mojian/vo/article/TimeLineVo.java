package com.mojian.vo.article;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xxj
 * @title TimeLineVo
 * @date 2025/9/24 21:47
 * @description TODO
 */
@Data
@ApiModel(value = "时间线列表视图对象")
public class TimeLineVo {

    @ApiModelProperty(value = "时间点")
    private String date;

    @ApiModelProperty(value = "文章数")
    private Integer articleNum;

}
