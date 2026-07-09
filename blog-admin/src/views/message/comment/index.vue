<template>
    <div class="app-container">

      <!-- 搜索表单 -->
      <div class="search-wrapper">
        <el-form :model="queryParams" ref="queryFormRef" inline>
          <el-form-item label="文章标题" prop="articleTitle">
            <el-input v-model="queryParams.articleTitle" placeholder="请输入文章标题" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="用户昵称" prop="nickname">
            <el-input v-model="queryParams.nickname" placeholder="请输入用户昵称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="评论内容" prop="content">
            <el-input v-model="queryParams.content" placeholder="请输入评论内容" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作按钮区域 -->
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <div v-if="articleTitle" style="color: var(--el-color-primary); font-size: 14px; margin-right: 20px;">
              当前文章：{{ articleTitle }}
              <el-button type="text" size="small" @click="clearArticleFilter">查看全部评论</el-button>
            </div>
            <ButtonGroup>
              <el-button
                v-permission="['sys:comment:delete']"
                type="danger"
                icon="Delete"
                :disabled="selectedIds.length === 0"
                @click="handleBatchDelete"
              >批量删除</el-button>
            </ButtonGroup>
          </div>
        </template>
  
        <!-- 数据表格 -->
        <el-table
          v-loading="loading"
          :data="commentList"
          style="width: 100%"
          row-key="id"
          :tree-props="{ children: 'children' }"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection"  width="55" align="center" />
          <el-table-column label="用户昵称" align="center" width="120" prop="nickname" show-overflow-tooltip />
          <el-table-column label="回复人昵称" align="center" width="120" prop="replyNickname" show-overflow-tooltip />
          <el-table-column label="文章标题" align="center" prop="articleTitle" show-overflow-tooltip />
          <el-table-column label="评论内容" width="300" align="center" prop="content" show-overflow-tooltip>
            <template #default="scope">
                <span v-html="scope.row.content"></span>
            </template>
          </el-table-column>
          <el-table-column label="点赞数" width="80" align="center" prop="likeCount" />
          <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
          <el-table-column label="操作" align="center" width="280" fixed="right">
            <template #default="scope">
              <TableMoreActions
                :actions="[
                  {
                    label: '编辑',
                    icon: 'Edit',
                    command: { type: 'edit', row: scope.row }
                  },
                  {
                    label: '删除',
                    type: 'danger',
                    icon: 'Delete',
                    disabled: !hasPermission('sys:comment:delete'),
                    command: { type: 'delete', row: scope.row }
                  },
                  
                ]"
                @command="handleActionCommand"
              />
            </template>
          </el-table-column>
        </el-table>
  
        <!-- 分页组件 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 30, 50]"
            :total="total"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>

        <!-- 编辑对话框 -->
        <el-dialog v-model="openEditDialog" title="编辑评论" width="600px" append-to-body>
          <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
            <el-form-item label="评论内容">
              <el-input type="textarea" :rows="6" v-model="editForm.content" placeholder="请输入评论内容" />
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="是否置顶">
                  <el-radio-group v-model="editForm.isStick">
                    <el-radio :label="0">否</el-radio>
                    <el-radio :label="1">是</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="点赞数">
                  <el-input-number v-model="editForm.likeCount" :min="0" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="cancelEdit">取 消</el-button>
              <el-button type="primary" @click="submitEdit">确 定</el-button>
            </div>
          </template>
        </el-dialog>
      </el-card>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance } from 'element-plus'
  import { useRoute } from 'vue-router'
  import {
    getCommentListApi,
    deleteCommentApi,
    editCommentApi
  } from '@/api/message/comment'
  import TableMoreActions from '@/components/TableMoreActions/index.vue'
  import { useUserStore } from '@/store/modules/user'

  const route = useRoute()
  const queryFormRef = ref<FormInstance>()

  // 获取用户权限信息
  const userStore = useUserStore()
  const permissions = computed(() => userStore.user.permissions || [])

  // 权限检查函数
  const hasPermission = (permission: string): boolean => {
    return permissions.value.includes(permission)
  }

  // 处理操作命令
  const handleActionCommand = async (action: any) => {
    const { type, row } = action.command

    switch (type) {
      case 'edit':
        handleEdit(row)
        break
      case 'delete':
        handleDelete(row)
        break
    }
  }

  const articleTitle = ref('')

  // 查询参数
  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    articleId: undefined as number | undefined,
    articleTitle: '',
    nickname: '',
    content: ''
  })
  
  const loading = ref(false)
  const total = ref(0)
  const commentList = ref([])
  
  // 选中项数组
  const selectedIds = ref<string[]>([])
  
  // 获取标签列表
  const getList = async () => {
    loading.value = true
    try {
      const { data } = await getCommentListApi(queryParams)
      commentList.value = data.records
      total.value = data.total
    } catch (error) {
    }
    loading.value = false
  }
  
  // 表格选择项变化
  const handleSelectionChange = (selection: any[]) => {
    selectedIds.value = selection.map(item => item.id)
  }
  
  // 批量删除
  const handleBatchDelete = () => {
    if (selectedIds.value.length === 0) return
    
    ElMessageBox.confirm(`是否确认删除 ${selectedIds.value.length} 个评论?`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await deleteCommentApi(selectedIds.value)
        ElMessage.success('批量删除成功')
        getList()
        selectedIds.value = []
      } catch (error) {
      }
    })
  }
  
  // 删除
  const handleDelete = (row: any) => {
    ElMessageBox.confirm(`是否确认删除 ${row.nickname} 的评论?`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await deleteCommentApi(row.id)
        ElMessage.success('删除成功')
        getList()
      } catch (error) {
      }
    })
  }

  // 搜索
  const handleQuery = () => {
    queryParams.pageNum = 1
    getList()
  }

  // 重置搜索
  const resetQuery = () => {
    queryFormRef.value?.resetFields()
    queryParams.articleId = undefined
    queryParams.articleTitle = ''
    queryParams.nickname = ''
    queryParams.content = ''
    articleTitle.value = ''
    handleQuery()
  }

  // 清除文章筛选
  const clearArticleFilter = () => {
    queryParams.articleId = undefined
    articleTitle.value = ''
    queryParams.articleTitle = ''
    handleQuery()
  }

// 分页大小改变
  const handleSizeChange = (val: number) => {
    queryParams.pageSize = val
    getList()
  }
  
  // 页码改变
  const handleCurrentChange = (val: number) => {
    queryParams.pageNum = val
    getList()
  }
  
  // 初始化
  onMounted(() => {
    const { articleId: id, articleTitle: title } = route.query
    if (id) {
      queryParams.articleId = Number(id)
      articleTitle.value = title as string
    }
    getList()
  })

  // 显示编辑对话框
  const openEditDialog = ref(false)
  const editFormRef = ref<FormInstance>()
  const editForm = reactive({
    id: undefined as number | undefined,
    content: '',
    replyUserId: undefined as number | undefined,
    isStick: 0,
    likeCount: 0
  })

  // 表单校验规则
  const editRules = reactive({
    content: [
      { required: true, message: '评论内容不能为空', trigger: 'blur' }
    ]
  })

  // 编辑按钮操作
  const handleEdit = (row: any) => {
    editForm.id = row.id
    editForm.content = row.content || ''
    editForm.replyUserId = row.replyUserId
    editForm.isStick = row.isStick || 0
    editForm.likeCount = row.likeCount ?? 0
    openEditDialog.value = true
  }

  // 提交编辑
  const submitEdit = async () => {
    if (!editFormRef.value) return
    await editFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          await editCommentApi(editForm)
          ElMessage.success('编辑成功')
          openEditDialog.value = false
          getList()
        } catch (error) {
          ElMessage.error('编辑失败')
        }
      }
    })
  }

  // 取消编辑
  const cancelEdit = () => {
    openEditDialog.value = false
    editForm.id = undefined
    editForm.content = ''
    editForm.replyUserId = undefined
    editForm.isStick = 0
    editForm.likeCount = 0
    editFormRef.value?.clearValidate()
  }
  </script>
  
 