#!/usr/bin/env python3
"""Compatibility alias for Stage204 jar/tube transfer edge-case audit.

The canonical Stage204 audit is tc4_stage204_jar_tube_edge_cases_audit.py.
This alias keeps older continuation prompts and CI snippets working while the
implementation remains in one strict parity audit.
"""
import runpy
from pathlib import Path

runpy.run_path(str(Path(__file__).with_name("tc4_stage204_jar_tube_edge_cases_audit.py")), run_name="__main__")
