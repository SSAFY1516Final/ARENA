<template>
  <main v-if="post" class="page detail-layout">
    <article class="article-card">
      <div class="detail-topbar">
        <button class="debate-action-button debate-action-button--secondary detail-back-button" type="button" @click="goBack">
          뒤로가기
        </button>
        <button
          v-if="isPostOwner"
          class="debate-action-button debate-action-button--secondary post-delete-button"
          type="button"
          @click="deletePost"
        >
          삭제
        </button>
      </div>
      <h1>{{ post.title }}</h1>

      <section v-if="post.shareBody" class="post-share-body-box">
        <p>{{ post.shareBody }}</p>
      </section>

      <section class="log-box">
        <h2>공유 라운드 토론 로그</h2>
        <div v-if="postRoundTitle || postRoundDescription" class="post-log-context">
          <span class="section-label">세부주제</span>
          <strong>{{ postRoundTitle }}</strong>
          <p v-if="postRoundDescription">{{ postRoundDescription }}</p>
        </div>
        <DebateMessage
          v-for="(message, index) in post.messages"
          :key="message.messageId"
          :message="message"
          :side-labels="postSideLabels"
          :display-index="index + 1"
        />
        <div v-if="postSummaryText" class="post-log-summary">
          <span class="section-label">요약</span>
          <p>{{ postSummaryText }}</p>
        </div>
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
        <div v-if="canParticipate" class="vote-choice-grid">
          <button
            class="vote-choice"
            :class="{ selected: post.userVoteChoice === 'A' }"
            type="button"
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
        <div v-else class="post-login-prompt">
          <p>로그인 후 투표할 수 있습니다.</p>
          <button class="debate-action-button debate-action-button--primary vote-login-button" type="button" @click="goLogin">
            로그인하기
          </button>
        </div>
      </section>
    </article>

    <aside class="comment-card">
      <h2>댓글</h2>
      <div v-for="comment in visibleComments" :key="comment.commentId" class="comment">
        <div class="comment__head">
          <strong>{{ comment.author }}</strong>
          <button
            v-if="comment.isOwner"
            class="comment-delete-button"
            type="button"
            @click="deleteComment(comment.commentId)"
          >
            삭제
          </button>
        </div>
        <p>{{ comment.content }}</p>
      </div>
      <form v-if="canParticipate" class="comment-form" @submit.prevent="addComment">
        <textarea
          v-model="commentContent"
          class="textarea"
          placeholder="댓글을 입력하세요"
          required
          @keyup.enter.exact.prevent="addComment"
        ></textarea>
        <button class="button button--full" type="submit" :disabled="isAddingComment">등록</button>
      </form>
      <div v-else class="post-login-prompt post-login-prompt--comment">
        <p>로그인 후 댓글을 작성할 수 있습니다.</p>
        <button class="debate-action-button debate-action-button--primary comment-login-button" type="button" @click="goLogin">
          로그인하기
        </button>
      </div>
    </aside>
  </main>

  <main v-else class="page">
    <section class="loading-state">
      게시글을 불러오는 중입니다.
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DebateMessage from '@/components/debate/DebateMessage.vue'
import { useAuthStore } from '@/stores/authStore'
import { useCommentStore } from '@/stores/commentStore'
import { usePostStore } from '@/stores/postStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const postStore = usePostStore()
const commentStore = useCommentStore()
const commentContent = ref('')
const isAddingComment = ref(false)

const post = computed(() => postStore.postDetail)
const isPostOwner = computed(() => Boolean(post.value?.isOwner || post.value?.owner))
const canParticipate = computed(() => authStore.isAuthenticated)
const totalVotes = computed(() => (post.value ? post.value.voteA + post.value.voteB : 0))
const percentA = computed(() => (totalVotes.value ? Math.round((post.value.voteA / totalVotes.value) * 100) : 0))
const percentB = computed(() => (totalVotes.value ? 100 - percentA.value : 0))
const selectedVoteLabel = computed(() => {
  if (!post.value?.userVoteChoice) return ''
  return post.value.userVoteChoice === 'A' ? post.value.voteOptionA : post.value.voteOptionB
})
const postSideLabels = computed(() => ({
  COOL_HEADED: post.value?.voteOptionA || 'A 진영',
  PASSIONATE: post.value?.voteOptionB || 'B 진영',
}))
const postRound = computed(() => post.value?.round || null)
const postRoundTitle = computed(() => postRound.value?.title || '')
const postRoundDescription = computed(() => {
  if (!postRound.value) return ''
  if (postRound.value.topic && postRound.value.topic !== postRound.value.title) {
    return postRound.value.topic
  }
  return postRound.value.description || ''
})
const postSummaryText = computed(() => (
  postRound.value?.summary?.summaryText ||
  post.value?.summary?.summaryText ||
  post.value?.summaryCard ||
  ''
))
const visibleComments = computed(() => {
  if (!post.value) return []
  return post.value.comments
})

onMounted(() => {
  authStore.fetchMe()
  postStore.fetchPost(route.params.postId)
})

async function vote(choice) {
  if (!canParticipate.value) {
    goLogin()
    return
  }
  await postStore.vote(post.value.postId, { choice })
}

async function addComment() {
  if (isAddingComment.value) return
  if (!canParticipate.value) {
    goLogin()
    return
  }
  const content = commentContent.value.trim()
  if (!content) return
  isAddingComment.value = true
  try {
    const comment = await commentStore.createComment(post.value.postId, { content })
    postStore.addCommentToDetail({ ...comment, isOwner: true })
    commentContent.value = ''
  } finally {
    isAddingComment.value = false
  }
}

async function deleteComment(commentId) {
  await commentStore.deleteComment(commentId)
  postStore.removeCommentFromDetail(commentId)
}

async function deletePost() {
  await postStore.deletePost(post.value.postId)
  router.replace('/posts')
}

function goBack() {
  router.push('/posts')
}

function goLogin() {
  router.push('/auth')
}
</script>
