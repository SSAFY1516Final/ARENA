from PIL import Image, ImageDraw, ImageFont


W, H = 1600, 1050


def font(size, bold=False):
    candidates = [
        "/System/Library/Fonts/AppleSDGothicNeo.ttc",
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf" if bold else "/System/Library/Fonts/Supplemental/Arial.ttf",
        "/Library/Fonts/Arial Unicode.ttf",
    ]
    for path in candidates:
        try:
            return ImageFont.truetype(path, size, index=0)
        except Exception:
            continue
    return ImageFont.load_default()


F_TITLE = font(34, True)
F_SUB = font(16)
F_LABEL = font(18, True)
F_SMALL = font(13, True)


def centered(draw, xy, text, fnt, fill="#111827"):
    x, y = xy
    box = draw.textbbox((0, 0), text, font=fnt)
    draw.text((x - (box[2] - box[0]) / 2, y - (box[3] - box[1]) / 2), text, font=fnt, fill=fill)


def actor(draw, x, y, name, sub=None):
    draw.ellipse((x - 24, y - 24, x + 24, y + 24), outline="#111827", width=3)
    draw.line((x, y + 24, x, y + 95), fill="#111827", width=3)
    draw.line((x - 45, y + 52, x + 45, y + 52), fill="#111827", width=3)
    draw.line((x, y + 95, x - 38, y + 150), fill="#111827", width=3)
    draw.line((x, y + 95, x + 38, y + 150), fill="#111827", width=3)
    centered(draw, (x, y + 190), name, F_LABEL)
    if sub:
        centered(draw, (x, y + 214), sub, F_SMALL, "#475569")


def ellipse(draw, cx, cy, rx, ry, text, fill, outline):
    draw.ellipse((cx - rx, cy - ry, cx + rx, cy + ry), fill=fill, outline=outline, width=2)
    centered(draw, (cx, cy + 2), text, F_LABEL)


def line(draw, points, fill="#64748b", width=2):
    draw.line(points, fill=fill, width=width, joint="curve")


def build(path, korean=True):
    img = Image.new("RGB", (W, H), "#f8fafc")
    d = ImageDraw.Draw(img)

    title = "ARENA(아레나) 유스케이스 다이어그램" if korean else "ARENA(아레나) Use Case Diagram"
    subtitle = "AI 토론 커뮤니티 MVP · 비회원/회원/AI 서버 관점" if korean else "AI debate community MVP · Guest / Member / AI Service"
    d.text((80, 42), title, font=F_TITLE, fill="#111827")
    d.text((80, 86), subtitle, font=F_SUB, fill="#64748b")

    d.rounded_rectangle((360, 150, 1210, 940), radius=18, fill="#ffffff", outline="#0f766e", width=3)
    centered(d, (785, 190), "ARENA(아레나) System", F_LABEL)

    actor(d, 145, 275, "비회원" if korean else "Guest")
    actor(d, 145, 690, "회원" if korean else "Member")
    actor(d, 1400, 472, "AI 연동 계층" if korean else "AI Integration", "Spring AI")

    labels = {
        "signup": "회원가입" if korean else "Sign up",
        "login": "로그인" if korean else "Log in",
        "logout": "로그아웃" if korean else "Log out",
        "board": "게시판 목록 조회" if korean else "View board list",
        "detail": "게시글 상세 조회" if korean else "View post detail",
        "search": "게시글 검색/필터" if korean else "Search/filter posts",
        "vote": "게시글 투표" if korean else "Vote on post",
        "create": "토론 생성" if korean else "Create debate",
        "turn": "다음 토론 턴 생성" if korean else "Next debate turn",
        "stop": "토론 중단" if korean else "Stop debate",
        "share": "중단된 토론 공유" if korean else "Share stopped debate",
        "summary": "AI 요약 생성" if korean else "Generate summary",
        "comment": "댓글 작성" if korean else "Write comment",
        "del_comment": "본인 댓글 삭제" if korean else "Delete own comment",
        "del_post": "본인 게시글 삭제" if korean else "Delete own post",
        "failure": "AI 장애 처리" if korean else "Handle AI failure",
    }

    ellipse(d, 525, 260, 115, 38, labels["signup"], "#ecfeff", "#0891b2")
    ellipse(d, 780, 260, 115, 38, labels["login"], "#ecfeff", "#0891b2")
    ellipse(d, 1035, 260, 115, 38, labels["logout"], "#ecfeff", "#0891b2")
    ellipse(d, 580, 380, 145, 38, labels["board"], "#f0fdf4", "#16a34a")
    ellipse(d, 945, 380, 145, 38, labels["detail"], "#f0fdf4", "#16a34a")
    ellipse(d, 580, 450, 145, 34, labels["search"], "#f0fdf4", "#16a34a")
    ellipse(d, 945, 450, 145, 34, labels["vote"], "#f0fdf4", "#16a34a")
    ellipse(d, 545, 520, 120, 38, labels["create"], "#eff6ff", "#2563eb")
    ellipse(d, 820, 520, 145, 38, labels["turn"], "#eff6ff", "#2563eb")
    ellipse(d, 1085, 520, 120, 38, labels["stop"], "#eff6ff", "#2563eb")
    ellipse(d, 820, 650, 145, 38, labels["share"], "#eff6ff", "#2563eb")
    ellipse(d, 1085, 650, 120, 38, labels["summary"], "#eff6ff", "#2563eb")
    ellipse(d, 545, 790, 120, 38, labels["comment"], "#fff7ed", "#f97316")
    ellipse(d, 820, 790, 135, 38, labels["del_comment"], "#fff7ed", "#f97316")
    ellipse(d, 1085, 790, 135, 38, labels["del_post"], "#fff7ed", "#f97316")
    ellipse(d, 820, 900, 125, 34, labels["failure"], "#fef2f2", "#dc2626")

    # Association lines. These are intentionally simple so the PNG remains readable.
    for p in [
        [(200, 330), (410, 265)], [(200, 355), (440, 378)], [(200, 375), (800, 382)], [(200, 395), (440, 448)], [(200, 320), (665, 260)],
        [(205, 735), (425, 525)], [(205, 748), (675, 525)], [(205, 760), (968, 523)], [(205, 785), (675, 655)],
        [(205, 772), (800, 455)],
        [(205, 805), (425, 792)], [(205, 825), (685, 793)], [(205, 845), (955, 794)], [(205, 715), (935, 268)],
    ]:
        line(d, p)

    for p in [[(1245, 530), (1360, 520)], [(1220, 650), (1360, 560)], [(945, 900), (1362, 610)]]:
        line(d, p, "#f97316", 3)

    d.rounded_rectangle((80, 925, 1300, 995), radius=12, fill="#ffffff", outline="#cbd5e1", width=2)
    legend = [("Auth" if not korean else "인증", "#ecfeff", "#0891b2"), ("Board" if not korean else "게시판", "#f0fdf4", "#16a34a"), ("Debate" if not korean else "토론", "#eff6ff", "#2563eb"), ("Comments" if not korean else "댓글", "#fff7ed", "#f97316"), ("Failure" if not korean else "장애/예외", "#fef2f2", "#dc2626")]
    x = 140
    for text, fill, outline in legend:
        d.ellipse((x - 42, 942, x + 42, 978), fill=fill, outline=outline, width=2)
        centered(d, (x + 95, 962), text, F_SMALL, "#475569")
        x += 190
    line(d, [(1070, 960), (1135, 960)], "#f97316", 3)
    centered(d, (1225, 962), "AI 서버 연동" if korean else "AI integration", F_SMALL, "#475569")

    img.save(path)


build("arena-use-case-ko.png", True)
build("arena-use-case.png", False)
