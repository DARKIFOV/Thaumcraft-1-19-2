package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaDriveBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.blockentity.InfusionMatrixBlockEntity;
import com.darkifov.thaumcraft.menu.EssentiaTerminalMenu;
import com.darkifov.thaumcraft.thaumicenergistics.ThaumicEnergisticsNetwork;
import com.darkifov.thaumcraft.thaumicenergistics.ThaumicAeGrid;
import com.darkifov.thaumcraft.thaumicenergistics.ThaumicEnergisticsRecipeBook;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import java.util.EnumMap;
import java.util.Map;

public class ThaumicEnergisticsDeviceBlock extends Block {
    private static final VoxelShape PART_LIKE_SHAPE = Block.box(3.0D, 3.0D, 0.0D, 13.0D, 13.0D, 5.0D);

    public enum Mode {
        TERMINAL,
        STORAGE_BUS,
        IMPORT_BUS,
        EXPORT_BUS,
        INTERFACE,
        PATTERN_ENCODER,
        PATTERN_PROVIDER,
        DRIVE,
        STORAGE_MONITOR,
        ARCANE_ASSEMBLER,
        ESSENTIA_PROVIDER,
        INFUSION_PROVIDER,
        ESSENTIA_CELL_WORKBENCH,
        DISTILLATION_PATTERN_ENCODER,
        ESSENTIA_VIBRATION_CHAMBER,
        GEAR_BOX,
        GOLEM_GEAR_BOX,
        KNOWLEDGE_INSCRIBER,
        ARCANE_CRAFTING_TERMINAL,
        ESSENTIA_LEVEL_EMITTER,
        ESSENTIA_CONVERSION_MONITOR,
        VIS_INTERFACE,
        ME_CONTROLLER,
        ME_CABLE,
        CRAFTING_CPU,
        ENERGY_ACCEPTOR
    }

    private final Mode mode;

    public ThaumicEnergisticsDeviceBlock(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }


    private boolean isPartLike() {
        return mode == Mode.STORAGE_BUS
                || mode == Mode.IMPORT_BUS
                || mode == Mode.EXPORT_BUS
                || mode == Mode.STORAGE_MONITOR
                || mode == Mode.TERMINAL
                || mode == Mode.ARCANE_CRAFTING_TERMINAL
                || mode == Mode.ESSENTIA_LEVEL_EMITTER
                || mode == Mode.ESSENTIA_CONVERSION_MONITOR;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return isPartLike() ? PART_LIKE_SHAPE : super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return isPartLike() ? PART_LIKE_SHAPE : super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (mode == Mode.TERMINAL) {
            if (player.isShiftKeyDown()) {
                ThaumicEnergisticsNetwork.sendStatus(level, pos, player);
                return InteractionResult.CONSUME;
            }

            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(
                        serverPlayer,
                        new SimpleMenuProvider(
                                (int id, Inventory inventory, Player menuPlayer) -> new EssentiaTerminalMenu(id, inventory, pos),
                                Component.literal("Essentia Terminal")
                        ),
                        buffer -> buffer.writeBlockPos(pos)
                );
            }

            return InteractionResult.CONSUME;
        }

        if (mode == Mode.STORAGE_MONITOR) {
            ThaumicEnergisticsNetwork.sendStatus(level, pos, player);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.ARCANE_ASSEMBLER || mode == Mode.PATTERN_PROVIDER) {
            return handleAssembler(level, pos, player, held);
        }

        if (mode == Mode.ESSENTIA_PROVIDER) {
            int moved = ThaumicEnergisticsNetwork.importNearbyJars(level, pos, transferLimit(player));
            player.displayClientMessage(Component.literal("Essentia Provider imported nearby jars into digital storage: " + moved).withStyle(ChatFormatting.AQUA), false);
            ThaumicEnergisticsNetwork.sendStatus(level, pos, player);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.INFUSION_PROVIDER) {
            return handleInfusionProvider(level, pos, player, held);
        }

        if (mode == Mode.ESSENTIA_CELL_WORKBENCH) {
            player.displayClientMessage(Component.literal("Essentia Cell Workbench: место настройки partition/speed/fuzzy upgrades для digital cells.").withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Partition Card уже умеет привязывать aspect к cell.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.DISTILLATION_PATTERN_ENCODER) {
            if (held.getItem() instanceof EncodedEssentiaPatternItem) {
                EncodedEssentiaPatternItem.setPatternType(held, EncodedEssentiaPatternItem.PatternType.CRUCIBLE);
                EncodedEssentiaPatternItem.cycleRecipeTarget(held);
                player.displayClientMessage(Component.literal("Distillation Pattern Encoder записал crucible/distillation pattern: " + ThaumicEnergisticsRecipeBook.displayName(EncodedEssentiaPatternItem.getRecipeTarget(held))).withStyle(ChatFormatting.GOLD), false);
            } else {
                player.displayClientMessage(Component.literal("Возьми Encoded Essentia Pattern в руку.").withStyle(ChatFormatting.RED), false);
            }

            return InteractionResult.CONSUME;
        }

        if (mode == Mode.ESSENTIA_VIBRATION_CHAMBER) {
            return handleVibrationChamber(level, pos, player, held);
        }

        if (mode == Mode.KNOWLEDGE_INSCRIBER) {
            player.displayClientMessage(Component.literal("Knowledge Inscriber: используй Knowledge Core + Research Note для TE progression.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.displayClientMessage(Component.literal("Stage 80 parity: GUI class mapped from original GuiKnowledgeInscriber.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.GEAR_BOX || mode == Mode.GOLEM_GEAR_BOX) {
            player.displayClientMessage(Component.literal(mode == Mode.GEAR_BOX ? "Gear Box: механический узел для Arcane Assembler." : "Golem Gear Box: узел для wireless golem essentia integration.").withStyle(ChatFormatting.GOLD), false);
            player.displayClientMessage(Component.literal("Сеть рядом:").withStyle(ChatFormatting.GRAY), false);
            ThaumicEnergisticsNetwork.sendStatus(level, pos, player);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.ARCANE_CRAFTING_TERMINAL) {
            if (held.getItem() instanceof EncodedEssentiaPatternItem) {
                if (player.isShiftKeyDown()) {
                    ThaumicEnergisticsRecipeBook.craft(level, pos, player, held);
                } else {
                    ThaumicEnergisticsRecipeBook.diagnose(level, pos, player, held);
                }
            } else {
                player.displayClientMessage(Component.literal("Arcane Crafting Terminal: держи Encoded Pattern для диагностики/автокрафта.").withStyle(ChatFormatting.AQUA), false);
            }
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.ESSENTIA_LEVEL_EMITTER) {
            int total = ThaumicEnergisticsNetwork.totals(level, pos).values().stream().mapToInt(Integer::intValue).sum();
            player.displayClientMessage(Component.literal("Essentia Level Emitter: total network essentia = " + total).withStyle(total > 0 ? ChatFormatting.GREEN : ChatFormatting.RED), false);
            player.displayClientMessage(Component.literal("Parity behavior: emits/diagnoses threshold logic in standalone mode.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.ESSENTIA_CONVERSION_MONITOR) {
            ThaumicEnergisticsNetwork.sendStatus(level, pos, player);
            player.displayClientMessage(Component.literal("Essentia Conversion Monitor: мониторинг и конверсия aspect/crafting-aspect.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.VIS_INTERFACE) {
            player.displayClientMessage(Component.literal("Vis Interface: связывает wand/vis layer с digital essentia network.").withStyle(ChatFormatting.AQUA), false);
            ThaumicEnergisticsNetwork.sendStatus(level, pos, player);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.ME_CONTROLLER) {
            player.displayClientMessage(Component.literal("Thaumic ME Controller: controller/channel root for the standalone AE2 parity grid.").withStyle(ChatFormatting.GOLD), false);
            ThaumicAeGrid.sendStatus(level, pos, player);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.ME_CABLE) {
            player.displayClientMessage(Component.literal("Thaumic ME Cable: channel carrier for nearby TE machines.").withStyle(ChatFormatting.AQUA), false);
            ThaumicAeGrid.sendStatus(level, pos, player);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.CRAFTING_CPU) {
            if (held.getItem() instanceof EncodedEssentiaPatternItem) {
                if (ThaumicAeGrid.canScheduleCraft(level, pos, player)) {
                    if (player.isShiftKeyDown()) {
                        ThaumicEnergisticsRecipeBook.craft(level, pos, player, held);
                    } else {
                        ThaumicEnergisticsRecipeBook.diagnose(level, pos, player, held);
                    }
                }
            } else {
                player.displayClientMessage(Component.literal("Thaumic Crafting CPU: держи Encoded Pattern. Shift+ПКМ — запустить job.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                ThaumicAeGrid.sendStatus(level, pos, player);
            }
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.ENERGY_ACCEPTOR) {
            if (held.is(ThaumcraftMod.NITOR.get())) {
                held.shrink(1);
                int injected = ThaumicAeGrid.injectEnergyFromNitor(level, pos, 1024);
                player.displayClientMessage(Component.literal("Thaumic Energy Acceptor принял Nitor как " + injected + " AE energy units.").withStyle(ChatFormatting.GOLD), false);
            } else {
                player.displayClientMessage(Component.literal("Thaumic Energy Acceptor: виртуальный AE2 energy bridge. Дай Nitor для диагностики.").withStyle(ChatFormatting.GOLD), false);
            }
            ThaumicAeGrid.sendStatus(level, pos, player);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.PATTERN_ENCODER) {
            if (held.getItem() instanceof EncodedEssentiaPatternItem) {
                if (player.isShiftKeyDown()) {
                    EncodedEssentiaPatternItem.cycleRecipeTarget(held);
                    player.displayClientMessage(Component.literal("Arcane Pattern Encoder target: " + ThaumicEnergisticsRecipeBook.displayName(EncodedEssentiaPatternItem.getRecipeTarget(held))).withStyle(ChatFormatting.GOLD), false);
                } else {
                    EncodedEssentiaPatternItem.cycle(held);
                    player.displayClientMessage(Component.literal("Arcane Pattern Encoder mode: " + EncodedEssentiaPatternItem.displayName(EncodedEssentiaPatternItem.getPatternType(held))).withStyle(ChatFormatting.LIGHT_PURPLE), false);
                }
            } else {
                player.displayClientMessage(Component.literal("Arcane Pattern Encoder: возьми Encoded Essentia Pattern в руку.").withStyle(ChatFormatting.GOLD), false);
            }

            return InteractionResult.CONSUME;
        }

        int limit = transferLimit(player);
        EssentiaDriveBlockEntity drive = findAdjacentDrive(level, pos);

        if (drive != null) {
            limit = Math.max(limit, drive.transferLimit());
        }

        if (held.getItem() instanceof EssentiaCellItem) {
            return handleHeldCell(level, pos, player, held, limit);
        }

        if (mode == Mode.STORAGE_BUS) {
            if (drive != null) {
                drive.sendStatus(player);
            } else {
                ThaumicEnergisticsNetwork.sendStatus(level, pos, player);
            }

            return InteractionResult.CONSUME;
        }

        if (drive == null) {
            player.displayClientMessage(Component.literal("Нужна Digital Essentia Cell в руке или Essentia Drive рядом.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.IMPORT_BUS) {
            int moved = importJarsToDrive(level, pos, drive, limit);
            player.displayClientMessage(Component.literal("Import Bus загрузил в Drive: " + moved + " essentia. Transfer limit: " + limit).withStyle(ChatFormatting.AQUA), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.EXPORT_BUS) {
            int moved = exportDriveToJars(level, pos, drive, limit);
            player.displayClientMessage(Component.literal("Export Bus выгрузил из Drive: " + moved + " essentia. Transfer limit: " + limit).withStyle(ChatFormatting.AQUA), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.INTERFACE) {
            int moved = player.isShiftKeyDown()
                    ? exportDriveToJars(level, pos, drive, limit)
                    : importJarsToDrive(level, pos, drive, limit);

            player.displayClientMessage(Component.literal("Essentia Interface переместил через Drive: " + moved + " essentia. Transfer limit: " + limit).withStyle(ChatFormatting.AQUA), false);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleAssembler(Level level, BlockPos pos, Player player, ItemStack held) {
        if (!(held.getItem() instanceof EncodedEssentiaPatternItem)) {
            ThaumicEnergisticsNetwork.sendStatus(level, pos, player);
            player.displayClientMessage(Component.literal("Arcane Assembler: возьми Encoded Essentia Pattern. Shift+ПКМ по pattern — выбрать цель.").withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            ThaumicEnergisticsRecipeBook.craft(level, pos, player, held);
        } else {
            ThaumicEnergisticsRecipeBook.diagnose(level, pos, player, held);
        }

        return InteractionResult.CONSUME;
    }

    private InteractionResult handleInfusionProvider(Level level, BlockPos pos, Player player, ItemStack held) {
        if (held.getItem() instanceof EncodedEssentiaPatternItem && player.isShiftKeyDown()) {
            ThaumicEnergisticsRecipeBook.craft(level, pos, player, held);
            return InteractionResult.CONSUME;
        }

        InfusionMatrixBlockEntity matrix = findNearbyMatrix(level, pos, 8);

        if (matrix == null) {
            player.displayClientMessage(Component.literal("Infusion Provider: рядом нет Infusion Matrix.").withStyle(ChatFormatting.RED), false);
            player.displayClientMessage(Component.literal("Он также может автокрафтить infusion-pattern, если держать Encoded Pattern и Shift+ПКМ.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            ThaumicEnergisticsNetwork.importNearbyJars(level, pos, transferLimit(player));
            boolean started = matrix.startInfusion(player);
            player.displayClientMessage(Component.literal("Infusion Provider start request: " + started).withStyle(started ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        } else {
            player.displayClientMessage(matrix.statusComponent(), false);
            ThaumicEnergisticsNetwork.sendStatus(level, pos, player);
        }

        return InteractionResult.CONSUME;
    }

    private InteractionResult handleVibrationChamber(Level level, BlockPos pos, Player player, ItemStack held) {
        if (held.is(ThaumcraftMod.NITOR.get())) {
            held.shrink(1);
            int moved = ThaumicEnergisticsNetwork.insert(level, pos, Aspect.POTENTIA, 64);
            player.displayClientMessage(Component.literal("Essentia Vibration Chamber converted Nitor into " + moved + " Potentia essentia.").withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.literal("Essentia Vibration Chamber: дай Nitor. Рядом нужен Essentia Drive с пустой cell.").withStyle(ChatFormatting.GRAY), false);
        return InteractionResult.CONSUME;
    }

    private InfusionMatrixBlockEntity findNearbyMatrix(Level level, BlockPos pos, int radius) {
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            BlockEntity blockEntity = level.getBlockEntity(candidate);

            if (blockEntity instanceof InfusionMatrixBlockEntity matrix) {
                return matrix;
            }
        }

        return null;
    }

    private InteractionResult handleHeldCell(Level level, BlockPos pos, Player player, ItemStack held, int limit) {
        int moved;

        if (mode == Mode.IMPORT_BUS) {
            moved = importFromAdjacentJars(level, pos, held, limit);
            player.displayClientMessage(Component.literal("Import Bus загрузил в ячейку: " + moved + " essentia. Transfer limit: " + limit).withStyle(ChatFormatting.AQUA), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.EXPORT_BUS) {
            moved = exportToAdjacentJars(level, pos, held, limit);
            player.displayClientMessage(Component.literal("Export Bus выгрузил в jar: " + moved + " essentia. Transfer limit: " + limit).withStyle(ChatFormatting.AQUA), false);
            return InteractionResult.CONSUME;
        }

        if (mode == Mode.INTERFACE) {
            if (player.isShiftKeyDown()) {
                moved = exportToAdjacentJars(level, pos, held, limit);
                player.displayClientMessage(Component.literal("Essentia Interface выгрузил: " + moved + " essentia. Transfer limit: " + limit).withStyle(ChatFormatting.AQUA), false);
            } else {
                moved = importFromAdjacentJars(level, pos, held, limit);
                player.displayClientMessage(Component.literal("Essentia Interface загрузил: " + moved + " essentia. Transfer limit: " + limit).withStyle(ChatFormatting.AQUA), false);
            }

            return InteractionResult.CONSUME;
        }

        if (mode == Mode.STORAGE_BUS) {
            player.displayClientMessage(Component.literal("Storage Bus показывает ячейку как цифровое хранилище essentia.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            player.displayClientMessage(EssentiaCellItem.status(held), false);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private int transferLimit(Player player) {
        int limit = 64;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.getItem() instanceof EssentiaUpgradeCardItem card) {
                limit += card.transferBonus() * stack.getCount();
            }
        }

        return Math.min(2048, limit);
    }

    private EssentiaDriveBlockEntity findAdjacentDrive(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));

            if (blockEntity instanceof EssentiaDriveBlockEntity drive) {
                return drive;
            }
        }

        return null;
    }

    private int importFromAdjacentJars(Level level, BlockPos pos, ItemStack cell, int limit) {
        int moved = 0;

        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));

            if (blockEntity instanceof EssentiaJarBlockEntity jar) {
                moved += EssentiaCellItem.transferFromJar(cell, jar, limit - moved);

                if (moved >= limit) {
                    break;
                }
            }
        }

        return moved;
    }

    private int exportToAdjacentJars(Level level, BlockPos pos, ItemStack cell, int limit) {
        int moved = 0;

        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));

            if (blockEntity instanceof EssentiaJarBlockEntity jar) {
                moved += EssentiaCellItem.transferToJar(cell, jar, limit - moved);

                if (moved >= limit) {
                    break;
                }
            }
        }

        return moved;
    }

    private int importJarsToDrive(Level level, BlockPos pos, EssentiaDriveBlockEntity drive, int limit) {
        int moved = 0;

        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));

            if (blockEntity instanceof EssentiaJarBlockEntity jar) {
                Aspect aspect = jar.aspects().firstAspect();

                if (aspect == null) {
                    continue;
                }

                int canMove = limit - moved;

                if (canMove <= 0) {
                    break;
                }

                int removed = jar.aspects().removeUpTo(aspect, canMove);
                int inserted = drive.insertEssentia(aspect, removed);

                if (inserted < removed) {
                    jar.acceptFromTube(aspect, removed - inserted, false);
                }

                if (inserted > 0) {
                    jar.setChangedAndSync();
                    moved += inserted;
                }
            }
        }

        return moved;
    }

    private int exportDriveToJars(Level level, BlockPos pos, EssentiaDriveBlockEntity drive, int limit) {
        int moved = 0;

        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));

            if (blockEntity instanceof EssentiaJarBlockEntity jar) {
                EssentiaDriveBlockEntity.MovedEssentia pulled = drive.extractAnyEssentia(limit - moved);

                if (pulled.amount <= 0 || pulled.aspect == null) {
                    break;
                }

                int accepted = jar.acceptFromTube(pulled.aspect, pulled.amount, false);

                if (accepted < pulled.amount) {
                    drive.insertEssentia(pulled.aspect, pulled.amount - accepted);
                }

                if (accepted > 0) {
                    jar.setChangedAndSync();
                    moved += accepted;
                }

                if (moved >= limit) {
                    break;
                }
            }
        }

        return moved;
    }
}
