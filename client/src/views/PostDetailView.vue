<template>
  <main v-if="post" class="page detail-layout">
    <article class="article-card">
      <div class="tag-row">
        <span class="tag" :class="post.mode === 'PRACTICAL' ? 'tag--teal' : 'tag--amber'">
          {{ post.mode === 'PRACTICAL' ? '실용 판정' : '예능 배틀' }}
        </span>
        <span class="tag tag--blue">SHARED</span>
      </div>
      <h1>{{ post.title }}</h1>
      <p class="page-copy">{{ post.body }}</p>

      <section class="log-box">
        <h2>전체 토론 로그</h2>
        <DebateMessage
          v-for="message in post.messages"
          :key="message.messageId"
          :message="message"
        />
      </section>

      <section class="vote-section vote-section--below-log" aria-labelledby="user-vote-title">
        <div class="vote-section-header">
          <div>
            <h2 id="user-vote-title" class="vote-section-title">사용자 투표</h2>
            <p class="vote-section-copy">
              {{ post.userVoteChoice ? '투표 결과' : '투표하고 결과 보기' }}
            </p>
          </div>
          <span v-if="selectedVoteLabel" class="vote-selected-label">내 선택: {{ selectedVoteLabel }}</span>
        </div>
        <div class="vote-choice-grid">
          <button
            class="vote-choice"
            :class="{ selected: post.userVoteChoice === 'A' }"
            type="button"
            :disabled="Boolean(post.userVoteChoice)"
            @click="vote('A')"
          >
            <strong>{{ post.voteOptionA }}</strong>
            <span v-if="post.userVoteChoice" class="vote-percent">{{ percentA }}%</span>
            <span v-else class="vote-ready">선택하기</span>
            <div v-if="post.userVoteChoice" class="vote-result-bar" aria-hidden="true">
              <i :style="{ width: `${percentA}%` }"></i>
            </div>
            <em v-if="post.userVoteChoice === 'A'">내 선택</em>
          </button>
          <button
            class="vote-choice vote-choice--amber"
            :class="{ selected: post.userVoteChoice === 'B' }"
            type="button"
            :disabled="Boolean(post.userVoteChoice)"
            @click="vote('B')"
          >
            <strong>{{ post.voteOptionB }}</strong>
            <span v-if="post.userVoteChoice" class="vote-percent">{{ percentB }}%</span>
            <span v-else class="vote-ready">선택하기</span>
            <div v-if="post.userVoteChoice" class="vote-result-bar vote-result-bar--amber" aria-hidden="true">
              <i :style="{ width: `${percentB}%` }"></i>
            </div>
            <em v-if="post.userVoteChoice === 'B'">내 선택</em>
          </button>
        </div>
      </section>
    </article>

    <aside class="comment-card">
      <h2>댓글</h2>
      <div v-for="comment in visibleComments" :key="comment.commentId" class="comment">
        <strong>{{ comment.author }}</strong>
        <p>{{ comment.content }}</p>
      </div>
      <form class="comment-form" @submit.prevent="addComment">
        <textarea v-model="commentContent" class="textarea" placeholder="댓글을 입력하세요" required></textarea>
        <button class="button button--full" type="submit">등록</button>
      </form>
    </aside>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import DebateMessage from '@/components/debate/DebateMessage.vue'
import { useCommentStore } from '@/stores/commentStore'
import { usePostStore } from '@/stores/postStore'

const route = useRoute()
const postStore = usePostStore()
const commentStore = useCommentStore()
const commentContent = ref('')

const post = computed(() => postStore.postDetail)
const totalVotes = computed(() => (post.value ? post.value.voteA + post.value.voteB : 0))
const percentA = computed(() => (totalVotes.value ? Math.round((post.value.voteA / totalVotes.value) * 100) : 0))
const percentB = computed(() => (totalVotes.value ? 100 - percentA.value : 0))
const selectedVoteLabel = computed(() => {
  if (!post.value?.userVoteChoice) return ''
  return post.value.userVoteChoice === 'A' ? post.value.voteOptionA : post.value.voteOptionB
})
const visibleComments = computed(() => {
  if (!post.value) return []
  const localComments = commentStore.comments.filter((comment) => comment.postId === post.value.postId)
  return [...post.value.comments, ...localComments.filter((comment) => !post.value.comments.some((item) => item.commentId === comment.commentId))]
})

onMounted(() => {
  postStore.fetchPost(route.params.postId)
})

async function vote(choice) {
  await postStore.vote(post.value.postId, { choice })
}

async function addComment() {
  await commentStore.createComment(post.value.postId, { content: commentContent.value })
  commentContent.value = ''
}
</script>
