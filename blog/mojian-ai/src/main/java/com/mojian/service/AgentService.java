package com.mojian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mojian.dto.ai.AgentDto;
import com.mojian.dto.ai.AgentQueryDto;
import com.mojian.entity.Agent;
import com.mojian.vo.ai.AgentVo;

import java.util.List;

public interface AgentService extends IService<Agent> {

    /**
     * 分页查询智能体列表
     */
    IPage<AgentVo> selectPage(Page<Agent> page, AgentQueryDto dto);

    /**
     * 智能体详情
     */
    AgentVo detail(Long id);

    /**
     * 新增智能体
     */
    Boolean add(AgentDto dto);

    /**
     * 修改智能体
     */
    Boolean update(AgentDto dto);

    /**
     * 删除智能体
     */
    Boolean delete(List<Long> ids);

    /**
     * 修改智能体状态
     */
    Boolean updateStatus(Long id, Integer status);

    /**
     * 刷新知识库内容（重新生成 systemPrompt）
     */
    Boolean refreshKnowledge(Long id);
}
