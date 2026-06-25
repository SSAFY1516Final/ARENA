from __future__ import annotations

import base64
import subprocess
from pathlib import Path
from urllib.parse import quote
from xml.sax.saxutils import escape


ROOT = Path(__file__).resolve().parent
ICON_DIR = ROOT / "icons"
SVG_PATH = ROOT / "arena-external-architecture.svg"
PNG_PATH = ROOT / "arena-external-architecture.png"

W, H = 1920, 1080

COLORS = {
    "bg": "#F8FAFC",
    "ink": "#111827",
    "muted": "#64748B",
    "line": "#334155",
    "soft_line": "#CBD5E1",
    "host": "#EAF3FF",
    "host_line": "#93C5FD",
    "external": "#F4F0FF",
    "external_line": "#C4B5FD",
    "client_line": "#BFDBFE",
    "app_line": "#A7F3D0",
    "mysql_bg": "#FFF7E6",
    "mysql_line": "#FDBA74",
    "white": "#FFFFFF",
    "blue": "#3182F6",
    "green": "#00A887",
    "purple": "#8B5CF6",
    "yellow": "#F9D949",
    "black": "#111827",
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
    "docker": "docker.svg",
    "github": "github.svg",
}


def data_uri(icon: str) -> str:
    raw = (ICON_DIR / ICON_FILES[icon]).read_bytes()
    return "data:image/svg+xml;base64," + base64.b64encode(raw).decode("ascii")


def text(x: int, y: int, value: str, size: int, weight: int = 700, fill: str = "#111827", anchor: str = "start") -> str:
    return (
        f'<text x="{x}" y="{y}" font-family="Arial, Noto Sans KR, Malgun Gothic, sans-serif" '
        f'font-size="{size}" font-weight="{weight}" fill="{fill}" text-anchor="{anchor}">{escape(value)}</text>'
    )


def box(x: int, y: int, w: int, h: int, fill: str, stroke: str, rx: int = 24, sw: int = 2) -> str:
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"/>'


def icon(x: int, y: int, size: int, name: str) -> str:
    return f'<image x="{x}" y="{y}" width="{size}" height="{size}" href="{data_uri(name)}" preserveAspectRatio="xMidYMid meet"/>'


def badge(x: int, y: int, w: int, h: int, label: str, fill: str, stroke: str = "#E2E8F0", label_fill: str = "#111827") -> str:
    return "\n".join([
        box(x, y, w, h, fill, stroke, 18, 2),
        text(x + w // 2, y + h // 2 + 7, label, 18, 900, label_fill, "middle"),
    ])


def arrow(x1: int, y1: int, x2: int, y2: int, label: str = "", two_way: bool = False) -> str:
    marker_start = ' marker-start="url(#arrow-start)"' if two_way else ""
    out = [f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{COLORS["line"]}" stroke-width="3" marker-end="url(#arrow)"{marker_start}/>']
    if label:
        lx, ly = (x1 + x2) // 2, (y1 + y2) // 2 - 13
        width = max(144, len(label) * 8 + 34)
        out.append(box(lx - width // 2, ly - 22, width, 34, "#FFFFFF", "#E2E8F0", 17, 1))
        out.append(text(lx, ly + 1, label, 14, 900, "#334155", "middle"))
    return "\n".join(out)


def windows_path(path: Path) -> str:
    resolved = path.resolve()
    parts = resolved.parts
    if len(parts) > 3 and parts[0] == "/" and parts[1] == "mnt" and len(parts[2]) == 1:
        drive = parts[2].upper()
        return drive + ":\\" + "\\".join(parts[3:])
    return str(resolved)


def windows_file_uri(path: Path) -> str:
    converted = windows_path(path).replace("\\", "/")
    if len(converted) > 2 and converted[1] == ":":
        return "file:///" + quote(converted)
    return path.resolve().as_uri()


def container_card(x: int, y: int, w: int, h: int, stroke: str, title: str, stack: str, port: str, roles: list[str], icons: list[str]) -> str:
    parts = [box(x, y, w, h, "#FFFFFF", stroke, 28, 2)]
    icon_total = len(icons) * 58 + (len(icons) - 1) * 18
    start_x = x + (w - icon_total) // 2
    for idx, name in enumerate(icons):
        parts.append(icon(start_x + idx * 76, y + 46, 58, name))
    parts.append(text(x + w // 2, y + 142, title, 27, 900, COLORS["ink"], "middle"))
    parts.append(text(x + w // 2, y + 176, stack, 18, 800, COLORS["muted"], "middle"))
    parts.append(text(x + w // 2, y + 208, port, 17, 900, COLORS["blue"] if "client" in title else COLORS["green"] if "app" in title else "#D97706", "middle"))
    for idx, role in enumerate(roles):
        parts.append(text(x + w // 2, y + 262 + idx * 28, role, 16, 750, COLORS["ink"], "middle"))
    return "\n".join(parts)


def build_svg() -> str:
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}" role="img" aria-labelledby="title desc">',
        "<title id=\"title\">ARENA Architecture — Docker Compose 기반 AI 토론 서비스</title>",
        "<desc id=\"desc\">Presentation architecture diagram for ARENA Docker Compose environment.</desc>",
        "<defs>",
        '<marker id="arrow" markerWidth="12" markerHeight="12" refX="10" refY="4" orient="auto" markerUnits="strokeWidth"><path d="M0,0 L0,8 L11,4 z" fill="#334155"/></marker>',
        '<marker id="arrow-start" markerWidth="12" markerHeight="12" refX="1" refY="4" orient="auto" markerUnits="strokeWidth"><path d="M11,0 L11,8 L0,4 z" fill="#334155"/></marker>',
        "</defs>",
        f'<rect width="{W}" height="{H}" fill="{COLORS["bg"]}"/>',
        text(W // 2, 70, "ARENA Architecture — Docker Compose 기반 AI 토론 서비스", 38, 900, COLORS["ink"], "middle"),
        text(W // 2, 108, "Browser → Vue Client → Spring Boot API → MySQL / Kakao / GMS", 19, 800, COLORS["muted"], "middle"),
    ]

    # Browser
    parts += [
        box(82, 255, 286, 230, "#FFFFFF", COLORS["soft_line"], 28, 2),
        icon(124, 304, 76, "chrome"),
        text(222, 315, "User Browser", 25, 900),
        text(222, 350, "ARENA Web UI", 17, 750, COLORS["muted"]),
        text(222, 382, "JWT 저장", 17, 750, COLORS["muted"]),
        text(222, 414, "REST API 호출", 17, 750, COLORS["muted"]),
    ]

    # Docker host
    parts += [
        box(415, 170, 920, 640, COLORS["host"], COLORS["host_line"], 36, 3),
        text(462, 222, "Docker Compose Host", 31, 900),
        text(464, 256, "client · app · mysql 컨테이너를 Docker Compose로 함께 실행", 16, 750, COLORS["muted"]),
        icon(1260, 205, 54, "docker"),
    ]

    parts.append(container_card(
        485, 330, 270, 330, COLORS["client_line"],
        "arena-client", "Vue 3 + Vite", "Port 15173",
        ["토론 생성/진행 UI", "결과/게시판 UI"],
        ["vue", "vite"],
    ))
    parts.append(container_card(
        820, 305, 340, 370, COLORS["app_line"],
        "arena-app", "Spring Boot 3 API", "Port 18080 → 8080",
        ["REST API", "Auth / JWT", "AI Pipeline", "Business Logic"],
        ["springboot", "springsecurity", "jwt"],
    ))

    # MySQL compact card
    parts += [
        box(842, 704, 340, 100, COLORS["mysql_bg"], COLORS["mysql_line"], 22, 2),
        icon(870, 726, 48, "mysql"),
        text(940, 736, "arena-mysql", 22, 900),
        text(940, 762, "MySQL 8.4 · Port 13306 → 3306", 15, 800, COLORS["muted"]),
        text(940, 787, "User / Debate / Post Data · AI Logs", 14, 900, "#D97706"),
    ]

    # External
    parts += [
        box(1440, 410, 390, 300, COLORS["external"], COLORS["external_line"], 30, 2),
        text(1482, 456, "External Services", 26, 900),
        box(1480, 494, 310, 84, "#FFFFFF", "#E9D5FF", 18, 2),
        icon(1510, 511, 50, "kakao"),
        text(1580, 528, "Kakao OAuth API", 19, 900),
        text(1580, 554, "Social Login", 15, 750, COLORS["muted"]),
        box(1480, 612, 310, 84, "#FFFFFF", "#E9D5FF", 18, 2),
        badge(1504, 630, 58, 48, "AI", "#111827", "#111827", "#FFFFFF"),
        text(1580, 644, "GMS OpenAI-compatible API", 18, 900),
        text(1580, 670, "GPT-5.4-mini", 15, 850, COLORS["muted"]),
        text(1580, 690, "Candidates · Turns · Summary", 13, 750, COLORS["muted"]),
    ]

    # Dev flow
    parts += [
        box(305, 872, 1310, 142, "#FFFFFF", "#E2E8F0", 28, 2),
        text(344, 916, "Development / Demo Environment", 24, 900),
        icon(500, 944, 54, "chrome"),
        text(527, 1026, "Local Developer", 15, 850, COLORS["ink"], "middle"),
        icon(760, 944, 54, "github"),
        text(787, 1026, "GitHub Repository", 15, 850, COLORS["ink"], "middle"),
        icon(1030, 944, 58, "docker"),
        text(1059, 1026, "docker compose up", 15, 850, COLORS["ink"], "middle"),
        icon(1300, 944, 58, "docker"),
        text(1329, 1026, "Docker Containers", 15, 850, COLORS["ink"], "middle"),
        arrow(560, 970, 735, 970, "Git Push"),
        arrow(820, 970, 1005, 970, "build"),
        arrow(1090, 970, 1275, 970, "run"),
    ]

    # Main arrows
    parts += [
        arrow(368, 370, 485, 430, "http://localhost:15173"),
        arrow(755, 500, 820, 500, "REST API /api"),
        arrow(990, 675, 990, 704, "MyBatis", True),
        arrow(1160, 500, 1480, 536, "OAuth Login", True),
        arrow(1160, 610, 1480, 654, "Spring AI ChatClient", True),
    ]

    parts.append("</svg>")
    return "\n".join(parts)


def render_png_with_chrome() -> None:
    chrome_candidates = [
        Path("C:/Program Files/Google/Chrome/Application/chrome.exe"),
        Path("C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"),
        Path("/mnt/c/Program Files/Google/Chrome/Application/chrome.exe"),
        Path("/mnt/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"),
    ]
    chrome = next((candidate for candidate in chrome_candidates if candidate.exists()), None)
    if chrome is None:
        raise FileNotFoundError("Chrome or Edge executable was not found")
    svg_uri = windows_file_uri(SVG_PATH)
    png_target = windows_path(PNG_PATH)
    subprocess.run(
        [
            str(chrome),
            "--headless=new",
            "--disable-gpu",
            "--hide-scrollbars",
            "--force-device-scale-factor=2",
            "--window-size=1920,1080",
            f"--screenshot={png_target}",
            svg_uri,
        ],
        check=True,
    )


def main() -> None:
    SVG_PATH.write_text(build_svg(), encoding="utf-8")
    render_png_with_chrome()
    print(SVG_PATH)
    print(PNG_PATH)


if __name__ == "__main__":
    main()
