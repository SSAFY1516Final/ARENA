import { ref } from 'vue'
import { defineStore } from 'pinia'
import { commentApi } from '@/api/commentApi'

function mapComment(comment) {
  return {
    ...comment,
    author: comment.authorNickname,
  }
}

export const useCommentStore = defineStore('comment', () => {
  const comments = ref([])
  const loading = ref(false)

  async function createComment(postId, payload) {
    loading.value = true
    try {
      const { data } = await commentApi.create(postId, payload)
      const comment = mapComment(data)
      comments.value.push(comment)
      return comment
    } finally {
      loading.value = false
    }
  }

  async function deleteComment(commentId) {
    await commentApi.remove(commentId)
    comments.value = comments.value.filter((comment) => String(comment.commentId) !== String(commentId))
  }

  return {
    comments,
    loading,
    createComment,
    deleteComment,
  }
})
