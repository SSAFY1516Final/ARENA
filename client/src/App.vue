<template>
  <NConfigProvider :theme-overrides="naiveThemeOverrides">
    <NLoadingBarProvider>
      <NDialogProvider>
        <NMessageProvider>
          <AppNav v-if="!authRouteNames.includes(route.name)" />
          <RouterView />
        </NMessageProvider>
      </NDialogProvider>
    </NLoadingBarProvider>
  </NConfigProvider>
</template>

<script setup>
import { onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { NConfigProvider, NDialogProvider, NLoadingBarProvider, NMessageProvider } from 'naive-ui'
import AppNav from '@/components/common/AppNav.vue'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const auth = useAuthStore()
const authRouteNames = ['auth', 'kakao-callback']
const naiveThemeOverrides = {
  common: {
    primaryColor: '#256fdb',
    primaryColorHover: '#1f5fc2',
    primaryColorPressed: '#174ea6',
    primaryColorSuppl: '#256fdb',
    borderRadius: '8px',
    borderRadiusSmall: '6px',
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", "Apple SD Gothic Neo", "Noto Sans KR", sans-serif',
  },
  Card: {
    borderRadius: '8px',
    paddingMedium: '20px',
  },
  Button: {
    borderRadiusMedium: '8px',
    fontWeight: '800',
  },
  Tag: {
    borderRadius: '999px',
    fontWeightStrong: '800',
  },
}

onMounted(() => {
  auth.fetchMe()
})
</script>
