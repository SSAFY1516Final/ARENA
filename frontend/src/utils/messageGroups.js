export function groupMessagesByRound(messages) {
  const groups = []

  for (const message of messages) {
    const lastGroup = groups[groups.length - 1]

    if (lastGroup?.roundNo === message.roundNo) {
      lastGroup.messages.push(message)
    } else {
      groups.push({
        roundNo: message.roundNo,
        messages: [message],
      })
    }
  }

  return groups
}
