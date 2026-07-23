package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Original ConfigResearch progression side-effects and ScanManager clue path. */
public final class OriginalResearchProgression {
    private OriginalResearchProgression() {}

    public static int seedAutoUnlocks(Player player) {
        if (player == null) return 0;
        int unlocked=0;
        for(String key:TC4ResearchMetadataIndex.autoUnlockKeys()) {
            Optional<ResearchEntry> entry=ResearchRegistry.byKey(key);
            if(entry.isPresent()&&PlayerThaumData.unlockResearch(player,key)) {applyUnlockSideEffects(player,entry.get());unlocked++;}
        }
        return unlocked;
    }

    /** ItemThaumonomicon normal-use repair: directly complete missing siblings of every completed original entry. */
    public static int repairCompletedSiblingsOnBookOpen(Player player) {
        if (player == null) return 0;
        int unlocked = 0;
        for (ResearchEntry entry : ResearchRegistry.originalEntries()) {
            if (!PlayerThaumData.hasResearch(player, entry.key())) continue;
            for (String siblingKey : entry.siblings()) {
                Optional<ResearchEntry> sibling = ResearchRegistry.byKey(siblingKey);
                if (sibling.isPresent() && PlayerThaumData.unlockResearch(player, sibling.get().key())) {
                    applyUnlockSideEffects(player, sibling.get());
                    unlocked++;
                }
            }
        }
        return unlocked;
    }

    public static boolean parentsComplete(Player player,ResearchEntry entry) {
        if(player==null||entry==null)return false;
        for(String r:entry.requirements())if(!PlayerThaumData.hasResearch(player,r))return false;
        for(String r:entry.hiddenRequirements())if(!PlayerThaumData.hasResearch(player,r))return false;
        return true;
    }

    /** ResearchManager.createClue: collect matching hidden/lost entries, choose exactly one, grant @KEY. */
    public static int applyScanTriggers(Player player,String itemExpression,Iterable<Aspect> aspects,String entityId) {
        if(player==null)return 0;
        seedAutoUnlocks(player);
        Set<String> raw=new LinkedHashSet<>();
        if(itemExpression!=null&&!itemExpression.isBlank())raw.addAll(TC4ResearchMetadataIndex.researchKeysForItemTrigger(itemExpression));
        if(entityId!=null&&!entityId.isBlank())raw.addAll(TC4ResearchMetadataIndex.researchKeysForEntityTrigger(entityId));
        if(aspects!=null)for(Aspect aspect:aspects)if(aspect!=null){raw.addAll(TC4ResearchMetadataIndex.researchKeysForAspectTrigger(aspect.name()));raw.addAll(TC4ResearchMetadataIndex.researchKeysForAspectTrigger(aspect.id()));}

        List<ResearchEntry> candidates=new ArrayList<>();
        for(String key:raw) {
            Optional<ResearchEntry> maybe=ResearchRegistry.byKey(key);
            if(maybe.isEmpty())continue;
            ResearchEntry entry=maybe.get();
            if(!TC4ResearchFlagPolicy.has(entry,TC4ResearchFlagPolicy.HIDDEN)&&!TC4ResearchFlagPolicy.has(entry,TC4ResearchFlagPolicy.LOST))continue;
            if(PlayerThaumData.hasResearch(player,entry.key())||PlayerThaumData.hasResearch(player,"@"+entry.key()))continue;
            candidates.add(entry);
        }
        if(candidates.isEmpty())return 0;
        ResearchEntry chosen=candidates.get(player.getRandom().nextInt(candidates.size()));
        if(!PlayerThaumData.unlockResearch(player,"@"+chosen.key()))return 0;
        if(player instanceof ServerPlayer serverPlayer)ThaumcraftNetwork.syncResearch(serverPlayer);
        return 1;
    }

    /** Compatibility entry point: scan triggers reveal a clue, never complete the research. */
    public static boolean tryUnlockTriggered(Player player,String key) {
        Optional<ResearchEntry> maybe=ResearchRegistry.byKey(key);
        if(maybe.isEmpty())return false;
        ResearchEntry entry=maybe.get();
        if(!TC4ResearchFlagPolicy.has(entry,TC4ResearchFlagPolicy.HIDDEN)&&!TC4ResearchFlagPolicy.has(entry,TC4ResearchFlagPolicy.LOST))return false;
        return PlayerThaumData.unlockResearch(player,"@"+entry.key());
    }

    public static void applyUnlockSideEffects(Player player,ResearchEntry entry){if(player!=null&&entry!=null)applyResearchWarp(player,entry.warp());}
    public static void applyResearchWarp(Player player,int rawWarp){if(player==null)return;TC4ResearchCompletionWarpParity.WarpSplit split=TC4ResearchCompletionWarpParity.splitResearchWarp(rawWarp);if(split.permanent()>0)PlayerThaumData.addWarpPermanent(player,split.permanent());if(split.sticky()>0)PlayerThaumData.addWarpSticky(player,split.sticky());}
}
