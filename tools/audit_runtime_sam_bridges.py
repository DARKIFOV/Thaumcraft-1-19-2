#!/usr/bin/env python3
"""Audit the release JAR for SRG-safe client functional-interface bridges.

The v11.62.54 emergency class patcher remapped ordinary method references but did
not rename invokedynamic call-site names. Direct lambdas targeting obfuscated
Minecraft SAM interfaces therefore generated runtime classes with `create`
instead of the SRG method name (for example `m_173570_`). This audit rejects that
bytecode shape and verifies explicit adapter overrides in ClientModEvents.
"""
from __future__ import annotations
import argparse
import struct
import zipfile
from pathlib import Path

MAIN = "com/darkifov/thaumcraft/client/ClientModEvents.class"
PREFIX = "com/darkifov/thaumcraft/client/ClientModEvents$"
DANGEROUS_RETURNS = {
    "Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider;",
    "Lnet/minecraft/client/renderer/entity/EntityRendererProvider;",
    "Lnet/minecraft/client/gui/screens/MenuScreens$ScreenConstructor;",
    "Lnet/minecraft/client/color/block/BlockColor;",
    "Lnet/minecraft/client/color/item/ItemColor;",
}
EXPECTED = {
    "net/minecraft/client/renderer/blockentity/BlockEntityRendererProvider": "m_173570_",
    "net/minecraft/client/renderer/entity/EntityRendererProvider": "m_174009_",
    "net/minecraft/client/gui/screens/MenuScreens$ScreenConstructor": "m_96214_",
    "net/minecraft/client/color/block/BlockColor": "m_92566_",
    "net/minecraft/client/color/item/ItemColor": "m_92671_",
}


def u2(data: bytes, off: int) -> int:
    return struct.unpack_from(">H", data, off)[0]


def u4(data: bytes, off: int) -> int:
    return struct.unpack_from(">I", data, off)[0]


def parse(data: bytes):
    if data[:4] != b"\xca\xfe\xba\xbe":
        raise ValueError("not a class file")
    count = u2(data, 8)
    cp = [None]
    off = 10
    i = 1
    while i < count:
        tag = data[off]
        off += 1
        if tag == 1:
            size = u2(data, off)
            off += 2
            cp.append((tag, data[off:off + size].decode("utf-8")))
            off += size
        elif tag in (3, 4):
            cp.append((tag, data[off:off + 4])); off += 4
        elif tag in (5, 6):
            cp.append((tag, data[off:off + 8])); off += 8
            cp.append(None); i += 1
        elif tag in (7, 8, 16, 19, 20):
            cp.append((tag, u2(data, off))); off += 2
        elif tag in (9, 10, 11, 12, 17, 18):
            cp.append((tag, (u2(data, off), u2(data, off + 2)))); off += 4
        elif tag == 15:
            cp.append((tag, (data[off], u2(data, off + 1)))); off += 3
        else:
            raise ValueError(f"unsupported constant-pool tag {tag}")
        i += 1
    return cp, off


def utf(cp, idx: int) -> str:
    tag, value = cp[idx]
    if tag != 1:
        raise ValueError("not UTF8")
    return value


def class_name(cp, idx: int) -> str:
    tag, name_idx = cp[idx]
    if tag != 7:
        raise ValueError("not Class")
    return utf(cp, name_idx)


def name_and_type(cp, idx: int):
    tag, pair = cp[idx]
    if tag != 12:
        raise ValueError("not NameAndType")
    return utf(cp, pair[0]), utf(cp, pair[1])


def skip_attributes(data: bytes, pos: int, count: int) -> int:
    for _ in range(count):
        pos += 2
        size = u4(data, pos)
        pos += 4 + size
    return pos


def class_shape(data: bytes):
    cp, pos = parse(data)
    pos += 6  # access, this, super
    interface_count = u2(data, pos); pos += 2
    interfaces = [class_name(cp, u2(data, pos + 2 * i)) for i in range(interface_count)]
    pos += 2 * interface_count
    field_count = u2(data, pos); pos += 2
    for _ in range(field_count):
        pos += 6
        attrs = u2(data, pos); pos += 2
        pos = skip_attributes(data, pos, attrs)
    method_count = u2(data, pos); pos += 2
    methods = []
    for _ in range(method_count):
        pos += 2
        name_idx = u2(data, pos); desc_idx = u2(data, pos + 2); pos += 4
        attrs = u2(data, pos); pos += 2
        methods.append((utf(cp, name_idx), utf(cp, desc_idx)))
        pos = skip_attributes(data, pos, attrs)
    return cp, interfaces, methods


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar", type=Path, required=True)
    args = ap.parse_args()
    errors = []
    with zipfile.ZipFile(args.jar) as zf:
        names = set(zf.namelist())
        if MAIN not in names:
            errors.append(f"missing {MAIN}")
        else:
            cp, _, _ = class_shape(zf.read(MAIN))
            for entry in cp[1:]:
                if not entry or entry[0] != 18:
                    continue
                _, nat_idx = entry[1]
                name, desc = name_and_type(cp, nat_idx)
                if any(ret in desc for ret in DANGEROUS_RETURNS):
                    errors.append(f"dangerous invokedynamic {name}{desc} in {MAIN}")

        found = {}
        for name in sorted(n for n in names if n.startswith(PREFIX) and n.endswith(".class")):
            _, interfaces, methods = class_shape(zf.read(name))
            method_names = {m for m, _ in methods}
            for interface in interfaces:
                if interface in EXPECTED:
                    found[interface] = (name, method_names)
        for interface, expected_method in EXPECTED.items():
            if interface not in found:
                errors.append(f"missing explicit adapter for {interface}")
                continue
            class_file, methods = found[interface]
            if expected_method not in methods:
                errors.append(f"{class_file} does not implement {expected_method} for {interface}")

    if errors:
        print("SRG SAM bridge audit: FAILED")
        for error in errors:
            print("-", error)
        return 1
    print("SRG SAM bridge audit: OK")
    print("- no Minecraft renderer/screen/color SAM invokedynamic call sites")
    print("- 5 explicit SRG adapter overrides found")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
