import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const routes = [
  {
    path: '/',
    redirect: '/auth',
  },
  {
    path: '/new',
    name: 'new-debate',
    component: () => import('@/views/HomeView.vue'),
  },
  {
    path: '/auth',
    name: 'auth',
    component: () => import('@/views/AuthView.vue'),
  },
  {
    path: '/auth/kakao/callback',
    name: 'kakao-callback',
    component: () => import('@/views/KakaoCallbackView.vue'),
  },
  {
    path: '/debates',
    name: 'my-debates',
    component: () => import('@/views/MyDebatesView.vue'),
  },
  {
    path: '/debates/:debateId',
    name: 'debate-room',
    component: () => import('@/views/DebateRoomView.vue'),
  },
  {
    path: '/debates/:debateId/summary',
    name: 'debate-summary',
    component: () => import('@/views/DebateSummaryView.vue'),
  },
  {
    path: '/posts',
    name: 'post-list',
    component: () => import('@/views/PostListView.vue'),
  },
  {
    path: '/posts/:postId',
    name: 'post-detail',
    component: () => import('@/views/PostDetailView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const publicRoutes = ['auth', 'kakao-callback', 'post-list', 'post-detail']
  if (publicRoutes.includes(to.name)) {
    return true
  }

  const auth = useAuthStore()
  auth.fetchMe()
  if (!auth.isAuthenticated) {
    return '/auth'
  }
  return true
})

export default router
