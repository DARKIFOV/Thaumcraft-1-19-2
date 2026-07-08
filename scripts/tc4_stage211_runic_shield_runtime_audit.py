#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
ORIG = Path('/mnt/data/tc4_orig_stage211/Thaumcraft4-1.7.10-master')


def read(rel: str, root: Path = ROOT) -> str:
    path = root / rel
    if not path.exists():
        raise AssertionError(f"missing file: {rel}")
    return path.read_text(encoding='utf-8', errors='ignore')


def require(text: str, needle: str, label: str) -> None:
    if needle not in text:
        raise AssertionError(f"missing {label}: {needle}")


def require_re(text: str, pattern: str, label: str) -> None:
    if not re.search(pattern, text, re.MULTILINE | re.DOTALL):
        raise AssertionError(f"missing {label}: /{pattern}/")


def main() -> None:
    build = read('build.gradle')
    mods = read('src/main/resources/META-INF/mods.toml')
    require(build, "mappings channel: 'official', version: '1.19.2'", 'Minecraft 1.19.2 mappings')
    require(build, "net.minecraftforge:forge:1.19.2-43", 'Forge 1.19.2 dependency')
    require_re(build, r"version = '2\.(15|16)\.0'", 'Stage211+ Gradle version')
    require_re(mods, r'version="2\.(15|16)\.0"', 'Stage211+ mods.toml version')

    orig = read('thaumcraft/common/lib/events/EventHandlerRunic.java', ORIG)
    for needle, label in [
        ('runicCharge = new HashMap', 'original runicCharge map'),
        ('nextCycle = new HashMap', 'original nextCycle map'),
        ('lastCharge = new HashMap', 'original lastCharge map'),
        ('runicInfo = new HashMap', 'original runicInfo map'),
        ('upgradeCooldown = new HashMap', 'original upgradeCooldown map'),
        ('Config.shieldRecharge', 'original shieldRecharge'),
        ('Config.shieldWait', 'original shieldWait'),
        ('Config.shieldCost', 'original shieldCost'),
        ('new PacketRunicCharge', 'original PacketRunicCharge usage'),
        ('new PacketFXShield', 'original PacketFXShield usage'),
        ('runicShieldCharge', 'original charge sound'),
        ('runicShieldEffect', 'original effect sound'),
        ('getFinalCharge', 'original final charge helper'),
        ('getHardening', 'original hardening helper'),
    ]:
        require(orig, needle, label)

    runtime = read('src/main/java/com/darkifov/thaumcraft/runic/TC4RunicShieldRuntime.java')
    for needle, label in [
        ('DEFAULT_SHIELD_RECHARGE_MS = 2000', 'TC4 default recharge'),
        ('DEFAULT_SHIELD_WAIT_TICKS = 80', 'TC4 default wait'),
        ('DEFAULT_SHIELD_COST = 50', 'TC4 default cost'),
        ('RUNIC_CHARGE', 'runic charge map'),
        ('NEXT_CYCLE', 'next cycle map'),
        ('LAST_CHARGE', 'last charge map'),
        ('RUNIC_INFO', 'runic info map'),
        ('UPGRADE_COOLDOWN', 'upgrade cooldown map'),
        ('RECHARGE_DELAY', 'recharge delay map'),
        ('player.tickCount % 40 == 0', '40 tick equipment refresh'),
        ('shieldRechargeMs() - (long) info.charged * 500L', 'charged ring recharge acceleration'),
        ('consumeShieldVis(player)', 'Aer/Terra vis recharge cost'),
        ('WandItem.consumeVisFromInventory(player, Aspect.AER', 'Aer vis drain'),
        ('WandItem.consumeVisFromInventory(player, Aspect.TERRA', 'Terra vis drain'),
        ('event.setAmount(0.0F)', 'damage fully absorbed'),
        ('event.setAmount(amount - charge)', 'partial absorption'),
        ('info.kinetic', 'kinetic variant'),
        ('info.healing', 'healing variant'),
        ('info.emergency', 'emergency variant'),
        ('20_000L', '20s kinetic/healing cooldown'),
        ('60_000L', '60s emergency cooldown'),
        ('8 * info.emergency', 'emergency recharge amount'),
        ('TC4Sounds.event(ORIGINAL_CHARGE_SOUND)', 'runicShieldCharge sound'),
        ('TC4Sounds.event(ORIGINAL_EFFECT_SOUND)', 'runicShieldEffect sound'),
        ('sendRunicCharge', 'charge sync packet'),
        ('sendRunicShieldFx', 'shield FX packet'),
        ('DamageSource.FALL', 'excluded fall damage'),
        ('DamageSource.OUT_OF_WORLD', 'excluded out-of-world damage'),
        ('DamageSource.DROWN', 'excluded drown damage'),
        ('isRunicBauble(offhand)', '1.19.2 bauble adapter'),
    ]:
        require(runtime, needle, label)

    helper = read('src/main/java/com/darkifov/thaumcraft/infusion/TC4RunicArmorHelper.java')
    for needle, label in [
        ('HARDEN_TAG = "RS.HARDEN"', 'RS.HARDEN tag'),
        ('isRunicBauble', 'runic bauble discriminator'),
        ('isChargedVariant', 'charged variant discriminator'),
        ('isHealingVariant', 'healing variant discriminator'),
        ('isKineticVariant', 'kinetic variant discriminator'),
        ('isEmergencyVariant', 'emergency variant discriminator'),
        ('getFinalCharge', 'final charge helper'),
        ('getHardening', 'hardening helper'),
    ]:
        require(helper, needle, label)

    network = read('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java')
    require(network, 'PacketRunicCharge.class', 'PacketRunicCharge registration')
    require(network, 'PacketFXShield.class', 'PacketFXShield registration')
    require(network, 'sendRunicCharge(ServerPlayer player', 'sendRunicCharge method')
    require(network, 'sendRunicShieldFx(ServerLevel level', 'sendRunicShieldFx method')

    require(read('src/main/java/com/darkifov/thaumcraft/network/PacketRunicCharge.java'), 'writeInt(packet.entityId)', 'PacketRunicCharge entity id payload')
    require(read('src/main/java/com/darkifov/thaumcraft/network/PacketRunicCharge.java'), 'writeShort(packet.amount)', 'PacketRunicCharge amount payload')
    require(read('src/main/java/com/darkifov/thaumcraft/network/PacketRunicCharge.java'), 'writeShort(packet.max)', 'PacketRunicCharge max payload')
    require(read('src/main/java/com/darkifov/thaumcraft/network/PacketFXShield.java'), 'writeInt(packet.source)', 'PacketFXShield source payload')
    require(read('src/main/java/com/darkifov/thaumcraft/network/PacketFXShield.java'), 'writeInt(packet.target)', 'PacketFXShield target payload')
    require(read('src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientRunicShieldFx.java'), 'targetId == -1', 'shield FX generic hit branch')
    require(read('src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientRunicShieldFx.java'), 'targetId == -2', 'shield FX falling block branch')
    require(read('src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientRunicShieldFx.java'), 'targetId == -3', 'shield FX falling stalactite/anvil branch')
    require(read('src/main/java/com/darkifov/thaumcraft/client/RunicShieldOverlayEvents.java'), 'RunicShieldClientState.max', 'runic HUD max mirror')

    common = read('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java')
    require(common, 'TC4RunicShieldRuntime.tick(player)', 'server tick hook')
    require(common, 'TC4RunicShieldRuntime.handleHurt(event)', 'hurt hook')

    config = read('src/main/java/com/darkifov/thaumcraft/config/ThaumcraftConfig.java')
    require(config, 'RUNIC_SHIELD_RECHARGE_MS', 'runic recharge config')
    require(config, 'RUNIC_SHIELD_WAIT_TICKS', 'runic wait config')
    require(config, 'RUNIC_SHIELD_COST', 'runic vis cost config')

    sounds = read('src/main/resources/assets/thaumcraft/sounds.json')
    require(sounds, 'runicShieldEffect', 'runicShieldEffect sound json')
    require(sounds, 'runicShieldCharge', 'runicShieldCharge sound json')

    touched = [
        'src/main/java/com/darkifov/thaumcraft/runic/TC4RunicShieldRuntime.java',
        'src/main/java/com/darkifov/thaumcraft/network/PacketRunicCharge.java',
        'src/main/java/com/darkifov/thaumcraft/network/PacketFXShield.java',
        'src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientRunicShieldFx.java',
        'src/main/java/com/darkifov/thaumcraft/client/RunicShieldClientState.java',
        'src/main/java/com/darkifov/thaumcraft/client/RunicShieldOverlayEvents.java',
        'src/main/java/com/darkifov/thaumcraft/infusion/TC4RunicArmorHelper.java',
    ]
    forbidden = ['net.minecraft.item.ItemStack', 'net.minecraft.nbt.NBTTag', 'thaumcraft.api.IRunicArmor', 'func_', 'field_']
    for rel in touched:
        text = read(rel)
        for needle in forbidden:
            if needle in text:
                raise AssertionError(f'1.19.2 guard failed: {needle} appears in {rel}')

    print('Stage211 runic shield runtime 1.19.2 parity audit OK')


if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage211 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
