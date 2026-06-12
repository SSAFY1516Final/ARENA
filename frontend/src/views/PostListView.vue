<template>
  <main class="page">
    <section class="list-header">
      <div>
        <span class="eyebrow">Community Board</span>
        <h1>공유된 토론</h1>
        <p class="page-copy">공유자가 남긴 본문과 토론 주제를 보고 의견을 남길 게시글을 탐색합니다.</p>
      </div>
      <RouterLink class="button" to="/new">새 토론 만들기</RouterLink>
    </section>

    <section class="toolbar" aria-label="게시글 검색과 필터">
      <input v-model="filters.keyword" class="input" placeholder="검색어를 입력하세요" @keyup.enter="search" />
      <select v-model="filters.mode" class="select" @change="search">
        <option value="ALL">전체</option>
        <option value="PRACTICAL">실용 판정</option>
        <option value="ENTERTAINMENT">예능 배틀</option>
      </select>
      <select v-model="filters.sort" class="select" @change="search">
        <option value="latest">최신순</option>
        <option value="comments">댓글순</option>
        <option value="vote">투표순</option>
      </select>
      <button class="button button--ghost" type="button" @click="search">검색</button>
    </section>

    <section v-if="postStore.hasPosts" class="post-grid">
      <RouterLink
        v-for="post in postStore.posts"
        :key="post.postId"
        class="post-card"
        :to="`/posts/${post.postId}`"
      >
        <div class="tag-row">
          <span class="tag" :class="post.mode === 'PRACTICAL' ? 'tag--teal' : 'tag--amber'">
            {{ post.mode === 'PRACTICAL' ? '실용 판정' : '예능 배틀' }}
          </span>
          <span class="tag">댓글 {{ post.commentCount }}</span>
        </div>
        <h2>{{ post.title }}</h2>
        <p>{{ post.body }}</p>
      </RouterLink>
    </section>

    <section v-else class="empty-state">
      <h2>검색 결과가 없습니다</h2>
      <p>다른 키워드나 필터로 다시 찾아보세요.</p>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { RouterLink } from 'vue-router'
import { usePostStore } from '@/stores/postStore'

const postStore = usePostStore()
const filters = reactive({
  keyword: '',
  mode: 'ALL',
  sort: 'latest',
})

onMounted(() => {
  postStore.fetchPosts(filters)
})

function search() {
  postStore.fetchPosts(filters)
}
</script>
