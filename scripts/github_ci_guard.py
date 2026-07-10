#!/usr/bin/env python3
from __future__ import annotations

import os
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
    ".gitignore",
    ".github/workflows/main.yml",
    "scripts/java_syntax_guard.py",
    "scripts/github_static_audit.py",
    "scripts/tc4_v11_62_2_integrated_server_world_load_hotfix_audit.py",
    "scripts/tc4_v11_62_3_nitor_runtime_parity_audit.py",
    "scripts/tc4_v11_62_4_thaumonomicon_markup_audit.py",
    "scripts/tc4_v11_62_5_thaumonomicon_recipe_cards_audit.py",
    "scripts/tc4_v11_62_6_thaumonomicon_tooltip_scissor_audit.py",
    "scripts/tc4_v11_62_7_magic_tree_visual_shape_audit.py",
    "scripts/tc4_v11_62_8_essentia_jar_visual_runtime_audit.py",
    "scripts/tc4_v11_62_10_revealing_jar_gui_audit.py",
    "scripts/tc4_v11_62_11_arcane_workbench_original_parity_audit.py",
    "scripts/tc4_v11_62_12_wand_node_tapping_parity_audit.py",
]

REQUIRED_WORKFLOW_SNIPPETS = [
    "actions/checkout@v4",
    "actions/setup-python@v5",
    "actions/setup-java@v4",
    "distribution: temurin",
    "java-version: \"17\"",
    "gradle/actions/setup-gradle@v4",
    "chmod +x ./gradlew",
    "./gradlew --no-daemon clean build verifyJarResources verifyGithubOutputJarResources copyGithubOutputJar --stacktrace",
    "python scripts/java_syntax_guard.py",
    "python scripts/github_static_audit.py",
    "python scripts/tc4_v11_62_8_essentia_jar_visual_runtime_audit.py",
    "python scripts/tc4_v11_62_10_revealing_jar_gui_audit.py",
    "python scripts/tc4_v11_62_11_arcane_workbench_original_parity_audit.py",
    "python scripts/tc4_v11_62_12_wand_node_tapping_parity_audit.py",
    "actions/upload-artifact@v4",
    "build/libs/*-github.jar",
]

errors: list[str] = []

for rel in REQUIRED_FILES:
    if not (ROOT / rel).exists():
        errors.append(f"missing GitHub build file: {rel}")

workflow_text = WORKFLOW.read_text(encoding="utf-8") if WORKFLOW.exists() else ""
for snippet in REQUIRED_WORKFLOW_SNIPPETS:
    if snippet not in workflow_text:
        errors.append(f"workflow missing required snippet: {snippet}")

# ZIP uploads through GitHub can lose Unix mode bits. The workflow must restore
# gradlew's executable bit before this guard checks it.
chmod_pos = workflow_text.find("chmod +x ./gradlew")
guard_pos = workflow_text.find("python scripts/github_ci_guard.py")
if chmod_pos >= 0 and guard_pos >= 0 and chmod_pos > guard_pos:
    errors.append("workflow must chmod gradlew before running github_ci_guard.py")

if "build/libs/*.jar" in workflow_text and "build/libs/*-github.jar" not in workflow_text:
    errors.append("workflow uploads all jars instead of only the playable *-github.jar")

wrapper_props = ROOT / "gradle/wrapper/gradle-wrapper.properties"
if wrapper_props.exists() and "gradle-7.5.1-bin.zip" not in wrapper_props.read_text(encoding="utf-8"):
    errors.append("Gradle wrapper should be pinned to gradle-7.5.1-bin.zip for Forge 1.19.2")

build_gradle = ROOT / "build.gradle"
if build_gradle.exists():
    build_text = build_gradle.read_text(encoding="utf-8")
    if "version '5.1.76'" not in build_text:
        errors.append("ForgeGradle must be pinned to 5.1.76")
    if "1.19.2-43.5.2" not in build_text:
        errors.append("Forge dependency should stay pinned to 1.19.2-43.5.2")
    if "version = '11.62.12'" not in build_text:
        errors.append("Project version should be 11.62.12")

mods_toml = ROOT / "src/main/resources/META-INF/mods.toml"
if mods_toml.exists() and 'version="11.62.12"' not in mods_toml.read_text(encoding="utf-8"):
    errors.append('mods.toml should be version="11.62.12"')

if os.name != "nt":
    gradlew = ROOT / "gradlew"
    if gradlew.exists() and not os.access(gradlew, os.X_OK):
        errors.append("gradlew must be executable before the GitHub CI guard runs")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)

print("GitHub CI guard: OK")
