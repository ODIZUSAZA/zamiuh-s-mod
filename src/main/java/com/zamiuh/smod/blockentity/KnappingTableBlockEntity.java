package com.zamiuh.smod.blockentity;

import com.zamiuh.smod.component.StoneProperties;
import com.zamiuh.smod.component.StoneToolStats;
import com.zamiuh.smod.knapping.KnapGrid;
import com.zamiuh.smod.knapping.StoneMaterial;
import com.zamiuh.smod.menu.KnappingTableMenu;
import com.zamiuh.smod.registry.ModBlockEntities;
import com.zamiuh.smod.registry.ModDataComponents;
import com.zamiuh.smod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 敲石台方块实体（docs/14 v0.1.1）：
 * 打制小游戏的服务端权威数据 —— 待打制原石 + 32×32 打制网格 + 右侧 5 格打制工具槽。
 *
 * 五维隐藏属性（不显示）：岩性成分 / 成岩与风化 / 力学与断裂 / 打制性能 / 缺陷与结构。
 * 中度及以上风化的原石无法放置打制；打制产物携带石质工具组件。
 */
public class KnappingTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TOOL_SLOTS = 5;
    /** 取出成品所需的最低移除方格数。 */
    public static final int MIN_REMOVED_FOR_RESULT = 20;

    private final NonNullList<ItemStack> toolSlots = NonNullList.withSize(TOOL_SLOTS, ItemStack.EMPTY);
    private ItemStack stoneStack = ItemStack.EMPTY;
    private final KnapGrid grid = new KnapGrid();
    /** 吸附在光标上的工具（服务端权威，菜单关闭时归还槽位）。 */
    private ItemStack activeTool = ItemStack.EMPTY;

    public KnappingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KNAPPING_TABLE.get(), pos, state);
    }

    // ==================== 原石放置 / 收回 ====================

    /**
     * 手持可打制石材右键：生成五维隐藏属性（含随机风化），
     * 中度 / 重度风化的石头无法打制，直接拒绝。
     */
    public boolean tryPlaceStone(Player player) {
        if (level() == null || level().isClientSide || !stoneStack.isEmpty()) return false;
        ItemStack held = player.getMainHandItem();
        StoneMaterial material = StoneMaterial.byItem(held.getItem());
        if (material == null) return false;

        StoneProperties props = material.generate(level().random);
        if (!props.canKnapp()) {
            player.displayClientMessage(Component.translatable("message.smod.knapping.weathered"), true);
            return true; // 已处理（拒绝放置）
        }
        stoneStack = held.split(1);
        grid.generate(props.defects(), level().random);
        playKnockSound(0.6F);
        syncToClient();
        return true;
    }

    /** 潜行 + 空手右键：收回台面上的原石。 */
    public boolean retrieveStone(Player player) {
        if (level() == null || level().isClientSide || stoneStack.isEmpty()) return false;
        giveOrDrop(player, stoneStack);
        stoneStack = ItemStack.EMPTY;
        syncToClient();
        return true;
    }

    // ==================== 工具吸附 / 放下 ====================

    /** 左键点击工具栏：吸附第 slot 格工具到光标。 */
    public void attachTool(int slot) {
        if (level() == null || level().isClientSide) return;
        if (slot < 0 || slot >= TOOL_SLOTS) return;
        if (!activeTool.isEmpty() || toolSlots.get(slot).isEmpty()) return;
        activeTool = toolSlots.get(slot);
        toolSlots.set(slot, ItemStack.EMPTY);
        playKnockSound(0.4F);
        syncToClient();
    }

    /** 再次点击工具栏：放回距离点击位置最近的空槽位。 */
    public void putTool(int slotHint) {
        if (level() == null || level().isClientSide || activeTool.isEmpty()) return;
        int target = nearestEmptySlot(slotHint);
        toolSlots.set(target, activeTool);
        activeTool = ItemStack.EMPTY;
        playKnockSound(0.4F);
        syncToClient();
    }

    private int nearestEmptySlot(int hint) {
        int best = -1;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < TOOL_SLOTS; i++) {
            if (!toolSlots.get(i).isEmpty()) continue;
            int dist = Math.abs(i - hint);
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return best == -1 ? Math.max(0, Math.min(TOOL_SLOTS - 1, hint)) : best;
    }

    // ==================== 打击 ====================

    /**
     * 松开鼠标砸下：以目标格为中心，5×5 内按工具锋利度与蓄力力量移除方格；
     * 随后按「力学与断裂」判定崩边 / 炸裂。
     */
    public void strike(int x, int y, int powerPercent) {
        if (level() == null || level().isClientSide || stoneStack.isEmpty() || activeTool.isEmpty()) return;
        StoneProperties props = stoneStack.get(ModDataComponents.STONE_PROPERTIES.get());
        if (props == null || !props.canKnapp()) return;
        if (!grid.isValidTarget(x, y)) return;

        double power = Math.max(0.1, Math.min(1.0, powerPercent / 100.0));
        int removed = grid.strike(x, y, power, getToolSharpness(activeTool), level().random);
        if (removed <= 0) return;

        playKnockSound(0.9F);
        switch (grid.rollFracture(props.mechanics(), level().random)) {
            case SHATTER -> {
                // 炸裂：整块石头报废
                stoneStack = ItemStack.EMPTY;
                playerMessage("message.smod.knapping.shatter");
            }
            case EDGE_CHIP -> playerMessage("message.smod.knapping.edge_chip");
            case NONE -> { }
        }
        syncToClient();
    }

    /** 工具锋利度：打制产物取自石质工具组件；原版石材当粗制工具固定 20。 */
    private static int getToolSharpness(ItemStack tool) {
        StoneToolStats stats = tool.get(ModDataComponents.STONE_TOOL.get());
        return stats != null ? stats.sharpness() : 20;
    }

    // ==================== 取出成品 ====================

    /**
     * 取出打制成品：
     * 燧石 → 直接产出燧石碎片；石材 → 打制工具（携带石质工具组件 + 逐件最大耐久）。
     */
    public void takeResult(Player player) {
        if (level() == null || level().isClientSide || stoneStack.isEmpty()) return;
        if (grid.getRemovedCount() < MIN_REMOVED_FOR_RESULT) {
            player.displayClientMessage(Component.translatable("message.smod.knapping.not_enough"), true);
            return;
        }
        if (stoneStack.is(Items.FLINT)) {
            giveOrDrop(player, new ItemStack(ModItems.FLINT_SHARD.get(), 3 + level().random.nextInt(3)));
        } else {
            giveOrDrop(player, buildToolStone());
        }
        stoneStack = ItemStack.EMPTY;
        playKnockSound(0.7F);
        syncToClient();
    }

    /**
     * 计算成品四维：
     * - 打制度 = 缺失比例；
     * - 锋利度 = 打制性能 × 岩性上限系数；
     * - 尖锐度 = 左右上三面轮廓峰值检测；
     * - 耐久度 = 风化 / 缺陷定基础，三项正面属性合计最多削弱 15%。
     */
    private ItemStack buildToolStone() {
        StoneProperties props = stoneStack.getOrDefault(ModDataComponents.STONE_PROPERTIES.get(),
                new StoneProperties(50, 0, 50, 50, 50));
        int knappedness = grid.computeKnappedness();
        // 岩性成分决定可打性上限：岩性 100 时完整发挥打制性能，岩性 0 时减半
        int sharpness = Math.max(1, Math.min(100,
                (int) (props.knapPerf() * (0.5 + props.lithology() / 200.0))));
        int pointedness = grid.computePointedness();

        // 基础耐久分（0~100）：风化与缺陷越差越低
        float base = 100F - props.defects() * 0.35F - (props.weathering() == StoneProperties.WEATHERING_LIGHT ? 10F : 0F);
        base = Math.max(40F, Math.min(100F, base));
        // 三项正面属性作为影响因子，合计最多削弱 15%
        float factor = (knappedness + sharpness + pointedness) / 300F;
        float durabilityScore = base * (1F - 0.15F * factor);
        int maxDamage = Math.round(80 + durabilityScore * 2.2F); // 约 155 ~ 267

        ItemStack result = new ItemStack(ModItems.KNAPPING_TOOL_STONE.get());
        result.set(ModDataComponents.STONE_TOOL.get(), new StoneToolStats(knappedness, sharpness, pointedness));
        result.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, maxDamage);
        return result;
    }

    // ==================== 通用 ====================

    private void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private void playKnockSound(float volume) {
        if (level() != null) {
            level().playSound(null, worldPosition, SoundEvents.STONE_HIT, SoundSource.BLOCKS, volume, 0.8F);
        }
    }

    private void playerMessage(String key) {
        // 打制反馈广播给附近玩家（简易处理：全服范围内同维度玩家在 16 格内可见）
        if (level() == null) return;
        Component msg = Component.translatable(key);
        for (Player p : level().players()) {
            if (p.blockPosition().distSqr(worldPosition) < 16 * 16) {
                p.displayClientMessage(msg, true);
            }
        }
    }

    /** 将网格与槽位状态推送给追踪客户端（BE update tag 通道）。 */
    public void syncToClient() {
        setChanged();
        if (level() != null && !level().isClientSide) {
            level().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    public ItemStack getStoneStack() {
        return stoneStack;
    }

    public ItemStack getActiveTool() {
        return activeTool;
    }

    public NonNullList<ItemStack> getToolSlots() {
        return toolSlots;
    }

    public KnapGrid getGrid() {
        return grid;
    }

    // ==================== 菜单 ====================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.smod.knapping_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new KnappingTableMenu(containerId, playerInventory, this);
    }

    // ==================== 网络同步 ====================

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        loadAdditional(packet.getTag(), registries);
    }

    // ==================== 存取档 ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Grid", new ByteArrayTag(grid.toBytes()));
        tag.putInt("InitialFilled", grid.getInitialFilled());
        tag.putInt("StrikeCount", grid.getStrikeCount());
        tag.put("Stone", stoneStack.save(registries, new CompoundTag()));
        tag.put("ActiveTool", activeTool.save(registries, new CompoundTag()));
        ContainerHelperSaveHelper.save(tag, toolSlots, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Grid", Tag.TAG_BYTE_ARRAY)) {
            grid.fromBytes(tag.getByteArray("Grid"));
            grid.restoreStats(tag.getInt("InitialFilled"), tag.getInt("StrikeCount"));
        } else {
            grid.generate(40, net.minecraft.util.RandomSource.create());
        }
        ItemStack.parse(registries, tag.getCompound("Stone")).ifPresent(s -> this.stoneStack = s);
        ItemStack.parse(registries, tag.getCompound("ActiveTool")).ifPresent(s -> this.activeTool = s);
        for (int i = 0; i < toolSlots.size(); i++) {
            toolSlots.set(i, ItemStack.EMPTY);
        }
        ContainerHelperSaveHelper.load(tag, toolSlots, registries);
    }

    // ==================== 掉落 ====================

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            for (ItemStack stack : toolSlots) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            }
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), activeTool);
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stoneStack);
        }
    }
}
