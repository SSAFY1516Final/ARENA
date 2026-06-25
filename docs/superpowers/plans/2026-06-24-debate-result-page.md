# Debate Result Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `/debates/:debateId/result` as the default completed-debate review and sharing screen.

**Architecture:** Keep live debate progression in `DebateRoomView.vue`; move completed debate review, round selection, and post registration actions into a new `DebateResultView.vue`. Reuse `debateStore.fetchDebate()` and `debateStore.shareDebate()` so backend API changes are not required for the MVP.

**Tech Stack:** Vue 3, Vue Router, Pinia, Naive UI, Vitest, existing Spring Boot debate/share APIs.

---

### Task 1: Add Result Route And Recent-Debate Navigation

**Files:**
- Modify: `client/src/router/index.js`
- Modify: `client/src/views/MyDebatesView.vue`
- Test: `client/src/views/__tests__/MyDebatesView.test.js`

- [ ] **Step 1: Write the failing navigation test**

Update `opens an existing debate from the recent list in review mode` so routes include `{ path: '/debates/:debateId/result', component: { template: '<div>result</div>' } }` and expectations are:

```js
expect(router.currentRoute.value.path).toBe('/debates/10/result')
expect(router.currentRoute.value.query).toEqual({ from: 'recent' })
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker compose exec -T client npm test -- --run src/views/__tests__/MyDebatesView.test.js`

Expected: FAIL because `openDebate()` still routes to `/debates/10`.

- [ ] **Step 3: Implement route and navigation**

Add this route before `/debates/:debateId`:

```js
{
  path: '/debates/:debateId/result',
  name: 'debate-result',
  component: () => import('@/views/DebateResultView.vue'),
},
```

Change `openDebate()`:

```js
router.push({
  path: `/debates/${debate.debateId}/result`,
  query: { from: 'recent' },
})
```

- [ ] **Step 4: Run test to verify it passes**

Run: `docker compose exec -T client npm test -- --run src/views/__tests__/MyDebatesView.test.js`

Expected: PASS.

### Task 2: Create Debate Result View

**Files:**
- Create: `client/src/views/DebateResultView.vue`
- Test: `client/src/views/__tests__/DebateResultView.test.js`
- Modify: `client/src/styles/base.css`

- [ ] **Step 1: Write failing result-page tests**

Create tests that mount `/debates/10/result`, mock `debateApi.detail`, and verify:

```js
expect(wrapper.text()).toContain('내 선택')
expect(wrapper.text()).toContain('제육파')
expect(wrapper.findAll('.result-round-card')).toHaveLength(3)
expect(wrapper.text()).toContain('1라운드')
expect(wrapper.text()).toContain('제육은 만족감이 큽니다.')
expect(wrapper.text()).toContain('돈까스는 실패가 적습니다.')
expect(wrapper.text()).toContain('새로운 토론')
expect(wrapper.text()).toContain('게시글 등록')
```

Also test clicking round 2 updates the displayed round messages.

- [ ] **Step 2: Run test to verify it fails**

Run: `docker compose exec -T client npm test -- --run src/views/__tests__/DebateResultView.test.js`

Expected: FAIL because `DebateResultView.vue` does not exist.

- [ ] **Step 3: Implement minimal result view**

`DebateResultView.vue` should:
- Fetch debate by route param on mount.
- Group messages by `roundNo`.
- Show title, original topic, selected side from `route.query.choice`.
- Default selected round to the first round.
- Render clickable `.result-round-card` buttons.
- Render selected round messages with the same `DebateMessage` component.
- `새로운 토론` routes to `/new?topic=<originalTopic>&candidates=1`.
- `게시글 등록` calls `debateStore.shareDebate()` using title `currentDebate.topic`, vote labels from `debateSideLabels`, and routes to `/posts/:postId`.

- [ ] **Step 4: Add focused result styles**

Add `.debate-result-page`, `.result-summary-card`, `.result-round-grid`, `.result-round-card`, `.result-round-card--selected`, `.result-selected-round`, and `.result-actions` styles to `client/src/styles/base.css`.

- [ ] **Step 5: Run test to verify it passes**

Run: `docker compose exec -T client npm test -- --run src/views/__tests__/DebateResultView.test.js`

Expected: PASS.

### Task 3: Route Debate Choice To Result Page

**Files:**
- Modify: `client/src/views/DebateRoomView.vue`
- Test: `client/src/views/__tests__/DebateRoomView.test.js`

- [ ] **Step 1: Write failing room-flow test**

Update `moves from automatic choice to summary actions after selecting a side` to expect:

```js
expect(router.currentRoute.value.path).toBe('/debates/999/result')
expect(router.currentRoute.value.query.choice).toBe('COOL_HEADED')
```

Remove expectations for in-room `토론 더 진행하기` and `게시글 등록` after selection.

- [ ] **Step 2: Run test to verify it fails**

Run: `docker compose exec -T client npm test -- --run src/views/__tests__/DebateRoomView.test.js`

Expected: FAIL because `pickSide()` still stays on the room screen.

- [ ] **Step 3: Implement result navigation**

Change `pickSide(side)` to stop the debate, preserve the short selection animation delay, then route:

```js
router.push({
  path: `/debates/${route.params.debateId}/result`,
  query: { choice: side },
})
```

- [ ] **Step 4: Run test to verify it passes**

Run: `docker compose exec -T client npm test -- --run src/views/__tests__/DebateRoomView.test.js`

Expected: PASS.

### Task 4: Update Documentation And Full Verification

**Files:**
- Modify: `docs/codex-handoff.md`
- Modify: `docs/api.md` only if frontend behavior note is already present there; otherwise leave API unchanged.

- [ ] **Step 1: Document the new MVP flow**

Add a note to `docs/codex-handoff.md` that completed debates now default to `/debates/:id/result`, where users can review rounds, start a new debate from the original topic, or register a post from the selected round.

- [ ] **Step 2: Run frontend tests**

Run: `docker compose exec -T client npm test -- --run src/views/__tests__/MyDebatesView.test.js src/views/__tests__/DebateRoomView.test.js src/views/__tests__/DebateResultView.test.js`

Expected: PASS.

- [ ] **Step 3: Run frontend build**

Run: `docker compose exec -T client npm run build`

Expected: PASS.

- [ ] **Step 4: Restart client container**

Run: `docker compose restart client`

Expected: `arena-client Started`.

## Self-Review

- Spec coverage: route split, automatic result navigation, recent-debate default result page, round review, new debate action, and post registration are covered.
- Placeholder scan: no placeholders or TBD items remain.
- Type consistency: route query uses `choice`; debate messages use existing `roundNo`, `speaker`, and `content`; sharing uses existing `shareDebate`.
