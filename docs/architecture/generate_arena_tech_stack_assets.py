#!/usr/bin/env python3
from __future__ import annotations

import html
import json
import re
import urllib.request
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parent
OUT_SVG = ROOT / "arena-tech-stack-slide.svg"
OUT_PNG = ROOT / "arena-tech-stack-slide.png"
OUT_SOURCE = ROOT / "arena-tech-stack-slide.source.json"
OUT_SOURCES = ROOT / "arena-tech-stack-icon-sources.md"
CACHE = ROOT / ".icon-cache"
CACHE.mkdir(exist_ok=True)

WIDTH = 1920
HEIGHT = 1080
SI_BASE = "https://cdn.simpleicons.org"


ICON_SOURCES = {
    "vue": ("vuedotjs", "Vue.js", "https://simpleicons.org/?q=vue"),
    "vite": ("vite", "Vite", "https://simpleicons.org/?q=vite"),
    "pinia": ("pinia", "Pinia", "https://simpleicons.org/?q=pinia"),
    "axios": ("axios", "Axios", "https://simpleicons.org/?q=axios"),
    "openjdk": ("openjdk", "OpenJDK", "https://simpleicons.org/?q=openjdk"),
    "springboot": ("springboot", "Spring Boot", "https://simpleicons.org/?q=spring%20boot"),
    "springsecurity": ("springsecurity", "Spring Security", "https://simpleicons.org/?q=spring%20security"),
    "spring": ("spring", "Spring", "https://simpleicons.org/?q=spring"),
    "kakao": ("kakao", "Kakao", "https://simpleicons.org/?q=kakao"),
    "jwt": ("jsonwebtokens", "JSON Web Tokens", "https://simpleicons.org/?q=json%20web%20tokens"),
    "mysql": ("mysql", "MySQL", "https://simpleicons.org/?q=mysql"),
    "docker": ("docker", "Docker", "https://simpleicons.org/?q=docker"),
    "node": ("nodedotjs", "Node.js", "https://simpleicons.org/?q=node"),
    "gradle": ("gradle", "Gradle", "https://simpleicons.org/?q=gradle"),
    "temurin": ("eclipseadoptium", "Eclipse Adoptium", "https://simpleicons.org/?q=eclipse%20adoptium"),
    "vitest": ("vitest", "Vitest", "https://simpleicons.org/?q=vitest"),
    "junit": ("junit5", "JUnit5", "https://simpleicons.org/?q=junit"),
    "swagger": ("swagger", "Swagger", "https://simpleicons.org/?q=swagger"),
}


def fetch_icon(key: str) -> tuple[str, str, str]:
    slug, _, _ = ICON_SOURCES[key]
    path = CACHE / f"{slug}.svg"
    if not path.exists():
        request = urllib.request.Request(
            f"{SI_BASE}/{slug}",
            headers={"User-Agent": "ARENA-tech-stack-generator/1.0"},
        )
        with urllib.request.urlopen(request, timeout=20) as response:
            path.write_bytes(response.read())
    svg = path.read_text(encoding="utf-8")
    match = re.search(r"<svg([^>]*)>(.*?)</svg>", svg, re.S)
    if not match:
        raise RuntimeError(f"Cannot parse icon {key}")
    attrs, body = match.groups()
    view_box_match = re.search(r'viewBox="([^"]+)"', attrs)
    if not view_box_match:
        raise RuntimeError(f"Cannot parse icon viewBox {key}")
    fill_match = re.search(r'fill="([^"]+)"', attrs)
    view_box = view_box_match.group(1)
    fill = fill_match.group(1) if fill_match else "#111827"
    body = re.sub(r"<title>.*?</title>", "", body, flags=re.S)
    return view_box, body.strip(), fill


def esc(text: str) -> str:
    return html.escape(text, quote=True)


def icon(key: str, x: int, y: int, size: int, label: str, version: str = "") -> str:
    view_box, body, fill = fetch_icon(key)
    min_x, min_y, view_w, view_h = [float(part) for part in view_box.replace(",", " ").split()]
    scale = size / max(view_w, view_h)
    draw_w = view_w * scale
    draw_h = view_h * scale
    offset_x = x + (size - draw_w) / 2 - min_x * scale
    offset_y = y + (size - draw_h) / 2 - min_y * scale
    label_y = y + size + 24
    version_markup = f'<text class="version" x="{x + size / 2}" y="{label_y + 18}" text-anchor="middle">{esc(version)}</text>' if version else ""
    return f"""
      <g class="tech-icon" aria-label="{esc(label)}">
        <g transform="translate({offset_x:.3f} {offset_y:.3f}) scale({scale:.6f})" fill="{esc(fill)}">{body}</g>
        <text class="tech-label" x="{x + size / 2}" y="{label_y}" text-anchor="middle">{esc(label)}</text>
        {version_markup}
      </g>
    """


def badge(x: int, y: int, text: str, w: int = 132, color: str = "#ffffff", stroke: str = "#CBD5E1") -> str:
    return f"""
      <g>
        <rect class="badge" x="{x}" y="{y}" width="{w}" height="32" rx="16" fill="{color}" stroke="{stroke}"/>
        <text class="badge-text" x="{x + w / 2}" y="{y + 21}" text-anchor="middle">{esc(text)}</text>
      </g>
    """


def desc_lines(x: int, y: int, lines: list[str]) -> str:
    return "\n".join(
        f'<text class="desc" x="{x}" y="{y + index * 20}">{esc(line)}</text>'
        for index, line in enumerate(lines)
    )


@dataclass(frozen=True)
class Category:
    title: str
    x: int
    y: int
    w: int
    h: int
    fill: str
    stroke: str
    content: str


def category(cat: Category) -> str:
    return f"""
      <g class="category">
        <rect x="{cat.x}" y="{cat.y}" width="{cat.w}" height="{cat.h}" rx="26" fill="{cat.fill}" stroke="{cat.stroke}" stroke-width="1.8"/>
        <text class="category-title" x="{cat.x + 26}" y="{cat.y + 43}">{esc(cat.title)}</text>
        {cat.content}
      </g>
    """


def build_svg() -> str:
    frontend = (
        icon("vue", 95, 178, 86, "Vue", "3.5")
        + icon("vite", 210, 178, 86, "Vite", "6")
        + icon("pinia", 94, 312, 48, "Pinia", "3")
        + icon("axios", 204, 312, 48, "Axios", "")
        + badge(88, 412, "Vue Router 4", 140, "#FFFFFF", "#A7F3D0")
        + badge(238, 412, "Naive UI", 110, "#FFFFFF", "#A7F3D0")
        + desc_lines(92, 485, ["SPA UI", "Routing", "State Management", "REST API Client"])
    )
    backend = (
        icon("springboot", 508, 170, 104, "Spring Boot", "3.5")
        + icon("openjdk", 440, 320, 58, "Java", "17")
        + badge(526, 325, "MyBatis 3.0.5", 142, "#FFFFFF", "#BFDBFE")
        + icon("springsecurity", 685, 316, 58, "Security", "")
        + badge(440, 412, "Spring Validation", 164, "#FFFFFF", "#BFDBFE")
        + badge(616, 412, "Spring Web MVC", 154, "#FFFFFF", "#BFDBFE")
        + badge(440, 454, "Lombok", 104, "#FFFFFF", "#BFDBFE")
        + desc_lines(440, 520, ["REST API", "Auth / Authorization", "Business Logic", "SQL Mapper"])
    )
    auth = (
        icon("kakao", 862, 180, 76, "Kakao", "OAuth")
        + icon("jwt", 988, 180, 76, "JWT", "")
        + badge(852, 318, "JJWT 0.12.6", 142, "#FFFFFF", "#FDE68A")
        + badge(1008, 318, "Spring Security", 154, "#FFFFFF", "#FDE68A")
        + desc_lines(858, 410, ["Kakao Login", "Access Token", "Protected API"])
    )
    ai = (
        icon("spring", 1250, 175, 76, "Spring AI", "1.0.0")
        + badge(1368, 184, "GMS OpenAI-compatible API", 232, "#FFFFFF", "#DDD6FE")
        + badge(1398, 230, "gpt-5.4-mini", 152, "#FFFFFF", "#DDD6FE")
        + badge(1240, 318, "OpenAI starter", 142, "#FFFFFF", "#DDD6FE")
        + badge(1392, 318, "Prompt Template", 164, "#FFFFFF", "#DDD6FE")
        + badge(1566, 318, "JSON Parser", 124, "#FFFFFF", "#DDD6FE")
        + desc_lines(1244, 410, ["Topic Candidates", "Debate Turns", "Summary", "JSON Parsing"])
    )
    database = (
        icon("mysql", 125, 682, 100, "MySQL", "8.4")
        + badge(264, 708, "Connector/J", 132, "#FFFFFF", "#FDBA74")
        + badge(264, 752, "Mapper XML", 132, "#FFFFFF", "#FDBA74")
        + desc_lines(96, 842, ["User Data", "Debate Data", "Post / Comment / Vote", "AI Logs"])
    )
    infra = (
        icon("docker", 528, 672, 92, "Docker", "")
        + badge(664, 696, "Docker Compose", 166, "#FFFFFF", "#CBD5E1")
        + icon("node", 470, 820, 48, "Node", "22 Alpine")
        + icon("gradle", 602, 820, 48, "Gradle", "8.14")
        + icon("temurin", 734, 820, 48, "Temurin", "17 JRE")
        + badge(520, 920, "Local Development / Demo Environment", 310, "#FFFFFF", "#CBD5E1")
        + desc_lines(490, 968, ["arena-client · arena-app · arena-mysql", "docker compose up"])
    )
    testing = (
        icon("vitest", 940, 688, 78, "Vitest", "4")
        + icon("junit", 1068, 688, 78, "JUnit", "5")
        + badge(910, 828, "Vue Test Utils", 150, "#FFFFFF", "#FBCFE8")
        + badge(1072, 828, "jsdom", 88, "#FFFFFF", "#FBCFE8")
        + badge(910, 872, "Spring Boot Test", 166, "#FFFFFF", "#FBCFE8")
        + badge(1088, 872, "Security Test", 134, "#FFFFFF", "#FBCFE8")
        + badge(910, 916, "MyBatis Test", 134, "#FFFFFF", "#FBCFE8")
        + desc_lines(910, 960, ["Frontend / Backend Test", "Security / MyBatis Test"])
    )
    api_docs = (
        icon("swagger", 1395, 690, 86, "Swagger UI", "")
        + badge(1336, 830, "Springdoc OpenAPI", 178, "#FFFFFF", "#99F6E4")
        + desc_lines(1338, 910, ["REST API Documentation", "Swagger UI"])
    )

    cats = [
        Category("Frontend", 58, 122, 335, 475, "#ECFDF5", "#A7F3D0", frontend),
        Category("Backend", 416, 122, 380, 475, "#EFF6FF", "#BFDBFE", backend),
        Category("Authentication", 820, 122, 350, 475, "#FEFCE8", "#FDE68A", auth),
        Category("AI / Prompt", 1194, 122, 668, 475, "#F5F3FF", "#DDD6FE", ai),
        Category("Database", 58, 636, 360, 360, "#FFF7ED", "#FDBA74", database),
        Category("Infra / Runtime", 442, 636, 420, 360, "#F8FAFC", "#CBD5E1", infra),
        Category("Testing", 886, 636, 370, 360, "#FDF2F8", "#FBCFE8", testing),
        Category("API Docs", 1280, 636, 310, 360, "#F0FDFA", "#99F6E4", api_docs),
    ]
    category_markup = "\n".join(category(cat) for cat in cats)

    flow = """
      <g class="flow-row">
        <rect x="58" y="1010" width="1804" height="56" rx="22" fill="#FFFFFF" stroke="#E2E8F0" stroke-width="1.8"/>
        <text class="flow-node" x="210" y="1045" text-anchor="middle">Local Developer</text>
        <path class="arrow" d="M330 1038 L475 1038"/>
        <text class="flow-node" x="600" y="1045" text-anchor="middle">GitHub Repository</text>
        <path class="arrow" d="M740 1038 L890 1038"/>
        <text class="flow-node" x="1015" y="1045" text-anchor="middle">Docker Compose</text>
        <path class="arrow" d="M1155 1038 L1315 1038"/>
        <text class="flow-node" x="1535" y="1045" text-anchor="middle">Vue Client / Spring Boot API / MySQL</text>
      </g>
    """

    return f"""<svg xmlns="http://www.w3.org/2000/svg" width="{WIDTH}" height="{HEIGHT}" viewBox="0 0 {WIDTH} {HEIGHT}" role="img" aria-labelledby="title desc">
  <title id="title">ARENA Tech Stack</title>
  <desc id="desc">ARENA project technology stack slide with frontend, backend, authentication, AI, database, runtime, testing, and API docs.</desc>
  <defs>
    <style>
      .slide-bg {{ fill: #F8FAFC; }}
      text {{ font-family: Arial, "Noto Sans KR", sans-serif; letter-spacing: 0; }}
      .headline {{ font-weight: 900; font-size: 50px; fill: #111827; }}
      .subhead {{ font-weight: 700; font-size: 20px; fill: #475569; }}
      .category-title {{ font-weight: 900; font-size: 27px; fill: #111827; }}
      .tech-label {{ font-weight: 900; font-size: 15px; fill: #111827; }}
      .version {{ font-weight: 750; font-size: 11px; fill: #64748B; }}
      .badge-text {{ font-weight: 850; font-size: 13px; fill: #1F2937; }}
      .desc {{ font-weight: 800; font-size: 14px; fill: #475569; }}
      .flow-node {{ font-weight: 900; font-size: 17px; fill: #111827; }}
      .arrow {{ stroke: #94A3B8; stroke-width: 3; marker-end: url(#arrow-head); }}
      .badge {{ stroke-width: 1.5; }}
      svg path {{ shape-rendering: geometricPrecision; }}
    </style>
    <marker id="arrow-head" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
      <path d="M0,0 L0,6 L9,3 z" fill="#94A3B8"/>
    </marker>
  </defs>
  <rect class="slide-bg" x="0" y="0" width="{WIDTH}" height="{HEIGHT}"/>
  <text class="headline" x="70" y="72">ARENA Tech Stack</text>
  <text class="subhead" x="70" y="110">Vue 3 · Spring Boot 3 · MySQL · Docker Compose · Spring AI</text>
  {category_markup}
  {flow}
</svg>
"""


def source_json() -> dict:
    return {
        "title": "ARENA Tech Stack",
        "subtitle": "Vue 3 · Spring Boot 3 · MySQL · Docker Compose · Spring AI",
        "canvas": {"width": WIDTH, "height": HEIGHT, "ratio": "16:9"},
        "categories": [
            {
                "title": "Frontend",
                "technologies": ["Vue 3.5", "Vite 6", "Pinia 3", "Vue Router 4", "Axios", "Naive UI"],
                "descriptions": ["SPA UI", "Routing", "State Management", "REST API Client"],
            },
            {
                "title": "Backend",
                "technologies": ["Java 17", "Spring Boot 3.5", "Spring Security", "Spring Validation", "Spring Web MVC", "MyBatis 3.0.5", "Lombok"],
                "descriptions": ["REST API", "Auth / Authorization", "Business Logic", "SQL Mapper"],
            },
            {
                "title": "Authentication",
                "technologies": ["Kakao OAuth", "JWT", "JJWT 0.12.6", "Spring Security"],
                "descriptions": ["Kakao Login", "Access Token", "Protected API"],
            },
            {
                "title": "AI / Prompt",
                "technologies": ["Spring AI 1.0.0", "Spring AI OpenAI starter", "GMS OpenAI-compatible API", "gpt-5.4-mini", "Prompt Template", "JSON Response Parser"],
                "descriptions": ["Topic Candidates", "Debate Turns", "Summary", "JSON Parsing"],
            },
            {
                "title": "Database",
                "technologies": ["MySQL 8.4", "MySQL Connector/J", "MyBatis Mapper XML"],
                "descriptions": ["User Data", "Debate Data", "Post / Comment / Vote", "AI Logs"],
            },
            {
                "title": "Infra / Runtime",
                "technologies": ["Docker", "Docker Compose", "Node 22 Alpine", "Gradle 8.14", "Eclipse Temurin 17 JRE"],
                "descriptions": ["arena-client", "arena-app", "arena-mysql", "docker compose up"],
            },
            {
                "title": "Testing",
                "technologies": ["Vitest 4", "Vue Test Utils", "jsdom", "JUnit 5", "Spring Boot Test", "Spring Security Test", "MyBatis Test"],
                "descriptions": ["Frontend Test", "Backend Test", "Security Test", "MyBatis Test"],
            },
            {
                "title": "API Docs",
                "technologies": ["Springdoc OpenAPI", "Swagger UI"],
                "descriptions": ["REST API Documentation", "Swagger UI"],
            },
        ],
        "bottomFlow": ["Local Developer", "GitHub Repository", "Docker Compose", "Vue Client / Spring Boot API / MySQL"],
    }


def icon_sources_md() -> str:
    rows = [
        ("Technology", "Icon source", "Notes"),
        ("---", "---", "---"),
    ]
    for _, label, url in ICON_SOURCES.values():
        rows.append((label, url, "Simple Icons CDN, embedded as SVG path"))
    rows.extend([
        ("Vue Router", "Text badge", "No Simple Icons logo used"),
        ("Naive UI", "Text badge", "Official icon ambiguous for this slide"),
        ("Spring Validation", "Text badge", "Spring module label"),
        ("Spring Web MVC", "Text badge", "Spring module label"),
        ("MyBatis", "Text badge", "No Simple Icons logo available"),
        ("Lombok", "Text badge", "No Simple Icons logo used"),
        ("GMS OpenAI-compatible API", "Text badge", "Labeled explicitly to avoid implying direct OpenAI service use"),
        ("Prompt Template", "Text badge", "Project concept"),
        ("JSON Response Parser", "Text badge", "Project concept"),
        ("MySQL Connector/J", "Text badge", "Driver label"),
        ("MyBatis Mapper XML", "Text badge", "Persistence implementation label"),
        ("Springdoc OpenAPI", "Text badge", "Library label"),
    ])
    return "# ARENA Tech Stack Icon Sources\n\n" + "\n".join(
        f"| {a} | {b} | {c} |" for a, b, c in rows
    ) + "\n"


def main() -> None:
    svg = build_svg()
    OUT_SVG.write_text(svg, encoding="utf-8")
    OUT_SOURCE.write_text(json.dumps(source_json(), ensure_ascii=False, indent=2), encoding="utf-8")
    OUT_SOURCES.write_text(icon_sources_md(), encoding="utf-8")

    try:
        import cairosvg

        cairosvg.svg2png(bytestring=svg.encode("utf-8"), write_to=str(OUT_PNG), output_width=3840, output_height=2160)
    except Exception as exc:
        print(f"PNG_RENDER_FAILED: {exc}")
        raise

    print(f"Wrote {OUT_SVG}")
    print(f"Wrote {OUT_PNG}")
    print(f"Wrote {OUT_SOURCE}")
    print(f"Wrote {OUT_SOURCES}")


if __name__ == "__main__":
    main()
