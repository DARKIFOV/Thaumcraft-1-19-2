#!/usr/bin/env python3
from pathlib import Path
import json, re, sys
R=Path(__file__).resolve().parents[1]
checks=[]
def text(p):
 q=R/p; return q.read_text(encoding="utf-8",errors="ignore") if q.is_file() else ""
def ok(name,value): checks.append((name,bool(value)))
def need(path,token): ok(f"{path}:{token[:92]}",token in text(path))

build=text("build.gradle"); mods=text("src/main/resources/META-INF/mods.toml")
manifest=json.loads(text("runtime_artifacts/runtime_test_manifest.template.json")); ids={x.get("id") for x in manifest["tests"]}
ok("build_version_116319", any(f"version = '{v}'" in build for v in ['11.63.19','11.63.23']))
ok("mods_version_116319", any(f'version="{v}"' in mods for v in ['11.63.19','11.63.23']))
ok("manifest_version_116319", manifest.get("version") in {'11.63.19','11.63.23','11.63.24','11.63.26','11.63.27','11.63.28','11.63.29','11.63.30','11.63.31','11.63.32','11.63.33','11.63.36','11.63.37','11.63.38','11.63.39','11.63.40', '11.63.41', '11.63.42','11.63.43','11.63.44','11.63.45','11.63.46', '11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52','11.63.53','11.63.54', '11.63.55', '11.63.56', '11.63.58','11.63.59', '11.63.60', '11.63.61'})
ok("manifest_count_184", len(manifest.get("tests",[]))>=184)

for path,tokens in {
 "src/main/java/com/darkifov/thaumcraft/item/gear/HoverHarnessItem.java":[
  "initializeClient(Consumer<IClientItemExtensions> consumer)",
  "new com.darkifov.thaumcraft.client.render.TC4HoverHarnessClientExtension()",
  'return "thaumcraft:textures/models/hoverharness.png"',
  "isHoverEnabled(ItemStack harness)",
 ],
 "src/main/java/com/darkifov/thaumcraft/client/render/model/TC4HoverHarnessArmorModel.java":[
  "extends HumanoidModel<T>", "hover_harness_armor", "HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F)",
  '.texOffs(16, 16)', '.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))',
  "LayerDefinition.create(mesh, 128, 64)", "head.visible = false", "hat.visible = false", "body.visible = true",
  "rightArm.visible = false", "leftArm.visible = false", "rightLeg.visible = false", "leftLeg.visible = false",
 ],
 "src/main/java/com/darkifov/thaumcraft/client/render/TC4HoverHarnessClientExtension.java":[
  "implements IClientItemExtensions", "static void bake(EntityModelSet modelSet)",
  "modelSet.bakeLayer(TC4HoverHarnessArmorModel.LAYER)", "getHumanoidArmorModel", "equipmentSlot != EquipmentSlot.CHEST",
  "model.showChestOnly()", "return model;",
 ],
 "src/main/java/com/darkifov/thaumcraft/client/render/TC4HoverHarnessLayer.java":[
  "extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>", "textures/models/hoverharness2.png",
  "textures/item/tc4/lightningring.png", "RING_FRAMES = 16", "instanceof HoverHarnessItem",
  "renderBackModel(poseStack, buffers, packedLight)", "HoverHarnessItem.isHoverEnabled(harness)",
  "getParentModel().body.translateAndRotate(poseStack)", "poseStack.scale(0.1F, 0.1F, 0.1F)",
  "Vector3f.XN.rotationDegrees(90.0F)", "poseStack.translate(0.0D, 0.33D, -3.7D)",
  "RenderType.entityCutoutNoCull(BACK_TEXTURE)", "TC4HoverHarnessBackModel.render",
  "poseStack.translate(0.0D, 0.20D, 0.55D)", "Mth.floor(ageInTicks)", "RenderType.eyes(RING_TEXTURE)",
  "renderRing(poseStack.last(), rings, 2.5F", "Vector3f.YP.rotationDegrees(180.0F)",
  "poseStack.translate(0.0D, 0.0D, 0.03D)", "renderRing(poseStack.last(), rings, 1.5F",
  "LightTexture.FULL_BRIGHT", "player.tickCount / 3L", "RandomSource.create(seed)", "segments = 8",
  "buffers.getBuffer(RenderType.lines())", ".color(196, 224, 255, 230)",
 ],
 "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java":[
  "TC4HoverHarnessArmorModel.LAYER", "TC4HoverHarnessArmorModel::createBodyLayer",
  "TC4HoverHarnessClientExtension.bake(event.getEntityModels())", "renderer.addLayer(new TC4HoverHarnessLayer(renderer))",
 ],
}.items():
 for token in tokens: need(path,token)

mesh_path="src/main/java/com/darkifov/thaumcraft/client/render/model/TC4HoverHarnessBackModel.java"
mesh=text(mesh_path)
need(mesh_path,"TRIANGLE_COUNT = 124")
need(mesh_path,"Exact triangle data converted from the original TC4 textures/models/hoverharness.obj")
need(mesh_path,"for (int i = 0; i < TRIANGLES.length; i += 24)")
need(mesh_path,"emit(i + 16")
need(mesh_path,".normal(normal, TRIANGLES[i + 5], TRIANGLES[i + 6], TRIANGLES[i + 7])")
# Validate every embedded vertex tuple against the retained original OBJ.
arr=re.search(r"TRIANGLES\s*=\s*\{(.*?)\n\s*\};",mesh,re.S)
java_values=[float(x[:-1]) for x in re.findall(r"[-+]?\d+(?:\.\d+)?F",arr.group(1) if arr else "")]
obj=R/"src/main/resources/assets/thaumcraft/textures/models/hoverharness.obj"
verts=[]; uvs=[]; normals=[]; expected=[]
for raw in obj.read_text(encoding="utf-8",errors="ignore").splitlines():
 line=raw.strip()
 if line.startswith("v "): verts.append(tuple(map(float,line.split()[1:4])))
 elif line.startswith("vt "): uvs.append(tuple(map(float,line.split()[1:3])))
 elif line.startswith("vn "): normals.append(tuple(map(float,line.split()[1:4])))
 elif line.startswith("f "):
  for token in line.split()[1:]:
   vi,ti,ni=(int(x) for x in token.split("/"))
   x,y,z=verts[vi-1]; u,v=uvs[ti-1]; nx,ny,nz=normals[ni-1]
   expected.extend((x,y,z,u,1.0-v,nx,ny,nz))
ok("obj_source_124_triangles", sum(1 for l in obj.read_text().splitlines() if l.startswith("f "))==124)
ok("embedded_float_count", len(java_values)==124*3*8)
ok("embedded_obj_byte_semantics", len(java_values)==len(expected) and all(abs(a-b)<0.0000011 for a,b in zip(java_values,expected)))

for asset in [
 "src/main/resources/assets/thaumcraft/textures/models/hoverharness.obj",
 "src/main/resources/assets/thaumcraft/textures/models/hoverharness.png",
 "src/main/resources/assets/thaumcraft/textures/models/hoverharness2.png",
 "src/main/resources/assets/thaumcraft/textures/item/tc4/lightningring.png",
 "src/main/resources/assets/thaumcraft/textures/item/tc4/lightningring.png.mcmeta",
]: ok("asset:"+asset,(R/asset).is_file())

for tid in [
 "gear.hover_harness_visual_torso_shell_and_armor_texture",
 "gear.hover_harness_visual_exact_obj_back_model",
 "gear.hover_harness_visual_active_dual_lightning_rings",
 "gear.hover_harness_visual_active_local_lightning_arcs",
 "gear.hover_harness_visual_pose_swim_elytra_and_resource_reload",
 "gear.hover_harness_visual_multiplayer_visibility_and_state_sync",
]: ok("manifest:"+tid,tid in ids)
for wf in [".github/workflows/build.yml",".github/workflows/release.yml"]:
 need(wf,"tc4_116319_hover_harness_visual_parity_guard.py")
 ok("workflow_label:"+wf, any(f"Validate v{v} Hover Harness visual parity" in text(wf) for v in ["11.63.19","11.63.23"]))
need("README.md","11.63.19 — Hover Harness worn-model visual parity")
need("KNOWN_DEVIATIONS.md","11.63.19 Hover Harness visual runtime notes")
need("TC4_11.63.19_HOVER_HARNESS_VISUAL_PORT_REPORT_RU.md","Thaumostatic Harness")

failed=[name for name,value in checks if not value]
for name,value in checks: print(("PASS" if value else "FAIL")+" | "+name)
print(f"SUMMARY | {len(checks)-len(failed)}/{len(checks)} passed")
sys.exit(1 if failed else 0)
