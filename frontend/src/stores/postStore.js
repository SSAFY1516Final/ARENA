import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { postApi } from '@/api/postApi'

function mapPost(post) {
  return {
    ...post,
    body: post.summaryCard || '',
    voteA: post.voteCountA || 0,
    voteB: post.voteCountB || 0,
    userVoteChoice: null,
  }
}

function mapComment(comment) {
  return {
    ...comment,
    author: comment.authorNickname,
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
        userVoteChoice: userVotes.value[post.postId] || null,
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
      userVoteChoice: userVotes.value[post.postId] || null,
      summary: data.summary,
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
  }
})
