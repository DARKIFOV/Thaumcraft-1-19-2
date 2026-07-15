#!/usr/bin/env python3
"""Static source/network contract guard for v11.62.84 TC4 Warp/Eldritch parity."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def text(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


checks: list[tuple[str, bool]] = []


def check(name: str, ok: bool) -> None:
    checks.append((name, bool(ok)))


build = text("build.gradle")
mods = text("src/main/resources/META-INF/mods.toml")
data = text("src/main/java/com/darkifov/thaumcraft/data/PlayerThaumData.java")
warp = text("src/main/java/com/darkifov/thaumcraft/event/WarpEvents.java")
soap = text("src/main/java/com/darkifov/thaumcraft/block/SanitySoapItem.java")
fluid = text("src/main/java/com/darkifov/thaumcraft/block/PurifyingFluidBlock.java")
talisman = text("src/main/java/com/darkifov/thaumcraft/block/WarpWardTalismanItem.java")
packet = text("src/main/java/com/darkifov/thaumcraft/network/ResearchSyncPacket.java")
network = text("src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java")
client = text("src/main/java/com/darkifov/thaumcraft/client/ClientResearchData.java")
manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))

check("build version 11.62.84", "version = '11.62.84'" in build)
check("mods version 11.62.84", 'version="11.62.84"' in mods)
check("warp check interval 2000", "CHECK_INTERVAL = 2000" in warp)
check("death gaze interval ten", "player.tickCount % 10 == 0" in warp)
check("event roll sqrt counter", "roll <= Math.sqrt(warpCounter)" in warp)
check("temporary decay after unwarded check", "PlayerThaumData.decayTemporaryWarp(player, 1)" in warp)
check("separate sticky event decay API", "decayStickyWarpFromEvent" in data)
check("rare cleanse uses non-reset path", "PlayerThaumData.decayStickyWarpFromEvent(player, 1)" in warp)
check("sticky event decay does not reset counter", "decayStickyWarpFromEvent" in data and "resetWarpCounterToTotal" not in data[data.find("decayStickyWarpFromEvent"):data.find("private static void resetWarpCounterToTotal")])

check("legacy ward migration consumer", "takeLegacyWarpWardTicks" in data and "root.remove(WARP_WARD_TICKS)" in data)
check("legacy ward migrates to MobEffect", "int legacyWard = PlayerThaumData.takeLegacyWarpWardTicks(player)" in warp and "new MobEffectInstance" in warp)
check("runtime ward authority is effect only", "return player.hasEffect(ThaumcraftMod.WARP_WARD.get());" in warp)
check("soap bonus checks effect only", "player.hasEffect(ThaumcraftMod.WARP_WARD.get())" in soap and "PlayerThaumData.hasWarpWard" not in soap)
check("pure fluid checks effect only", "player.hasEffect(ThaumcraftMod.WARP_WARD.get())" in fluid and "PlayerThaumData.hasWarpWard" not in fluid)
check("pure fluid no duplicate persistent timer", "setWarpWardTicks" not in fluid)
check("talisman no duplicate persistent timer", "addWarpWard" not in talisman and "setWarpWardTicks" not in talisman)

check("spawn search returns Optional", "Optional<BlockPos> findSpawnAround" in warp)
check("spawn search uses fifty attempts", "for (int i = 0; i < 50; i++)" in warp)
check("tc4 signed offset includes zero", "Mth.nextInt(player.getRandom(), -1, 1)" in warp)
check("tc4 three-axis magnitude", warp.count("randomTc4AxisOffset(player, min, max)") == 3)
check("spawn requires sturdy support", "isFaceSturdy(level, pos.below(), Direction.UP)" in warp)
check("spawn requires two air blocks", "getBlockState(pos).isAir()" in warp and "getBlockState(pos.above()).isAir()" in warp)
check("spawn rejects fluids", "getFluidState(pos).isEmpty()" in warp and "getFluidState(pos.above()).isEmpty()" in warp)
check("spawn checks entity collision", "level.noCollision(entity)" in warp)
check("guardian skips invalid spawn", "Optional<BlockPos> spawn = findSpawnAround(player, guardian" in warp and "if (spawn.isEmpty())" in warp)
check("mind spider skips invalid spawn", "Optional<BlockPos> spawn = findSpawnAround(player, spider" in warp)
check("no fallback forced spawn", "return Optional.empty();" in warp and "base.offset(2, 0, 2)" not in warp)

check("death gaze 0.75 forward cone", ">= 0.75D" in warp and "getLookAngle" in warp)
check("death gaze line of sight", "player.hasLineOfSight(target)" in warp)
check("death gaze respects pvp", "!player.getServer().isPvpAllowed()" in warp)
check("death gaze records attacker", "target.setLastHurtByPlayer(player)" in warp)
check("death gaze mobs aggro", "mob.setTarget(player)" in warp)
check("death gaze wither duration 80", "new MobEffectInstance(MobEffects.WITHER, 80, 0)" in warp)

for field in ("totalWarp", "permanentWarp", "stickyWarp", "temporaryWarp", "warpCounter"):
    check(f"packet field {field}", f"private final int {field};" in packet)
check("packet writes five warp values", packet.count("buffer.writeVarInt(packet.") == 6)
check("packet reads five warp values", packet.count("buffer.readVarInt()") >= 6)  # research size + five values
check("network sends all warp buckets", all(token in network for token in (
    "getWarpTotal(player)", "getWarpPerm(player)", "getWarpSticky(player)",
    "getWarpTemporary(player)", "getWarpCounter(player)")))
for getter in ("permanentWarp", "stickyWarp", "temporaryWarp", "warpCounter"):
    check(f"client getter {getter}", f"public static int {getter}()" in client)

required = {
    "warp.buckets_counter_effect_only_ward_migration",
    "warp.guardian_mind_spider_valid_spawn_search",
    "warp.death_gaze_forward_cone_pvp_aggro",
}
ids = {entry.get("id") for entry in manifest.get("tests", [])}
check("manifest version 11.62.84", manifest.get("version") == "11.62.84")
check("manifest retains at least the 59 warp-era cases", len(manifest.get("tests", [])) >= 59)
for test_id in required:
    check(f"runtime case {test_id}", test_id in ids)

failed = [name for name, ok in checks if not ok]
payload = {
    "version": "11.62.84",
    "scope": "TC4 warp buckets/counter, Warp Ward migration, event spawning, Death Gaze cone and network sync source contracts",
    "checks_total": len(checks),
    "checks_passed": len(checks) - len(failed),
    "failed": failed,
    "runtime_verified": False,
    "known_deviations": [
        "original PacketMiscEvent warp post-processing is still approximated by particles/GUI overlays",
        "runtime event distribution, multiplayer sync and effect visuals require evidence",
    ],
}
out = ROOT / "reports/tc4_116281_warp_eldritch_parity_audit.json"
out.parent.mkdir(exist_ok=True)
out.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(f"Warp/Eldritch parity guard: {payload['checks_passed']}/{payload['checks_total']}")
if failed:
    for name in failed:
        print("FAIL:", name)
    raise SystemExit(1)
print("STATIC SOURCE/NETWORK CONTRACT PASS; runtime remains NOT TESTED")
