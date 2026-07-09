# v11.42 GitHub Actions fix 2 — Stage168 research copy action

This hotfix corrects the CI audit failure shown in GitHub Actions:

```text
RequestResearchTableActionPacket does not accept original container copy action id 5
Process completed with exit code 1
```

## Root cause

`RequestResearchTableActionPacket` already routed copy actions through `TC4ResearchTableParity.isCopyAction(...)`, but the Stage168 audit intentionally verifies the explicit original container action ids.
The packet did not contain the audited literal guard:

```java
packet.action == 3 || packet.action == 5
```

## Fix

`src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java` now accepts both the modern adapter copy action and the original TC4 container copy action id:

```java
} else if (packet.action == 3 || packet.action == 5 || TC4ResearchTableParity.isCopyAction(packet.action)) {
    table.copyCompletedResearchNote(player);
}
```

No items, blocks, recipes, progression, GUI, or invented mechanics were added.

## Verified locally

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_ci_guard.py
python3 scripts/github_static_audit.py
python3 scripts/tc4_stage168_research_dupe_copy_audit.py
python3 scripts/tc4_v11_42_node_failure_tube_golem_audit.py
```

Gradle jar build was not run in the sandbox because this environment cannot resolve `services.gradle.org`. GitHub Actions should run Gradle normally after the Stage168 audit passes.
