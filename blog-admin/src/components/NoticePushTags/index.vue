<template>
  <div class="npt-tags">
    <template v-if="total > 0">
      <div class="npt-tags-group">
        <span class="npt-tags-label">用户</span>
        <el-tag v-for="u in display.user" :key="u.id" size="small" class="npt-tags-tag">{{ u.name }}</el-tag>
        <span v-if="!display.user.length" class="npt-tags-zero">（0）</span>
      </div>
      <div class="npt-tags-group">
        <span class="npt-tags-label">部门</span>
        <el-tag v-for="d in display.dept" :key="d.id" size="small" class="npt-tags-tag">{{ d.name }}</el-tag>
        <span v-if="!display.dept.length" class="npt-tags-zero">（0）</span>
      </div>
      <div class="npt-tags-group">
        <span class="npt-tags-label">角色</span>
        <el-tag v-for="r in display.role" :key="r.id" size="small" class="npt-tags-tag">{{ r.name }}</el-tag>
        <span v-if="!display.role.length" class="npt-tags-zero">（0）</span>
      </div>
    </template>
    <span v-else class="npt-tags-empty">全员可见</span>
  </div>
</template>

<script setup lang="ts">
import { getUserDetailApi } from '@/api/system/user'
import { getDeptTreeApi } from '@/api/system/dept'
import { getAllRoleList } from '@/api/system/role'

const props = defineProps<{ modelValue?: string | null }>()

// ---- 模块级共享数据：列表多行共用，部门/角色只请求一次，用户详情按 id 去重 ----
let deptTreeData: any[] | null = null
let deptLoading: Promise<any[]> | null = null
const deptNames: Record<number, string> = {}
let roleData: any[] | null = null
let roleLoading: Promise<any[]> | null = null
const roleNames: Record<number, string> = {}
const userNames: Record<number, string> = {}
const userLoading: Record<number, Promise<string | undefined> | undefined> = {}

const loadDept = (): Promise<any[]> => {
  if (deptTreeData) return Promise.resolve(deptTreeData)
  if (!deptLoading) {
    deptLoading = getDeptTreeApi()
      .then(({ data }) => {
        deptTreeData = data || []
        const walk = (nodes: any[]) => {
          nodes.forEach(n => {
            deptNames[Number(n.id)] = n.name || String(n.id)
            if (n.children?.length) walk(n.children)
          })
        }
        walk(deptTreeData)
        return deptTreeData
      })
      .catch(() => deptTreeData || [])
  }
  return deptLoading
}

const loadRoles = (): Promise<any[]> => {
  if (roleData) return Promise.resolve(roleData)
  if (!roleLoading) {
    roleLoading = getAllRoleList()
      .then(({ data }) => {
        roleData = data || []
        roleData.forEach(r => {
          roleNames[Number(r.id)] = r.name || String(r.id)
        })
        return roleData
      })
      .catch(() => roleData || [])
  }
  return roleLoading
}

const fetchUserName = (id: number): Promise<string | undefined> => {
  if (userNames[id]) return Promise.resolve(userNames[id])
  if (!userLoading[id]) {
    userLoading[id] = getUserDetailApi(id)
      .then(({ data }) => {
        const name = data?.nickname || data?.username || String(id)
        userNames[id] = name
        return name
      })
      .catch(() => {
        userNames[id] = String(id)
        return undefined
      })
  }
  return userLoading[id]!
}

const display = reactive<{
  user: { id: number; name: string }[]
  dept: { id: number; name: string }[]
  role: { id: number; name: string }[]
}>({ user: [], dept: [], role: [] })

const total = computed(() => display.user.length + display.dept.length + display.role.length)

const resolve = (val: string | null | undefined) => {
  let obj: any = { user: [], dept: [], role: [] }
  if (typeof val === 'string' && val.trim()) {
    try {
      obj = JSON.parse(val)
    } catch (e) {
      obj = { user: [], dept: [], role: [] }
    }
  }
  display.user = (obj.user || []).map((id: any) => {
    const n = Number(id)
    return { id: n, name: userNames[n] ?? String(n) }
  })
  display.dept = (obj.dept || []).map((id: any) => {
    const n = Number(id)
    return { id: n, name: deptNames[n] ?? String(n) }
  })
  display.role = (obj.role || []).map((id: any) => {
    const n = Number(id)
    return { id: n, name: roleNames[n] ?? String(n) }
  })
  // 异步补全名字（共享缓存，不重复请求）
  display.user.forEach(u => {
    if (!userNames[u.id]) {
      fetchUserName(u.id).then(name => {
        if (name) u.name = name
      })
    }
  })
  Promise.all([loadDept(), loadRoles()]).then(() => {
    display.dept.forEach(d => {
      if (deptNames[d.id]) d.name = deptNames[d.id]
    })
    display.role.forEach(r => {
      if (roleNames[r.id]) r.name = roleNames[r.id]
    })
  })
}

watch(() => props.modelValue, (val) => resolve(val), { immediate: true })
</script>

<style scoped lang="scss">
.npt-tags {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .npt-tags-group {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px;
    line-height: 20px;

    .npt-tags-label {
      font-size: 12px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      flex-shrink: 0;
    }

    .npt-tags-zero {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
    }
  }

  .npt-tags-empty {
    color: var(--el-text-color-placeholder);
    font-size: 12px;
  }
}
</style>
