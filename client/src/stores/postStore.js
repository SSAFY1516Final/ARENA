import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { postApi } from '@/api/postApi'

function normalizeShareBody(value) {
  const body = value?.trim() || ''
  const sectionMatch = body.match(/(?:^|\n)본문\s*\n([\s\S]*)$/)
  return sectionMatch ? sectionMatch[1].trim() : body
}

function mapPost(post) {
  const shareBody = normalizeShareBody(post.shareBody || post.body || '')
  return {
    ...post,
    body: shareBody || post.summaryCard || '',
    shareBody,
    voteA: post.voteCountA || 0,
    voteB: post.voteCountB || 0,
    userVoteChoice: post.userVoteChoice || null,
    isOwner: Boolean(post.isOwner ?? post.owner),
  }
}

function mapComment(comment) {
  return {
    ...comment,
    author: comment.authorNickname,
    isOwner: Boolean(comment.isOwner ?? comment.owner),
  }
}

function mapMessage(message) {
  return {
    ...message,
    messageId: message.messageId || message.id,
  }
}

export const usePostStore = defineStore('post', () => {
  const posts = ref([])
  const postDetail = ref(null)
  const userVotes = ref({})
  const searchCondition = ref({
    keyword: '',
    mode: 'ALL',
    sort: 'latest',
  })
  const pagination = ref({
    page: 1,
    size: 10,
    total: 0,
  })
  const loading = ref(false)

  const hasPosts = computed(() => posts.value.length > 0)

  async function fetchPosts(params = {}) {
    loading.value = true
    try {
      searchCondition.value = {
        ...searchCondition.value,
        ...params,
      }
      const requestParams = {
        ...searchCondition.value,
        mode: searchCondition.value.mode === 'ALL' ? undefined : searchCondition.value.mode,
      }
      const { data } = await postApi.list(requestParams)
      posts.value = data.items.map((post) => ({
        ...mapPost(post),
        userVoteChoice: post.userVoteChoice || userVotes.value[post.postId] || null,
      }))
      pagination.value = {
        page: data.page,
        size: data.size,
        total: data.totalCount,
      }
      return posts.value
    } finally {
      loading.value = false
    }
  }

  async function fetchPost(postId) {
    const { data } = await postApi.detail(postId)
    const post = mapPost(data.post)
    postDetail.value = {
      ...post,
      userVoteChoice: post.userVoteChoice || userVotes.value[post.postId] || null,
      summary: data.summary,
      round: data.round || null,
      messages: (data.messages || []).map(mapMessage),
      comments: (data.comments || []).map(mapComment),
    }
    return postDetail.value
  }

  async function vote(postId, payload) {
    const { data } = await postApi.vote(postId, payload)

    userVotes.value = {
      ...userVotes.value,
      [postId]: payload.choice,
    }

    const votePatch = {
      voteOptionA: data.voteOptionA,
      voteOptionB: data.voteOptionB,
      voteA: data.voteCountA,
      voteB: data.voteCountB,
      voteRatioA: data.voteRatioA,
      voteRatioB: data.voteRatioB,
      userVoteChoice: payload.choice,
    }

    posts.value = posts.value.map((item) =>
      String(item.postId) === String(postId) ? { ...item, ...votePatch } : item,
    )
    if (String(postDetail.value?.postId) === String(postId)) {
      postDetail.value = {
        ...postDetail.value,
        ...votePatch,
      }
    }

    return {
      postId: data.postId,
      choice: payload.choice,
      alreadyVoted: false,
      voteA: data.voteCountA,
      voteB: data.voteCountB,
    }
  }

  async function createSharedPost(payload) {
    await fetchPosts(searchCondition.value)
    return payload
  }

  async function deletePost(postId) {
    await postApi.remove(postId)
    posts.value = posts.value.filter((post) => String(post.postId) !== String(postId))
    if (String(postDetail.value?.postId) === String(postId)) {
      postDetail.value = null
    }
  }

  function addCommentToDetail(comment) {
    if (!postDetail.value) return
    if (postDetail.value.comments.some((item) => String(item.commentId) === String(comment.commentId))) return
    postDetail.value = {
      ...postDetail.value,
      comments: [
        ...postDetail.value.comments,
        mapComment(comment),
      ],
    }
  }

  function removeCommentFromDetail(commentId) {
    if (!postDetail.value) return
    postDetail.value = {
      ...postDetail.value,
      comments: postDetail.value.comments.filter((comment) => String(comment.commentId) !== String(commentId)),
    }
  }

  return {
    posts,
    postDetail,
    userVotes,
    searchCondition,
    pagination,
    loading,
    hasPosts,
    fetchPosts,
    fetchPost,
    vote,
    createSharedPost,
    deletePost,
    addCommentToDetail,
    removeCommentFromDetail,
  }
})
