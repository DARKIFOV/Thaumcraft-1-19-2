#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/main.yml"

REQUIRED_FILES = [
    "build.gradle",
    "settings.gradle",
    "gradle.properties",
    "gradlew",
    "gradlew.bat",
    "gradle/wrapper/gradle-wrapper.jar",
    "gradle/wrapper/gradle-wrapper.properties",
    ".gitattributes",
    ".github/workflows/main.yml",
]

REQUIRED_WORKFLOW_SNIPPETS = [
    "actions/checkout@v4",
    "actions/setup-java@v4",
    "distribution: temurin",
    "java-version: \"17\"",
    "gradle/actions/setup-gradle@v4",
    "chmod +x ./gradlew",
    "./gradlew --no-daemon clean build --stacktrace",
    "actions/upload-artifact@v4",
    "python scripts/java_syntax_guard.py",
    "python scripts/github_static_audit.py",
    "python scripts/tc4_stage139_whole_port_sweep_audit.py",
    "python scripts/tc4_stage140_golemancy_checkpoint_audit.py",
    "python scripts/tc4_stage141_golemancy_controls_audit.py",
    "python scripts/tc4_stage142_golemancy_gui_render_audit.py",
    "python scripts/tc4_stage143_golemancy_whole_sweep_audit.py",
    "python scripts/tc4_stage144_eldritch_warp_taint_audit.py",
]

errors: list[str] = []

for rel in REQUIRED_FILES:
    if not (ROOT / rel).exists():
        errors.append(f"missing GitHub build file: {rel}")

workflow_text = WORKFLOW.read_text(encoding="utf-8") if WORKFLOW.exists() else ""
for snippet in REQUIRED_WORKFLOW_SNIPPETS:
    if snippet not in workflow_text:
        errors.append(f"workflow missing required snippet: {snippet}")

wrapper_props = ROOT / "gradle/wrapper/gradle-wrapper.properties"
if wrapper_props.exists():
    props = wrapper_props.read_text(encoding="utf-8")
    if "gradle-7.5.1-bin.zip" not in props:
        errors.append("Gradle wrapper should be pinned to gradle-7.5.1-bin.zip for this Forge 1.19.2 stage")

build_gradle = ROOT / "build.gradle"
if build_gradle.exists():
    build_text = build_gradle.read_text(encoding="utf-8")
    if "version '5.1.76'" not in build_text:
        errors.append("ForgeGradle must be pinned to 5.1.76 instead of dynamic 5.1.+")
    if "1.19.2-43.5.2" not in build_text:
        errors.append("Forge dependency should stay pinned to 1.19.2-43.5.2")
    if "version = '1.44.0'" not in build_text:
        errors.append("Project version should be 1.44.0 for Stage144")

settings_gradle = ROOT / "settings.gradle"
if settings_gradle.exists():
    settings_text = settings_gradle.read_text(encoding="utf-8")
    if "https://maven.minecraftforge.net/" not in settings_text:
        errors.append("settings.gradle missing Forge Maven plugin repository")
    if "https://maven.neoforged.net/releases/" not in settings_text:
        errors.append("settings.gradle missing NeoForged releases fallback repository")
    if "useModule(\"net.minecraftforge.gradle:ForgeGradle:${requested.version}\")" not in settings_text:
        errors.append("settings.gradle missing ForgeGradle plugin resolutionStrategy")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("GitHub CI guard: OK")
