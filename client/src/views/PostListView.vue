<template>
  <main class="page">
    <section class="list-header">
      <div>
        <span class="eyebrow">Community Board</span>
        <h1>공유된 토론</h1>
        <p class="page-copy">토론 결과가 커뮤니티로 이어집니다. 공유된 판단 기준과 투표 흐름을 확인하세요.</p>
      </div>
      <RouterLink class="button" to="/new">새 토론</RouterLink>
    </section>

    <section class="toolbar" aria-label="게시글 검색과 필터">
      <input v-model="filters.keyword" class="input" placeholder="검색어를 입력하세요" @keyup.enter="search" />
      <select v-model="filters.mode" class="select" @change="search">
        <option value="ALL">전체</option>
        <option value="PRACTICAL">토론</option>
      </select>
      <select v-model="filters.sort" class="select" @change="search">
        <option value="latest">최신순</option>
        <option value="comments">댓글순</option>
        <option value="votes">투표순</option>
      </select>
      <button class="button button--ghost" type="button" @click="search">검색</button>
    </section>

    <section v-if="postStore.loading" class="loading-state">
      공유된 토론을 불러오는 중입니다.
    </section>

    <section v-else-if="postStore.hasPosts" class="post-grid">
      <RouterLink
        v-for="post in postStore.posts"
        :key="post.postId"
        class="post-card"
        :to="`/posts/${post.postId}`"
      >
        <div class="tag-row">
          <span class="tag">댓글 {{ post.commentCount }}</span>
        </div>
        <h2>{{ post.title }}</h2>
        <p>{{ post.body }}</p>
        <div class="post-card__meta">
          <span>{{ post.voteOptionA }} {{ votePercent(post, 'A') }}%</span>
          <span>{{ post.voteOptionB }} {{ votePercent(post, 'B') }}%</span>
        </div>
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

function votePercent(post, side) {
  const total = Number(post.voteA || 0) + Number(post.voteB || 0)
  if (!total) return 0
  if (side === 'A') return Math.round((Number(post.voteA || 0) / total) * 100)
  return Math.round((Number(post.voteB || 0) / total) * 100)
}
</script>
