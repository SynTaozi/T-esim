<template>
  <div>
    <PageHeader title="教程管理" subtitle="分类与教程内容" />
    <el-tabs v-model="tab">
      <el-tab-pane label="分类" name="cat">
        <div class="page-card">
          <div class="table-toolbar"><el-button type="primary" @click="editCat()">新建分类</el-button></div>
          <el-table :data="categories" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="nameZh" label="中文名" />
            <el-table-column prop="nameEn" label="英文名" />
            <el-table-column label="操作" width="160"><template #default="{row}"><el-button link type="primary" @click="editCat(row)">编辑</el-button><el-button link type="danger" @click="removeCat(row.id)">删除</el-button></template></el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="教程" name="item">
        <div class="page-card">
          <div class="table-toolbar"><el-button type="primary" @click="editItem()">新建教程</el-button></div>
          <el-table :data="items" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="titleZh" label="标题" min-width="180" />
            <el-table-column prop="categoryId" label="分类" width="80" />
            <el-table-column prop="contentType" label="类型" width="100" />
            <el-table-column label="操作" width="160"><template #default="{row}"><el-button link type="primary" @click="editItem(row)">编辑</el-button><el-button link type="danger" @click="removeItem(row.id)">删除</el-button></template></el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="catVisible" title="教程分类" width="480px">
      <el-form :model="catForm" label-width="80px"><el-form-item label="中文名"><el-input v-model="catForm.nameZh"/></el-form-item><el-form-item label="英文名"><el-input v-model="catForm.nameEn"/></el-form-item><el-form-item label="排序"><el-input-number v-model="catForm.sortOrder" :min="0"/></el-form-item></el-form>
      <template #footer><el-button @click="catVisible=false">取消</el-button><el-button type="primary" @click="saveCat">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="itemVisible" title="教程" width="720px">
      <el-form :model="itemForm" label-width="90px">
        <el-form-item label="分类"><el-select v-model="itemForm.categoryId" style="width:100%"><el-option v-for="c in categories" :key="c.id" :label="c.nameZh" :value="c.id"/></el-select></el-form-item>
        <el-form-item label="标题中文"><el-input v-model="itemForm.titleZh"/></el-form-item>
        <el-form-item label="标题英文"><el-input v-model="itemForm.titleEn"/></el-form-item>
        <el-form-item label="类型"><el-select v-model="itemForm.contentType" style="width:100%"><el-option label="ARTICLE" value="ARTICLE"/><el-option label="VIDEO" value="VIDEO"/></el-select></el-form-item>
        <el-form-item label="内容中文"><el-input v-model="itemForm.contentZh" type="textarea" :rows="5"/></el-form-item>
        <el-form-item label="内容英文"><el-input v-model="itemForm.contentEn" type="textarea" :rows="5"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="itemVisible=false">取消</el-button><el-button type="primary" @click="saveItem">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { tutorialApi } from '@/api/cms'
const tab=ref('item'); const categories=ref<any[]>([]); const items=ref<any[]>([])
const catVisible=ref(false); const itemVisible=ref(false); const catForm=reactive<any>({}); const itemForm=reactive<any>({})
async function load(){categories.value=(await tutorialApi.listCategories()).data||[]; items.value=(await tutorialApi.list()).data||[]}
function editCat(row?:any){Object.assign(catForm,row||{nameZh:'',nameEn:'',sortOrder:0});catVisible.value=true}
async function saveCat(){await tutorialApi.saveCategory(catForm);ElMessage.success('已保存');catVisible.value=false;load()}
async function removeCat(id:number){await ElMessageBox.confirm('确认删除？');await tutorialApi.removeCategory(id);load()}
function editItem(row?:any){Object.assign(itemForm,row||{categoryId:categories.value[0]?.id,titleZh:'',titleEn:'',contentType:'ARTICLE',contentZh:'',contentEn:'',sortOrder:0});itemVisible.value=true}
async function saveItem(){await tutorialApi.save(itemForm);ElMessage.success('已保存');itemVisible.value=false;load()}
async function removeItem(id:number){await ElMessageBox.confirm('确认删除？');await tutorialApi.remove(id);load()}
onMounted(load)
</script>
