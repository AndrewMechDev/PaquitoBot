# SVG to Android Vector Drawable — Conversion Guide

## Template

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:pathData="M15 7L10 12L15 17"
        android:strokeColor="#000000"
        android:strokeWidth="1.5"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="#00000000"/>
</vector>
```

## Viewport Mapping

The SVG `viewBox` maps directly to viewport dimensions:

- `viewBox="0 0 24 24"` → `viewportWidth="24"` + `viewportHeight="24"`
- `viewBox="0 0 64 64"` → `viewportWidth="64"` + `viewportHeight="64"`
- `width` and `height` in the `<vector>` tag are always in `dp` and typically match the desired display size (usually `24dp`).

## Multi-Path Icons

Icons with multiple SVG `<path>` elements become multiple `<path>` elements inside the `<vector>`:

```xml
<vector ...>
    <path android:pathData="..." android:fillColor="#2F3750"/>
    <path android:pathData="..." android:fillColor="#262C42"/>
    <path android:pathData="..." android:fillColor="#F4D08C"/>
</vector>
```

## Circle Conversion

SVG `<circle>` has no direct equivalent. Convert to an arc path:

```
SVG:  <circle cx="12" cy="12" r="10"/>
XML:  android:pathData="M2,12 a10,10 0 1,0 20,0 a10,10 0 1,0 -20,0"
```

Formula: `M (cx-r),(cy) a (r),(r) 0 1,0 (2*r),0 a (r),(r) 0 1,0 -(2*r),0`

## Line Conversion

```
SVG:  <line x1="5" y1="12" x2="19" y2="12"/>
XML:  android:pathData="M5,12 L19,12"
```

## Rectangle Conversion

```
SVG:  <rect x="3" y="3" width="18" height="18" rx="2"/>
XML:  android:pathData="M5,3 h14 a2,2 0 0,1 2,2 v14 a2,2 0 0,1 -2,2 h-14 a2,2 0 0,1 -2,-2 v-14 a2,2 0 0,1 2,-2 Z"
```

## Stroke-Only Icons (No Fill)

When an SVG uses `fill="none"` with only strokes:

```xml
<path
    android:pathData="..."
    android:strokeColor="#000000"
    android:strokeWidth="1.5"
    android:fillColor="#00000000"/>
```

## Unsupported SVG Features

These require manual workarounds or simplification:

| Feature | Workaround |
|---|---|
| `<linearGradient>` | Use `<aapt:attr>` with `<gradient>` (API 24+) or flatten to solid color |
| `<radialGradient>` | Same as above |
| `<filter>` / blur | Remove — not supported |
| `<mask>` / `<clipPath>` | Convert to path intersection manually |
| `<text>` | Convert text to path outlines |
| CSS `style` attribute | Inline each property as `android:` attribute |
| `opacity` on element | Use alpha channel in color: `#80FF0000` for 50% red |

## File Location

Place converted files in: `composeApp/src/commonMain/composeResources/drawable/`

Naming convention PaquitoBot: `ic_paquito_{name}.xml` (lowercase, underscores).

Note: the previous version of this skill referenced `sharedUI/src/commonMain/composeResources/drawable/`, which was the path layout of a different project (`AlphaKids-KMP`). PaquitoBot does NOT have a `sharedUI` module — UI is native per platform — so Android draws live under `composeApp/`.
