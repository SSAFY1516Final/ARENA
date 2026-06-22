from PIL import Image, ImageDraw, ImageFont


W, H = 1600, 1100


def font(size, bold=False):
    for p in ["/System/Library/Fonts/AppleSDGothicNeo.ttc", "/System/Library/Fonts/Supplemental/Arial.ttf"]:
        try:
            return ImageFont.truetype(p, size, index=0)
        except Exception:
            pass
    return ImageFont.load_default()


F_TITLE = font(40, True)
F_SUB = font(18)
F_HEAD = font(24, True)
F_TEXT = font(18)
F_SMALL = font(14)

BG = "#F8FAFC"
INK = "#111827"
MUTED = "#64748B"
LINE = "#CBD5E1"
PRIMARY = "#0F766E"
BLUE = "#0284C7"
GREEN = "#16A34A"
ORANGE = "#F97316"
VIOLET = "#4F46E5"
DARK = "#1F2937"
RED = "#DC2626"


def center(draw, box, text, fnt, fill=INK):
    x1, y1, x2, y2 = box
    b = draw.textbbox((0, 0), text, font=fnt)
    draw.text((x1 + (x2 - x1 - (b[2] - b[0])) / 2, y1 + (y2 - y1 - (b[3] - b[1])) / 2), text, font=fnt, fill=fill)


def left(draw, xy, text, fnt, fill=INK):
    draw.text(xy, text, font=fnt, fill=fill)


def rr(draw, box, fill="#FFFFFF", outline=LINE, width=2, radius=20):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def arrow(draw, start, end, fill=MUTED, width=3):
    draw.line((start, end), fill=fill, width=width)
    x1, y1 = start
    x2, y2 = end
    if abs(x2 - x1) >= abs(y2 - y1):
        s = 1 if x2 >= x1 else -1
        pts = [(x2, y2), (x2 - s * 14, y2 - 7), (x2 - s * 14, y2 + 7)]
    else:
        s = 1 if y2 >= y1 else -1
        pts = [(x2, y2), (x2 - 7, y2 - s * 14), (x2 + 7, y2 - s * 14)]
    draw.polygon(pts, fill=fill)


def usecase(draw, xy, text, outline, fill="#FFFFFF"):
    x, y = xy
    box = (x, y, x + 330, y + 70)
    draw.rounded_rectangle(box, radius=35, fill=fill, outline=outline, width=3)
    center(draw, box, text, F_TEXT)
    return box


def actor(draw, x, y):
    draw.ellipse((x - 28, y - 28, x + 28, y + 28), outline=INK, width=4)
    draw.line((x, y + 28, x, y + 140), fill=INK, width=4)
    draw.line((x - 64, y + 74, x + 64, y + 74), fill=INK, width=4)
    draw.line((x, y + 140, x - 54, y + 238), fill=INK, width=4)
    draw.line((x, y + 140, x + 54, y + 238), fill=INK, width=4)
    center(draw, (x - 100, y + 260, x + 100, y + 305), "사용자", F_HEAD)


def make_use_case():
    img = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(img)
    left(d, (80, 58), "ARENA(아레나) 유스케이스", F_TITLE)
    left(d, (80, 112), "사용자가 시스템으로 달성하는 목표 중심 다이어그램", F_SUB, MUTED)

    actor(d, 160, 420)
    rr(d, (360, 180, 1480, 930), fill="#FFFFFF", outline=PRIMARY, width=4, radius=30)
    center(d, (360, 205, 1480, 250), "ARENA(아레나)", F_HEAD, PRIMARY)
    arrow(d, (240, 555), (360, 555), fill="#94A3B8", width=3)
    left(d, (252, 520), "uses", F_SMALL, MUTED)

    left_col_x = 470
    right_col_x = 1010
    ys = [300, 405, 510, 615, 720]
    nodes = [
        ("회원 관리", left_col_x, ys[0], PRIMARY, "#D1FAE5"),
        ("AI 토론 생성", left_col_x, ys[1], BLUE, "#E0F2FE"),
        ("AI 토론 진행", left_col_x, ys[2], BLUE, "#E0F2FE"),
        ("토론 결과 요약", left_col_x, ys[3], VIOLET, "#EEF2FF"),
        ("토론 결과 공유", right_col_x, ys[1], GREEN, "#DCFCE7"),
        ("공유된 토론 조회", right_col_x, ys[2], GREEN, "#DCFCE7"),
        ("게시글 검색/필터", right_col_x, ys[3], GREEN, "#DCFCE7"),
        ("게시글 투표", left_col_x, ys[4], GREEN, "#DCFCE7"),
        ("댓글 작성", right_col_x, ys[4], ORANGE, "#FFEDD5"),
        ("내 콘텐츠 관리", right_col_x, ys[4] + 90, RED, "#FEE2E2"),
    ]
    boxes = {}
    for label, x, y, color, fill in nodes:
        boxes[label] = usecase(d, (x, y), label, color, fill)

    # Keep the presentation image intentionally clean. Detailed include/extend
    # relationships are kept in the PlantUML and Mermaid source files.
    rr(d, (455, 825, 1370, 875), fill="#F8FAFC", outline=LINE, width=2, radius=14)
    left(d, (485, 840), "상세 관계: 토론 결과 공유는 요약을 포함하고, 검색/필터·투표·댓글은 공유된 토론 조회에서 확장됩니다.", F_SMALL, MUTED)

    rr(d, (80, 975, 1480, 1030), fill="#FFFFFF", outline=LINE, width=2, radius=16)
    left(d, (112, 993), "유스케이스는 화면 순서가 아니라 사용자가 시스템에서 달성하려는 목표를 표현합니다.", F_SMALL, MUTED)
    img.save("arena-use-case-clean-ko.png")


make_use_case()
