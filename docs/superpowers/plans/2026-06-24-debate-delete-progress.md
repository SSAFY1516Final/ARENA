# Debate Delete Progress

Date: 2026-06-24

## Goal

Allow a signed-in user to delete one of their recent debates from the My Debates page.

## Scope

- Add an authenticated `DELETE /api/debates/{debateId}` endpoint.
- Verify ownership before deletion.
- Delete dependent data in FK-safe order: post votes, comments, shared post, summary, messages, debate session.
- Add a delete action to recent debate cards.
- Remove the deleted debate from the Pinia store without navigating into the debate room.
- Update existing API and requirements documents.

## Decisions

- Deletion is a hard delete for the MVP.
- If a debate was shared to the board, its shared post and related comments/votes are also deleted.
- Preview-only dummy debate cards do not expose a delete button.
- The card remains clickable for opening the debate; the delete button stops event propagation.

## Verification

- Added backend service test for deletion order.
- Added frontend store test for API call and list removal.
- Added My Debates view test for deleting from the recent debate card without route navigation.
