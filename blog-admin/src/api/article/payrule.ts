import request from '@/utils/request'

/**
 * 获取文章收费规则列表
 */
export function listPayRuleApi(params?: any) {
    return request({
        url: '/sys/articlePayRule/list',
        method: 'get',
        params
    })
}

/**
 * 获取文章收费规则详情
 */
export function detailPayRuleApi(id: any) {
    return request({
        url: '/sys/articlePayRule/' + id,
        method: 'get'
    })
}

/**
 * 添加文章收费规则
 */
export function addPayRuleApi(data: any) {
    return request({
        url: '/sys/articlePayRule/add',
        method: 'post',
        data
    })
}

/**
 * 修改文章收费规则
 */
export function updatePayRuleApi(data: any) {
    return request({
        url: `/sys/articlePayRule/update`,
        method: 'put',
        data
    })
}

/**
 * 删除文章收费规则
 */
export function deletePayRuleApi(ids: number[] | number) {
    return request({
        url: `/sys/articlePayRule/delete/` + ids,
        method: 'delete'
    })
}
