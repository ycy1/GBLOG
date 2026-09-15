<template>
  <div class="app-container">

    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :model="queryParams" ref="queryFormRef" inline>
        <el-form-item label="规则名称" prop="title">
          <el-input v-model="queryParams.title" placeholder="请输入规则名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="收费类型" prop="payType">
          <el-select v-model="queryParams.payType" placeholder="全部" clearable @change="handleQuery">
            <el-option v-for="item in dict.options('article_pay_type')" :key="item.value" :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员免费" prop="vipFree">
          <el-select v-model="queryParams.vipFree" placeholder="全部" clearable @change="handleQuery">
            <el-option v-for="item in dict.options('sys_yes_no')" :key="item.value" :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
            <el-option v-for="item in dict.options('article_pay_rule_status')" :key="item.value" :label="item.label"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card class="box-card">
      <!-- 操作工具栏 -->
      <template #header>
        <div class="card-header">
          <ButtonGroup>
            <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
            <el-button type="danger" plain icon="Delete" :disabled="selectedIds.length === 0"
              @click="handleBatchDelete">批量删除</el-button>
          </ButtonGroup>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="dataList" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="规则名称" align="center" prop="title" show-overflow-tooltip />
        <el-table-column label="收费类型" align="center" width="180">
          <template #default="scope">
            <el-tag size="small">{{ dict.label('article_pay_type', scope.row.payType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文章售价" align="right" width="130">
          <template #default="scope">{{ validate.formatAmount(scope.row.price) }}</template>
        </el-table-column>
        <el-table-column label="会员免费" align="center" width="110">
          <template #default="scope">{{ dict.label('sys_yes_no', scope.row.vipFree) }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag size="small" :type="dict.tag('article_pay_rule_status', scope.row.status)">
              {{ dict.label('article_pay_rule_status', scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" width="140">
          <template #default="scope">{{ validate.formatTime(scope.row.createTime, 'YYYY-MM-DD') }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template #default="scope">
            <TableMoreActions :actions="[
              { label: '编辑', icon: 'Edit', command: { type: 'edit', row: scope.row } },
              { label: '删除', type: 'danger', icon: 'Delete', command: { type: 'delete', row: scope.row } }
            ]" @command="handleActionCommand" />
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页工具栏 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]" :total="total" background layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>

      <!-- 添加或修改对话框 -->
      <el-dialog v-model="open" :title="title" width="680px" append-to-body>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
          <el-form-item label="规则名称" prop="title">
            <el-input v-model="form.title" placeholder="请输入规则名称" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="收费类型" prop="payType">
            <el-select v-model="form.payType" placeholder="请选择收费类型" style="width: 100%">
              <el-option v-for="item in dict.options('article_pay_type')" :key="item.value" :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="文章售价" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" controls-position="right"
              style="width: 100%" />
            <div class="form-tip">单位：元；仅会员可读时填 0 即可</div>
          </el-form-item>
          <el-form-item label="会员是否免费" prop="vipFree">
            <el-radio-group v-model="form.vipFree">
              <el-radio v-for="item in dict.options('sys_yes_no')" :key="item.value" :value="item.value">
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio v-for="item in dict.options('article_pay_rule_status')" :key="item.value" :value="item.value">
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="cancel">取 消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitForm">确 定</el-button>
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  listPayRuleApi,
  detailPayRuleApi,
  deletePayRuleApi,
  addPayRuleApi,
  updatePayRuleApi
} from '@/api/article/payrule'
import { useDict } from '@/utils/dictCache'
import TableMoreActions from '@/components/TableMoreActions/index.vue'
import validate from '@/utils/validate'

/** 收费规则相关字典：收费类型 / 是否通用 / 状态 */
const dict = useDict('article_pay_type', 'sys_yes_no', 'article_pay_rule_status')

const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()

// 遮罩层
const loading = ref(false)
// 提交中
const submitting = ref(false)
// 选中数组
const selectedIds = ref<number[]>([])
// 总条数
const total = ref(0)
// 表格数据
const dataList = ref<any[]>([])
// 弹出层标题
const title = ref('')
// 是否显示弹出层
const open = ref(false)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: undefined as string | undefined,
  payType: undefined as number | undefined,
  vipFree: undefined as number | undefined,
  status: undefined as number | undefined
})

// 表单默认值（新增与重置共用，避免残留上一条记录的值）
const defaultForm = () => ({
  id: undefined as number | undefined,
  title: '',
  payType: dict.defaultValue('article_pay_type', 1),
  price: 0,
  vipFree: 0,
  status: dict.defaultValue('article_pay_rule_status', 1)
})

// 表单参数
const form = reactive<any>(defaultForm())

// 表单校验
const rules = {
  title: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  payType: [{ required: true, message: '请选择收费类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入文章售价', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const { data } = await listPayRuleApi(queryParams)
    dataList.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
  } finally {
    loading.value = false
  }
}

/** 表单重置 */
const reset = () => {
  Object.assign(form, defaultForm())
  formRef.value?.clearValidate()
}

/** 取消按钮 */
const cancel = () => {
  open.value = false
  reset()
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.title = undefined
  queryParams.payType = undefined
  queryParams.vipFree = undefined
  queryParams.status = undefined
  handleQuery()
}

/** 多选框选中数据 */
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

/** 行操作 */
const handleActionCommand = (action: any) => {
  const { type, row } = action.command
  if (type === 'edit') {
    handleUpdate(row)
  } else if (type === 'delete') {
    handleDelete(row)
  }
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  title.value = '新增收费标准'
  open.value = true
}

/** 修改按钮操作 */
const handleUpdate = (row: any) => {
  reset()
  detailPayRuleApi(row.id).then(({ data }) => {
    Object.assign(form, data)
    title.value = '修改收费标准'
    open.value = true
  })
}

/** 提交按钮 */
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 时间戳交给后端自动填充
  const payload: any = { ...form }
  delete payload.createTime
  delete payload.updateTime

  submitting.value = true
  try {
    if (form.id !== undefined && form.id !== null) {
      await updatePayRuleApi(payload)
      ElMessage.success('修改成功')
    } else {
      await addPayRuleApi(payload)
      ElMessage.success('新增成功')
    }
    open.value = false
    getList()
  } catch (error) {
  } finally {
    submitting.value = false
  }
}

/** 批量删除按钮操作 */
const handleBatchDelete = () => {
  if (!selectedIds.value.length) return
  ElMessageBox.confirm(`是否确认删除选中的 ${selectedIds.value.length} 条收费标准?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePayRuleApi(selectedIds.value)
      ElMessage.success('删除成功')
      selectedIds.value = []
      getList()
    } catch (error) {
    }
  }).catch(() => { })
}

/** 删除按钮操作 */
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`是否确认删除收费标准【${row.id}】?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePayRuleApi(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  }).catch(() => { })
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

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  margin-top: 4px;
}
</style>
