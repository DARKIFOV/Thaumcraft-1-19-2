from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
block = (ROOT / 'src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java').read_text(encoding='utf-8')
build = (ROOT / 'build.gradle').read_text(encoding='utf-8')
mods = (ROOT / 'src/main/resources/META-INF/mods.toml').read_text(encoding='utf-8')
readme = (ROOT / 'README.md').read_text(encoding='utf-8')

def require(cond, msg):
    if not cond:
        print(f'::error::{msg}')
        sys.exit(1)

require("version = '11.62.1'" in build, 'build.gradle version must be 11.62.1')
require('version="11.62.1"' in mods, 'mods.toml version must be 11.62.1')
require('v11.62.1' in readme, 'README hotfix marker missing')
require('level == null || level.isClientSide' in block, 'vanilla preview must not query RecipeManager on client/invalid level')
require('catch (NullPointerException | IllegalStateException exception)' in block, 'integrated-server bootstrap RecipeManager guard missing')
require('ReloadableServerResources' in block and 'getRecipeManager' in block, 'crash-context comments missing')
require('return Optional.empty();' in block, 'safe fallback must skip vanilla preview')
print('TC4 v11.62.1 integrated-server startup hotfix audit: OK')
