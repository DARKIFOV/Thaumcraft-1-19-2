# Thaumcraft Legacy Rebuild v11.62.38

## Research Table single-GUI fix

This batch removes the remaining duplicate, client-only Research Table screen.

The actual Research Table already opens a server-backed `ResearchTableMenu` through `NetworkHooks.openScreen`. However, an older `OpenResearchTablePacket` and `ClientHooks.openResearchTable()` path still instantiated a plain `ResearchTableScreen` with no menu id, no block position, no table inventory and no note synchronization. That path could display a second GUI which looked partially correct but could not use the real table slots or puzzle state.

### Changes

- Deleted the obsolete `ResearchTableScreen.java`.
- Removed `ClientHooks.openResearchTable()`.
- Removed the unused server helper that sent `OpenResearchTablePacket`.
- Kept the legacy packet discriminator registered so later packet ids do not shift, but made its handler an intentional no-op.
- Added `tools/research_table_open_guard.py`.
- Added the new guard to both GitHub build and release workflows.
- Preserved the real integrated `ResearchTableContainerScreen` and `NetworkHooks.openScreen` path.

### Verification

- Forge-only guard: passed.
- Java source guard: passed.
- Research Table integration guard: passed.
- Research Table open guard: passed.
- JSON resource validation: 1697 files passed.
- Magical Forest worldgen guard: passed.
- TC4 visual parity guard: passed.
- Registry audit: 689 item models, 0 visible clone leaks, 0 resource problems.

### Build status

A local Gradle build could not start because the sandbox cannot resolve `services.gradle.org`. GitHub Actions remains configured to compile the Forge 1.19.2 project on Java 17.
