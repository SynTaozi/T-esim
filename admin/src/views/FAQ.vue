<template>
  <div>
    <PageHeader title="常见问题" subtitle="分类与问答维护" />
    <el-tabs v-model="tab">
      <el-tab-pane label="分类" name="cat">
        <div class="page-card">
          <div class="table-toolbar"><el-button type="primary" @click="editCat()">新建分类</el-button></div>
          <el-table :data="categories" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="nameZh" label="中文名" />
            <el-table-column prop="nameEn" label="英文名" />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="160">
              <template #default="{row}"><el-button link type="primary" @click="editCat(row)">编辑</el-button><el-button link type="danger" @click="removeCat(row.id)">删除</el-button></template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="问题" name="faq">
        <div class="page-card">
          <div class="table-toolbar"><el-button type="primary" @click="editFaq()">新建问题</el-button></div>
          <el-table :data="faqs" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="categoryId" label="分类" width="80" />
            <el-table-column prop="questionZh" label="问题" min-width="180" />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="160">
              <template #default="{row}"><el-button link type="primary" @click="editFaq(row)">编辑</el-button><el-button link type="danger" @click="removeFaq(row.id)">删除</el-button></template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="catVisible" title="FAQ 分类" width="480px">
      <el-form :model="catForm" label-width="80px">
        <el-form-item label="中文名"><el-input v-model="catForm.nameZh" /></el-form-item>
        <el-form-item label="英文名"><el-input v-model="catForm.nameEn" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="catForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="catVisible=false">取消</el-button><el-button type="primary" @click="saveCat">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="faqVisible" title="FAQ 问题" width="640px">
      <el-form :model="faqForm" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="faqForm.categoryId" style="width:100%"><el-option v-for="c in categories" :key="c.id" :label="c.nameZh" :value="c.id" /></el-select>
        </el-form-item>
        <el-form-item label="问题中文"><el-input v-model="faqForm.questionZh" /></el-form-item>
        <el-form-item label="答案中文"><el-input v-model="faqForm.answerZh" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="问题英文"><el-input v-model="faqForm.questionEn" /></el-form-item>
        <el-form-item label="答案英文"><el-input v-model="faqForm.answerEn" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="faqForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="faqVisible=false">取消</el-button><el-button type="primary" @click="saveFaq">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { faqApi } from '@/api/cms'
const tab=ref('faq'); const categories=ref<any[]>([]); const faqs=ref<any[]>([])
const catVisible=ref(false); const faqVisible=ref(false); const catForm=reactive<any>({}); const faqForm=reactive<any>({})
async function load(){categories.value=(await faqApi.listCategories()).data||[]; faqs.value=(await faqApi.list()).data||[]}
function editCat(row?:any){Object.assign(catForm,row||{nameZh:'',nameEn:'',sortOrder:0});catVisible.value=true}
async function saveCat(){await faqApi.saveCategory(catForm);ElMessage.success('已保存');catVisible.value=false;load()}
async function removeCat(id:number){await ElMessageBox.confirm('确认删除？');await faqApi.removeCategory(id);load()}
function editFaq(row?:any){Object.assign(faqForm,row||{categoryId:categories.value[0]?.id,questionZh:'',answerZh:'',questionEn:'',answerEn:'',sortOrder:0});faqVisible.value=true}
async function saveFaq(){await faqApi.save(faqForm);ElMessage.success('已保存');faqVisible.value=false;load()}
async function removeFaq(id:number){await ElMessageBox.confirm('确认删除？');await faqApi.remove(id);load()}
onMounted(load)
</script>
