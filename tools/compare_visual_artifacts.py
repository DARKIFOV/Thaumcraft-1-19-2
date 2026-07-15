#!/usr/bin/env python3
"""Create reproducible TC4-vs-port image difference evidence.

This is a diagnostic, not an automatic visual PASS. It requires equal image
sizes by default, writes an amplified absolute-difference PNG and a JSON file
with pixel metrics and source SHA-256 hashes.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

import numpy as np
from PIL import Image, ImageChops, ImageEnhance


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def global_ssim(a: np.ndarray, b: np.ndarray) -> float:
    # Global SSIM is intentionally dependency-free. It is a triage signal;
    # side-by-side human review remains mandatory for PASS.
    a = a.astype(np.float64)
    b = b.astype(np.float64)
    mu_a, mu_b = a.mean(), b.mean()
    var_a, var_b = a.var(), b.var()
    cov = ((a - mu_a) * (b - mu_b)).mean()
    c1 = (0.01 * 255.0) ** 2
    c2 = (0.03 * 255.0) ** 2
    return float(((2 * mu_a * mu_b + c1) * (2 * cov + c2)) /
                 ((mu_a ** 2 + mu_b ** 2 + c1) * (var_a + var_b + c2)))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("original", type=Path)
    parser.add_argument("port", type=Path)
    parser.add_argument("--diff", type=Path, required=True)
    parser.add_argument("--report", type=Path, required=True)
    parser.add_argument("--allow-resize", action="store_true")
    parser.add_argument("--amplify", type=float, default=4.0)
    args = parser.parse_args()

    original = Image.open(args.original).convert("RGBA")
    port = Image.open(args.port).convert("RGBA")
    resized = False
    if original.size != port.size:
        if not args.allow_resize:
            raise SystemExit(f"image size mismatch: original={original.size}, port={port.size}")
        port = port.resize(original.size, Image.Resampling.LANCZOS)
        resized = True

    a = np.asarray(original, dtype=np.float64)
    b = np.asarray(port, dtype=np.float64)
    delta = np.abs(a - b)
    rgb_delta = delta[..., :3]
    metrics = {
        "original": str(args.original),
        "port": str(args.port),
        "original_sha256": sha256(args.original),
        "port_sha256": sha256(args.port),
        "size": list(original.size),
        "port_resized": resized,
        "mae_rgb": float(rgb_delta.mean()),
        "rmse_rgb": float(np.sqrt(np.square(rgb_delta).mean())),
        "max_abs_difference": int(delta.max()),
        "exact_pixel_ratio_rgba": float(np.all(a == b, axis=2).mean()),
        "global_ssim_luma": global_ssim(
            np.asarray(original.convert("L")), np.asarray(port.convert("L"))
        ),
        "interpretation": "Diagnostic only; human side-by-side review is required for runtime visual PASS.",
    }

    args.diff.parent.mkdir(parents=True, exist_ok=True)
    args.report.parent.mkdir(parents=True, exist_ok=True)
    diff = ImageChops.difference(original, port)
    diff = ImageEnhance.Brightness(diff).enhance(max(1.0, args.amplify))
    diff.save(args.diff)
    args.report.write_text(json.dumps(metrics, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(metrics, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
