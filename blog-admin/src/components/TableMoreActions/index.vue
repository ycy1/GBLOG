<template>
  <div class="table-more-actions">
    <!-- 过滤掉被禁用的按钮 -->
    <template v-if="filteredActions.length <= maxVisible">
      <!-- 所有可见按钮直接显示 -->
      <el-button
        v-for="action in filteredActions"
        :key="action.label"
        :type="action.type || 'primary'"
        link
        :icon="action.icon"
        @click="handleCommand(action)"
      >
        {{ action.label }}
      </el-button>
    </template>

    <!-- 如果按钮数量超过限制，分组显示 -->
    <template v-else>
      <!-- 前 (maxVisible - 1) 个可见按钮直接显示 -->
      <el-button
        v-for="action in visibleFiltered"
        :key="action.label"
        :type="action.type || 'primary'"
        link
        :icon="action.icon"
        @click="handleCommand(action)"
      >
        {{ action.label }}
      </el-button>

      <!-- 更多下拉菜单 -->
      <el-dropdown trigger="click" @command="handleDropdownCommand">
        <el-button type="primary" link>
          更多
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="action in hiddenFiltered"
              :key="action.label"
              :command="action"
            >
              <el-icon v-if="action.icon" class="dropdown-icon">
                <component :is="action.icon" />
              </el-icon>
              {{ action.label }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown } from '@element-plus/icons-vue'

// 定义 props
interface ActionItem {
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  icon?: string
  disabled?: boolean
  command?: any
  [key: string]: any
}

const props = withDefaults(defineProps<{
  actions: ActionItem[]
  maxVisible?: number // 最大可见按钮数，默认为 3
}>(), {
  maxVisible: 3
})

// emit 定义
const emit = defineEmits<{
  command: [action: ActionItem]
}>()

// 过滤掉被禁用的按钮
const filteredActions = computed<ActionItem[]>(() =>
  props.actions.filter(action => !action.disabled)
)

// 计算可见和隐藏的按钮（基于过滤后的）
const visibleCount = computed(() => Math.max(1, props.maxVisible - 1))
const visibleFiltered = computed<ActionItem[]>(() => filteredActions.value.slice(0, visibleCount.value))
const hiddenFiltered = computed<ActionItem[]>(() => filteredActions.value.slice(visibleCount.value))

// 点击按钮事件 - 统一使用 command 事件
const handleCommand = (action: ActionItem) => {
  emit('command', action)
}

// 下拉菜单命令事件
const handleDropdownCommand = (action: ActionItem) => {
  emit('command', action)
}
</script>

<style lang="scss" scoped>
.table-more-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;

  .el-button {
    padding: 0;
  }

  :deep(.el-dropdown) {
    color: var(--el-color-primary);
    cursor: pointer;
  }
}

.dropdown-icon {
  margin-right: 5px;
  font-size: 14px;
}
</style>
