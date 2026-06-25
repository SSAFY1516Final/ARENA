import { describe, expect, it } from 'vitest'
import router from '@/router'

describe('router', () => {
  it('registers the MVP screen routes with auth-first entry', () => {
    const paths = router.getRoutes().map((route) => route.path)

    expect(paths).toContain('/')
    expect(paths).toContain('/new')
    expect(paths).toContain('/auth')
    expect(paths).toContain('/debates')
    expect(paths).toContain('/debates/:debateId')
    expect(paths).toContain('/debates/:debateId/summary')
    expect(paths).toContain('/posts')
    expect(paths).toContain('/posts/:postId')
  })

  it('redirects the first entry route to the login screen', () => {
    const rootRoute = router.getRoutes().find((route) => route.path === '/')

    expect(rootRoute.redirect).toBe('/auth')
  })
})
