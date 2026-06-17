export const personas = {
  COOL_HEADED: {
    label: '냉정파',
    shortLabel: '냉',
    tone: '비용, 리스크, 효율, 실행 가능성 중심',
  },
  PASSIONATE: {
    label: '열정파',
    shortLabel: '열',
    tone: '재미, 감정, 즉시 만족, 몰입감 중심',
  },
}

export const sampleDebate = {
  debateId: 10,
  topic: '오늘 점심 제육 vs 돈까스',
  mode: 'PRACTICAL',
  status: 'ACTIVE',
  peakReached: false,
}

export const sampleMessages = [
  {
    messageId: 31,
    speaker: 'COOL_HEADED',
    roundNo: 1,
    content: '돈까스는 메뉴 변수가 적고 실패 확률이 낮습니다. 오후 일정이 빡빡하면 안정성이 더 중요합니다.',
  },
  {
    messageId: 32,
    speaker: 'PASSIONATE',
    roundNo: 1,
    content: '하지만 점심은 단순 연료가 아니죠. 제육의 매콤함은 남은 하루를 버티게 하는 보상입니다.',
  },
]

export const nextTurnSamples = [
  {
    speaker: 'COOL_HEADED',
    content: '오늘 미팅이 있다면 매운맛 리스크를 계산해야 합니다. 만족보다 컨디션 관리가 우선입니다.',
  },
  {
    speaker: 'PASSIONATE',
    content: '컨디션도 중요하지만 반복되는 하루에는 확실한 보상이 필요합니다. 제육은 선택의 후회를 줄여줍니다.',
  },
  {
    speaker: 'COOL_HEADED',
    content: '대기 시간과 식후 집중력까지 합치면 돈까스가 더 예측 가능한 선택입니다.',
  },
]

export const sampleSummary = {
  coreArguments: '냉정파는 안정성과 오후 일정 리스크를, 열정파는 즉시 만족감과 점심 보상을 강조했습니다.',
  highlight: '열정파가 제육의 매콤함을 하루를 버티게 하는 보상이라고 표현한 장면',
  decisionCriteria: '선택 기준: 오후 일정이 중요하면 돈까스, 지금의 만족이 중요하면 제육',
  remainingIssue: '매운맛이 오후 집중력에 미치는 영향',
  summaryText: '선택 기준은 안정성과 만족감의 차이입니다. 돈까스는 예측 가능성을, 제육은 즉시 만족을 최적화합니다.',
}

export const samplePosts = [
  {
    postId: 5,
    debateId: 10,
    title: '오늘 점심 제육 vs 돈까스',
    mode: 'PRACTICAL',
    summaryCard: '안정성을 택하면 돈까스, 지금의 만족을 택하면 제육입니다.',
    body: '점심을 고르는 기준이 매번 달라져서 공유합니다. 안정적인 선택과 지금 당장의 만족 중 무엇이 더 중요한지 투표로 보고 싶어요.',
    voteOptionA: '제육',
    voteOptionB: '돈까스',
    voteA: 42,
    voteB: 58,
    commentCount: 12,
    createdAt: '2026-06-05T10:30:00',
  },
  {
    postId: 6,
    debateId: 11,
    title: '오타니 10명 vs 북극곰',
    mode: 'ENTERTAINMENT',
    summaryCard: '전력 분석보다 과장과 반박이 살아나는 예능형 논쟁입니다.',
    body: '친구들과 이야기하다가 나온 주제입니다. 말이 안 되는 대결일수록 사람들이 어느 쪽 상상을 더 설득력 있게 보는지 궁금합니다.',
    voteOptionA: '오타니 팀',
    voteOptionB: '북극곰',
    voteA: 71,
    voteB: 29,
    commentCount: 31,
    createdAt: '2026-06-04T18:12:00',
  },
  {
    postId: 7,
    debateId: 12,
    title: '아이폰 vs 갤럭시',
    mode: 'PRACTICAL',
    summaryCard: '생태계, 카메라, 유지비, 익숙함을 기준으로 비교했습니다.',
    body: '휴대폰을 바꾸기 전에 실제 사용자들의 선택 기준을 듣고 싶습니다. 익숙함과 기능성 중 어디에 더 무게를 두는지 궁금해요.',
    voteOptionA: '아이폰',
    voteOptionB: '갤럭시',
    voteA: 49,
    voteB: 51,
    commentCount: 8,
    createdAt: '2026-06-03T11:20:00',
  },
  {
    postId: 8,
    debateId: 13,
    title: '여행지 제주 vs 부산',
    mode: 'PRACTICAL',
    summaryCard: '예산, 이동 시간, 먹거리, 일정 밀도를 함께 비교했습니다.',
    body: '짧은 휴가를 계획 중인데 제주와 부산 중 고민입니다. 이동 피로와 먹거리 만족도 중 어떤 쪽이 더 중요한지 의견을 받고 싶어요.',
    voteOptionA: '제주',
    voteOptionB: '부산',
    voteA: 64,
    voteB: 36,
    commentCount: 5,
    createdAt: '2026-06-02T09:42:00',
  },
]

export const sampleComments = [
  {
    commentId: 1,
    postId: 5,
    author: '토론러',
    content: '저는 오늘 일정 없으면 제육 갑니다.',
    createdAt: '2026-06-05T11:00:00',
  },
  {
    commentId: 2,
    postId: 5,
    author: '현실파',
    content: '돈까스는 실패 확률이 낮아서 인정.',
    createdAt: '2026-06-05T11:12:00',
  },
]
