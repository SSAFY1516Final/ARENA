from __future__ import annotations

import base64
import subprocess
from pathlib import Path
from urllib.parse import quote
from xml.sax.saxutils import escape


ROOT = Path(__file__).resolve().parent
ARCH_ROOT = ROOT.parent
ICON_DIR = ARCH_ROOT / "icons"
SVG_PATH = ROOT / "arena-ai-pipeline-ko.svg"
PNG_PATH = ROOT / "arena-ai-pipeline-ko.png"

W, H = 1920, 1080

COLORS = {
    "bg": "#F8FAFC",
    "ink": "#111827",
    "muted": "#64748B",
    "line": "#334155",
    "soft_line": "#CBD5E1",
    "blue": "#2563EB",
    "blue_bg": "#EFF6FF",
    "green": "#059669",
    "green_bg": "#ECFDF5",
    "purple": "#6D28D9",
    "purple_bg": "#F5F3FF",
    "orange": "#D97706",
    "orange_bg": "#FFF7ED",
    "yellow": "#F59E0B",
    "yellow_bg": "#FFFBEB",
    "pink": "#BE185D",
    "pink_bg": "#FDF2F8",
    "white": "#FFFFFF",
}

ICON_FILES = {
    "chrome": "googlechrome.svg",
    "vue": "vuedotjs.svg",
    "vite": "vite.svg",
    "springboot": "springboot.svg",
    "springsecurity": "springsecurity.svg",
    "jwt": "jsonwebtokens.svg",
    "mysql": "mysql.svg",
    "kakao": "kakao.svg",
}


def data_uri(icon_name: str) -> str:
    raw = (ICON_DIR / ICON_FILES[icon_name]).read_bytes()
    return "data:image/svg+xml;base64," + base64.b64encode(raw).decode("ascii")


def windows_path(path: Path) -> str:
    resolved = path.resolve()
    parts = resolved.parts
    if len(parts) > 3 and parts[0] == "/" and parts[1] == "mnt" and len(parts[2]) == 1:
        return parts[2].upper() + ":\\" + "\\".join(parts[3:])
    return str(resolved)


def windows_file_uri(path: Path) -> str:
    converted = windows_path(path).replace("\\", "/")
    if len(converted) > 2 and converted[1] == ":":
        return "file:///" + quote(converted)
    return path.resolve().as_uri()


def text(x: int, y: int, value: str, size: int, weight: int = 700, fill: str = "#111827", anchor: str = "start") -> str:
    return (
        f'<text x="{x}" y="{y}" font-family="Noto Sans KR, Malgun Gothic, Arial, sans-serif" '
        f'font-size="{size}" font-weight="{weight}" fill="{fill}" text-anchor="{anchor}">{escape(value)}</text>'
    )


def multiline(x: int, y: int, lines: list[str], size: int, weight: int = 700, fill: str = "#111827", anchor: str = "start", gap: int = 24) -> str:
    return "\n".join(text(x, y + idx * gap, line, size, weight, fill, anchor) for idx, line in enumerate(lines))


def box(x: int, y: int, w: int, h: int, fill: str, stroke: str, rx: int = 24, sw: int = 2) -> str:
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"/>'


def icon(x: int, y: int, size: int, name: str) -> str:
    return f'<image x="{x}" y="{y}" width="{size}" height="{size}" href="{data_uri(name)}" preserveAspectRatio="xMidYMid meet"/>'


def arrow(x1: int, y1: int, x2: int, y2: int, label: str = "", two_way: bool = False, label_y_offset: int = -18) -> str:
    start = ' marker-start="url(#arrow-start)"' if two_way else ""
    parts = [
        f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{COLORS["line"]}" stroke-width="3" marker-end="url(#arrow)"{start}/>'
    ]
    if label:
        parts.append(text((x1 + x2) // 2, (y1 + y2) // 2 + label_y_offset, label, 15, 850, COLORS["muted"], "middle"))
    return "\n".join(parts)


def service_card(
    x: int,
    y: int,
    w: int,
    h: int,
    title: str,
    subtitle: str,
    body: list[str],
    fill: str,
    stroke: str,
    icons: list[str],
) -> str:
    parts = [box(x, y, w, h, fill, stroke, 24, 2)]
    parts.append(text(x + w // 2, y + 44, title, 25, 900, COLORS["ink"], "middle"))
    parts.append(text(x + w // 2, y + 76, subtitle, 17, 800, COLORS["muted"], "middle"))
    if icons:
        total = len(icons) * 56 + (len(icons) - 1) * 18
        sx = x + (w - total) // 2
        for idx, name in enumerate(icons):
            parts.append(icon(sx + idx * 74, y + 104, 56, name))
    parts.append(multiline(x + w // 2, y + h - 78, body, 16, 750, COLORS["ink"], "middle", 26))
    return "\n".join(parts)


def ai_badge(x: int, y: int, size: int = 72) -> str:
    return "\n".join([
        box(x, y, size, size, COLORS["white"], "#DDD6FE", 18, 2),
        text(x + size // 2, y + size // 2 + 13, "AI", 32, 900, COLORS["purple"], "middle"),
    ])


def db_card(x: int, y: int) -> str:
    parts = [
        box(x, y, 400, 170, COLORS["orange_bg"], "#FDBA74", 22, 2),
        icon(x + 34, y + 48, 72, "mysql"),
        text(x + 238, y + 54, "MySQL", 28, 900, COLORS["orange"], "middle"),
        multiline(x + 238, y + 92, ["사용자 입력 · AI 원문 응답", "토론 발화 · 라운드 요약", "게시글 데이터 저장"], 16, 750, COLORS["ink"], "middle", 25),
    ]
    return "\n".join(parts)


def kakao_card(x: int, y: int) -> str:
    parts = [
        box(x, y, 300, 170, COLORS["yellow_bg"], "#FACC15", 22, 2),
        text(x + 150, y + 54, "Kakao OAuth", 24, 900, COLORS["ink"], "middle"),
        icon(x + 116, y + 72, 64, "kakao"),
        text(x + 150, y + 150, "소셜 로그인", 16, 850, COLORS["ink"], "middle"),
    ]
    return "\n".join(parts)


def pipeline_step(x: int, y: int, w: int, number: int, title: str, lines: list[str], color: str, fill: str) -> str:
    parts = [
        box(x, y, w, 142, fill, color, 18, 2),
        f'<circle cx="{x + 34}" cy="{y + 34}" r="19" fill="{color}"/>',
        text(x + 34, y + 43, str(number), 19, 900, "#FFFFFF", "middle"),
        text(x + 66, y + 42, title, 18, 900, color),
        multiline(x + w // 2, y + 86, lines, 15, 700, COLORS["ink"], "middle", 24),
    ]
    return "\n".join(parts)


def build_svg() -> str:
    parts: list[str] = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}" role="img" aria-labelledby="title desc">',
        '<title id="title">ARENA AI 토론 파이프라인 아키텍처</title>',
        '<desc id="desc">Clean Korean SVG architecture diagram for ARENA AI debate pipeline.</desc>',
        "<defs>",
        '<marker id="arrow" markerWidth="12" markerHeight="12" refX="10" refY="4" orient="auto" markerUnits="strokeWidth"><path d="M0,0 L0,8 L11,4 z" fill="#334155"/></marker>',
        '<marker id="arrow-start" markerWidth="12" markerHeight="12" refX="1" refY="4" orient="auto" markerUnits="strokeWidth"><path d="M11,0 L11,8 L0,4 z" fill="#334155"/></marker>',
        "</defs>",
        f'<rect width="{W}" height="{H}" fill="{COLORS["bg"]}"/>',
        text(W // 2, 70, "ARENA AI 토론 파이프라인 아키텍처", 40, 900, COLORS["ink"], "middle"),
        text(W // 2, 112, "Vue Client · Spring Boot API · GMS AI API · MySQL · Kakao OAuth", 22, 750, COLORS["muted"], "middle"),
    ]

    # Main services
    parts.append(service_card(
        70, 190, 255, 270,
        "User Browser", "사용자 화면",
        ["주제 입력", "세부 주제 선택", "결과 확인"],
        COLORS["white"], COLORS["soft_line"], ["chrome"],
    ))
    parts.append(service_card(
        405, 190, 290, 270,
        "Vue Client", "Vue 3 + Vite",
        ["화면 렌더링", "REST API 호출", "토론 진행 UI"],
        COLORS["green_bg"], "#86EFAC", ["vue", "vite"],
    ))
    parts.append(service_card(
        795, 160, 430, 330,
        "Spring Boot API", "Java 17 · Spring Boot 3",
        [],
        COLORS["blue_bg"], "#60A5FA", ["springboot", "springsecurity", "jwt"],
    ))

    # Spring Boot highlight band
    parts += [
        box(840, 370, 106, 56, "#FFFFFF", "#BFDBFE", 14, 2),
        text(893, 404, "인증 / API", 16, 900, "#1D4ED8", "middle"),
        box(958, 370, 106, 56, "#FFFFFF", "#BFDBFE", 14, 2),
        text(1011, 394, "AI", 16, 900, "#1D4ED8", "middle"),
        text(1011, 414, "파이프라인", 15, 900, "#1D4ED8", "middle"),
        box(1076, 370, 106, 56, "#FFFFFF", "#BFDBFE", 14, 2),
        text(1129, 394, "비즈니스", 15, 900, "#1D4ED8", "middle"),
        text(1129, 414, "로직", 15, 900, "#1D4ED8", "middle"),
        box(850, 446, 320, 48, "#FFFFFF", "#BFDBFE", 16, 2),
        text(1010, 476, "AI 호출 · 응답 파싱 · 재시도 · 저장", 16, 900, "#1D4ED8", "middle"),
    ]

    # GMS / Result
    parts += [
        box(1340, 190, 300, 270, COLORS["purple_bg"], "#C4B5FD", 24, 2),
        text(1490, 228, "GMS OpenAI-", 22, 900, "#5B21B6", "middle"),
        text(1490, 258, "compatible API", 22, 900, "#5B21B6", "middle"),
        text(1490, 292, "gpt-5.4-mini", 18, 850, "#5B21B6", "middle"),
        ai_badge(1454, 300),
        multiline(1490, 402, ["세부 주제 후보", "토론 발화", "요약 생성"], 16, 800, "#5B21B6", "middle", 25),
        box(1710, 215, 160, 220, COLORS["white"], COLORS["soft_line"], 24, 2),
        text(1790, 265, "Result", 24, 900, COLORS["ink"], "middle"),
        text(1790, 300, "결과 페이지", 19, 900, COLORS["muted"], "middle"),
        box(1742, 330, 96, 12, "#E5E7EB", "#E5E7EB", 6, 1),
        box(1742, 358, 72, 12, "#E5E7EB", "#E5E7EB", 6, 1),
        text(1790, 398, "토론 기록 · 요약", 14, 850, COLORS["muted"], "middle"),
    ]

    # Data and auth
    parts.append(db_card(690, 560))
    parts.append(kakao_card(1160, 560))

    # Main flow arrows
    parts += [
        arrow(325, 325, 405, 325, "주제 입력 / 선택"),
        arrow(695, 325, 795, 325, "REST API 요청"),
        arrow(1225, 300, 1340, 300, "프롬프트 요청"),
        arrow(1340, 380, 1225, 380, "AI 응답"),
        arrow(1640, 325, 1710, 325, "결과 조회"),
        arrow(1010, 490, 890, 560, "토론 데이터 저장", True, -12),
        arrow(1160, 490, 1310, 560, "로그인 정보", True, -12),
    ]

    # Pipeline band
    parts += [
        box(70, 815, 1780, 210, "#FFFFFF", "#A78BFA", 26, 2),
        text(960, 802, "AI 토론 파이프라인", 29, 900, COLORS["purple"], "middle"),
    ]
    steps = [
        (105, 860, 245, 1, "주제 입력", ["사용자가 원본", "질문을 입력"], COLORS["blue"], COLORS["blue_bg"]),
        (395, 860, 245, 2, "후보 생성", ["AI가 토론 가능한", "세부 주제 생성"], "#0F766E", "#F0FDFA"),
        (685, 860, 245, 3, "발화 생성", ["양측 진영의", "10개 발화 생성"], COLORS["purple"], COLORS["purple_bg"]),
        (975, 860, 245, 4, "요약 / 선택", ["사용자 선택 반영", "AI 요약 생성"], COLORS["pink"], COLORS["pink_bg"]),
        (1265, 860, 245, 5, "DB 저장", ["토론과 요약을", "MySQL에 저장"], COLORS["orange"], COLORS["orange_bg"]),
        (1555, 860, 245, 6, "결과 표시", ["저장된 기록과", "요약을 조회"], COLORS["blue"], COLORS["blue_bg"]),
    ]
    for idx, step in enumerate(steps):
        parts.append(pipeline_step(*step))
        if idx < len(steps) - 1:
            x = step[0] + step[2] + 14
            parts.append(arrow(x, 931, x + 36, 931))

    parts.append("</svg>")
    return "\n".join(parts)


def render_png() -> None:
    chrome_candidates = [
        Path("C:/Program Files/Google/Chrome/Application/chrome.exe"),
        Path("C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"),
        Path("/mnt/c/Program Files/Google/Chrome/Application/chrome.exe"),
        Path("/mnt/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"),
    ]
    chrome = next((candidate for candidate in chrome_candidates if candidate.exists()), None)
    if chrome is None:
        raise FileNotFoundError("Chrome or Edge executable was not found")
    subprocess.run(
        [
            str(chrome),
            "--headless=new",
            "--disable-gpu",
            "--hide-scrollbars",
            "--force-device-scale-factor=2",
            "--window-size=1920,1080",
            f"--screenshot={windows_path(PNG_PATH)}",
            windows_file_uri(SVG_PATH),
        ],
        check=True,
    )


def main() -> None:
    SVG_PATH.write_text(build_svg(), encoding="utf-8")
    render_png()
    print(SVG_PATH)
    print(PNG_PATH)


if __name__ == "__main__":
    main()
