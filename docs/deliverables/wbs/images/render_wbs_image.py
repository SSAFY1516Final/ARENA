from PIL import Image, ImageDraw, ImageFont


W, H = 1800, 1200


def font(size, bold=False):
    for p in ["/System/Library/Fonts/AppleSDGothicNeo.ttc", "/System/Library/Fonts/Supplemental/Arial.ttf"]:
        try:
            return ImageFont.truetype(p, size, index=0)
        except Exception:
            pass
    return ImageFont.load_default()


F_TITLE = font(42, True)
F_SUB = font(18)
F_HEAD = font(22, True)
F_TEXT = font(15)
F_SMALL = font(13)

BG = "#F8FAFC"
INK = "#111827"
MUTED = "#64748B"
LINE = "#CBD5E1"
PANEL = "#FFFFFF"

PHASES = [
    ("1. 계획", "#0F766E", ["범위/인수 기준 확정", "요구사항 정의", "기획 산출물 초안"]),
    ("2. 설계", "#2563EB", ["아키텍처 정의", "DB/MyBatis 설계", "화면정의서 확정", "AI 프롬프트 설계"]),
    ("3. 기반 구축", "#7C3AED", ["Spring Boot 3.x 구성", "Spring AI 베이스 환경", "MySQL DDL/시드 작성"]),
    ("4. 회원/인증", "#0891B2", ["카카오 OAuth 로그인", "JWT 발급/검증 구성", "작성자 권한 검사"]),
    ("5. AI", "#F97316", ["다음 턴 생성 API", "요약 생성 API", "타임아웃/목 응답 처리"]),
    ("6. 토론", "#16A34A", ["토론 생성", "토론 턴 저장", "토론방 UI", "중단/요약 저장"]),
    ("7. 게시판/댓글", "#CA8A04", ["토론 공유", "검색/필터 목록", "상세/투표", "댓글 작성/삭제"]),
    ("8. 테스트", "#DC2626", ["매퍼/서비스/컨트롤러 테스트", "AI 클라이언트 테스트", "E2E 시나리오 검증", "치명 결함 수정"]),
    ("9. 발표 준비", "#111827", ["시연 스크립트", "샘플 토론 데이터", "발표자료", "최종 리허설"]),
]


def text_center(draw, box, text, fnt, fill=INK):
    x1, y1, x2, y2 = box
    b = draw.textbbox((0, 0), text, font=fnt)
    draw.text((x1 + (x2 - x1 - (b[2] - b[0])) / 2, y1 + (y2 - y1 - (b[3] - b[1])) / 2), text, font=fnt, fill=fill)


def text_left(draw, xy, text, fnt, fill=INK):
    draw.text(xy, text, font=fnt, fill=fill)


def rr(draw, box, fill=PANEL, outline=LINE, width=2, radius=18):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def draw_phase(draw, x, y, w, h, title, color, tasks):
    rr(draw, (x, y, x + w, y + h), fill=PANEL, outline=LINE, width=2, radius=18)
    rr(draw, (x, y, x + w, y + 54), fill=color, outline=color, width=2, radius=18)
    draw.rectangle((x, y + 32, x + w, y + 54), fill=color)
    text_center(draw, (x + 18, y, x + w - 18, y + 54), title, F_HEAD, "#FFFFFF")
    ty = y + 76
    for item in tasks:
        draw.ellipse((x + 24, ty + 5, x + 34, ty + 15), fill=color)
        text_left(draw, (x + 46, ty), item, F_TEXT, INK)
        ty += 34


def main():
    img = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(img)

    text_left(d, (80, 54), "ARENA(아레나) WBS", F_TITLE)
    text_left(d, (80, 110), "2026-05-15 ~ 2026-06-26 · 계획부터 발표 준비까지의 작업 분해 구조", F_SUB, MUTED)

    rr(d, (80, 160, 1720, 250), fill="#FFFFFF", outline=LINE, width=2, radius=18)
    summary = [
        ("작업 기간", "5/15 ~ 6/26"),
        ("작업 패키지", "30개"),
        ("범위", "계획 · 설계 · 구현 · 테스트 · 발표 준비"),
        ("규칙", "8/80 법칙 적용"),
    ]
    sx = 120
    for label, value in summary:
        text_left(d, (sx, 184), label, F_SMALL, MUTED)
        text_left(d, (sx, 210), value, F_HEAD, INK)
        sx += 395

    cols = 3
    card_w = 500
    card_h = 220
    gap_x = 50
    gap_y = 40
    start_x = 80
    start_y = 300
    for i, (title, color, tasks) in enumerate(PHASES):
        row = i // cols
        col = i % cols
        x = start_x + col * (card_w + gap_x)
        y = start_y + row * (card_h + gap_y)
        draw_phase(d, x, y, card_w, card_h, title, color, tasks)

    rr(d, (80, 1100, 1720, 1150), fill="#FFFFFF", outline=LINE, width=2, radius=14)
    text_left(d, (112, 1115), "WBS는 일정표가 아니라 전체 작업을 산출물 중심으로 분해한 구조입니다. 상세 일정은 간트차트 엑셀에서 관리합니다.", F_SMALL, MUTED)

    img.save("arena-wbs-clean-ko.png")


if __name__ == "__main__":
    main()
