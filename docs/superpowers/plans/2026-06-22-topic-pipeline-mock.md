# Topic Pipeline Mock Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Change `/new` from mode-based debate creation to a topic-candidate pipeline mock that follows the shared choice-pipeline document.

**Architecture:** Keep the existing backend contract unchanged. The frontend gathers `topic`, `condition`, and `details`, generates deterministic mock candidates locally, lets the user select one, then calls the existing debate creation API with the selected candidate title and an internal default mode.

**Tech Stack:** Vue 3, Pinia, Vue Router, Vitest, existing Spring Boot debate API.

---

### Task 1: Update HomeView Tests

**Files:**
- Modify: `client/src/views/__tests__/HomeView.test.js`

- [ ] **Step 1: Replace mode-oriented assertions**

Assert that `/new` renders the pipeline input fields, candidate cards, and no visible `실용 판정` or `예능 배틀` copy.

- [ ] **Step 2: Assert selected candidate is sent to the existing API**

Submit the form after selecting a generated candidate and expect `debateApi.create` to receive `{ topic: selectedCandidateTitle, mode: 'PRACTICAL' }`.

### Task 2: Refactor HomeView UI

**Files:**
- Modify: `client/src/views/HomeView.vue`

- [ ] **Step 1: Remove ModeCard usage**

Delete the visible practical/entertainment selector and the `mode` state.

- [ ] **Step 2: Add pipeline input state**

Use `topic`, `condition`, and `details` refs.

- [ ] **Step 3: Add deterministic mock pipeline candidates**

Expose 3 candidate cards derived from the current input and a selected candidate id.

- [ ] **Step 4: Keep existing API integration**

Submit the selected candidate title through `debateStore.createDebate({ topic: selectedCandidate.title, mode: 'PRACTICAL' })`.

### Task 3: Update Styles

**Files:**
- Modify: `client/src/styles/base.css`

- [ ] **Step 1: Add pipeline layout classes**

Add styles for pipeline stages, input grid, candidate cards, score chips, and selected candidate preview.

- [ ] **Step 2: Preserve responsive behavior**

Ensure the existing workspace layout still collapses cleanly on mobile.

### Task 4: Verify

**Commands:**
- `cd client && npm test -- --run`
- `cd client && npm run build`
- `git diff --check`

Expected result: tests pass, production build succeeds, and no whitespace errors are reported.
