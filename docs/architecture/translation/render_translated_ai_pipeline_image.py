from __future__ import annotations

from pathlib import Path
from textwrap import wrap

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parent
INPUT = ROOT / "arena-ai-pipeline-original.png"
OUTPUT = ROOT / "arena-ai-pipeline-ko.png"

FONT_REGULAR = Path("C:/Windows/Fonts/NotoSansKR-VF.ttf")
FONT_BOLD = Path("C:/Windows/Fonts/malgunbd.ttf")


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONT_BOLD if bold else FONT_REGULAR), size)


def cover(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], fill: str = "#FFFFFF", outline: str | None = None, radius: int = 10) -> None:
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline)


def center_text(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    size: int,
    fill: str = "#111827",
    bold: bool = False,
    line_spacing: int = 4,
) -> None:
    x1, y1, x2, y2 = xy
    fnt = font(size, bold)
    lines = text.split("\n")
    heights = [draw.textbbox((0, 0), line, font=fnt)[3] for line in lines]
    total_h = sum(heights) + line_spacing * (len(lines) - 1)
    y = y1 + ((y2 - y1) - total_h) / 2
    for line, h in zip(lines, heights):
        bbox = draw.textbbox((0, 0), line, font=fnt)
        x = x1 + ((x2 - x1) - (bbox[2] - bbox[0])) / 2
        draw.text((x, y), line, font=fnt, fill=fill)
        y += h + line_spacing


def left_text(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    text: str,
    size: int,
    fill: str = "#111827",
    bold: bool = False,
    line_spacing: int = 4,
) -> None:
    x1, y1, _, _ = xy
    fnt = font(size, bold)
    y = y1
    for line in text.split("\n"):
        draw.text((x1, y), line, font=fnt, fill=fill)
        bbox = draw.textbbox((0, 0), line, font=fnt)
        y += (bbox[3] - bbox[1]) + line_spacing


def main() -> None:
    img = Image.open(INPUT).convert("RGBA")
    draw = ImageDraw.Draw(img)

    # Left browser panel
    cover(draw, (66, 137, 210, 166), "#F8FAFC")
    center_text(draw, (66, 137, 210, 166), "사용자 브라우저", 20, bold=True)
    replacements = [
        ((78, 284, 220, 315), "주제 입력", "#FFFFFF", 15, "#111827"),
        ((78, 334, 222, 366), "세부 주제 선택", "#FFFFFF", 15, "#111827"),
        ((78, 385, 222, 417), "토론 진행", "#FFFFFF", 15, "#111827"),
        ((78, 435, 222, 468), "결과 확인", "#FFFFFF", 15, "#111827"),
        ((246, 210, 325, 255), "주제 입력 /\n선택", "#FFFFFF", 13, "#111827"),
        ((178, 545, 275, 575), "결과 렌더링", "#FFFFFF", 14, "#111827"),
    ]

    # Vue panel descriptions
    replacements += [
        ((397, 300, 505, 331), "UI 렌더링", "#F4FFF9", 14, "#111827"),
        ((397, 351, 508, 382), "REST API 호출", "#F4FFF9", 14, "#111827"),
        ((390, 389, 512, 431), "토론 화면", "#F4FFF9", 14, "#111827"),
        ((390, 438, 512, 483), "결과 화면", "#F4FFF9", 14, "#111827"),
        ((532, 210, 625, 272), "REST API\n요청", "#FFFFFF", 13, "#111827"),
        ((520, 420, 625, 463), "후보 / 토론 /\n결과 반환", "#FFFFFF", 12, "#111827"),
    ]

    # Spring inner modules
    replacements += [
        ((646, 379, 710, 453), "인증 /\nAPI", "#EEF6FF", 15, "#0F3FAE"),
        ((752, 379, 823, 453), "AI\n파이프라인", "#EEF6FF", 15, "#0F3FAE"),
        ((846, 379, 927, 453), "비즈니스\n로직", "#EEF6FF", 15, "#0F3FAE"),
        ((970, 379, 1042, 453), "DB\n저장", "#EEF6FF", 15, "#0F3FAE"),
    ]

    # GMS arrows and labels
    replacements += [
        ((1050, 206, 1182, 253), "후보 생성 /\n토론 / 요약", "#FFFFFF", 12, "#111827"),
        ((1068, 269, 1168, 294), "프롬프트 요청", "#FFFFFF", 13, "#0057B8"),
        ((1072, 373, 1168, 399), "AI 응답", "#FFFFFF", 13, "#0057B8"),
        ((1234, 344, 1364, 374), "세부 주제 후보", "#F6F0FF", 14, "#111827"),
        ((1234, 394, 1364, 425), "토론 발화", "#F6F0FF", 14, "#111827"),
        ((1234, 445, 1364, 475), "요약", "#F6F0FF", 14, "#111827"),
    ]

    # Result page
    replacements += [
        ((1470, 187, 1598, 213), "결과 페이지", "#F8FAFC", 22, "#374151"),
        ((1438, 266, 1588, 297), "토론 결과", "#FFFFFF", 18, "#111827"),
        ((1438, 304, 1525, 330), "요약", "#FFFFFF", 13, "#111827"),
        ((1445, 376, 1525, 400), "선택 A", "#FFFFFF", 12, "#16A34A"),
        ((1560, 376, 1630, 400), "선택 B", "#FFFFFF", 12, "#DC2626"),
        ((1438, 492, 1575, 518), "핵심 요약", "#FFFFFF", 13, "#111827"),
    ]

    # MySQL and Kakao
    replacements += [
        ((676, 486, 774, 528), "토론 데이터\n저장 / 조회", "#FFFFFF", 13, "#111827"),
        ((984, 487, 1070, 529), "로그인 /\n사용자 정보", "#FFFFFF", 13, "#111827"),
        ((720, 550, 865, 579), "사용자 입력", "#FFFDF8", 14, "#111827"),
        ((720, 575, 865, 604), "AI 원문 응답", "#FFFDF8", 14, "#111827"),
        ((720, 600, 865, 629), "토론 발화", "#FFFDF8", 14, "#111827"),
        ((720, 625, 865, 654), "라운드 요약", "#FFFDF8", 14, "#111827"),
        ((720, 650, 865, 678), "게시글", "#FFFDF8", 14, "#111827"),
        ((972, 650, 1084, 678), "소셜 로그인", "#FFF8E8", 14, "#111827"),
    ]

    for xy, label, bg, size, color in replacements:
        cover(draw, xy, bg)
        center_text(draw, xy, label, size, color, bold=True if size >= 15 else False)

    # Bottom pipeline title
    cover(draw, (696, 704, 976, 738), "#FFFFFF")
    center_text(draw, (696, 704, 976, 738), "AI 토론 파이프라인", 26, "#5B21B6", True)

    # Bottom pipeline steps
    step_specs = [
        ((86, 763, 266, 802), "1단계: 주제 입력", "#F7FBFF", "#155BB7"),
        ((83, 802, 266, 846), "사용자가 원본\n주제를 입력", "#F7FBFF", "#111827"),
        ((347, 763, 558, 802), "2단계: 세부 주제 후보", "#F7FFFC", "#0F766E"),
        ((347, 802, 558, 846), "AI가 토론 가능한\n세부 주제 후보 생성", "#F7FFFC", "#111827"),
        ((612, 763, 797, 802), "3단계: 토론 발화", "#FBF7FF", "#5B21B6"),
        ((612, 802, 797, 846), "AI가 양측 발화\n10개 생성", "#FBF7FF", "#111827"),
        ((879, 763, 1078, 807), "4단계: 요약 /\n사용자 선택", "#FFF7FB", "#BE1255"),
        ((879, 807, 1078, 855), "사용자가 진영 선택\nAI가 요약 생성", "#FFF7FB", "#111827"),
        ((1150, 763, 1358, 802), "5단계: 토론 데이터 저장", "#FFF8F0", "#C2410C"),
        ((1150, 802, 1358, 846), "생성된 토론과 요약을\nDB에 저장", "#FFF8F0", "#111827"),
        ((1412, 763, 1600, 802), "6단계: 결과 페이지", "#F7FBFF", "#155BB7"),
        ((1412, 802, 1600, 846), "저장된 토론 기록과\n요약 표시", "#F7FBFF", "#111827"),
    ]
    for xy, label, bg, color in step_specs:
        cover(draw, xy, bg)
        center_text(draw, xy, label, 13 if "\n" in label else 14, color, bold=True if "단계" in label else False)

    img.convert("RGB").save(OUTPUT, quality=95)
    print(OUTPUT)


if __name__ == "__main__":
    main()
