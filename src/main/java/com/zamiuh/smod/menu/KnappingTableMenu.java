package com.zamiuh.smod.menu;

import com.zamiuh.smod.blockentity.KnappingTableBlockEntity;
import com.zamiuh.smod.knapping.StoneMaterial;
import com.zamiuh.smod.registry.ModDataComponents;
import com.zamiuh.smod.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * 敲石台菜单（docs/14 v0.1.1）：
 * 左侧 5 格为「打制工具」槽（客户端以自定义吸附 / 放下逻辑交互，
 * Shift 点击或手持物品点击仍走原版槽位流程，便于放入 / 取出），
 * 其余为玩家背包。
 */
public class KnappingTableMenu extends AbstractContainerMenu {
    public static final int TOOL_SLOTS = KnappingTableBlockEntity.TOOL_SLOTS;
    /** 工具槽在 GUI 中的起始坐标（与 Screen 布局保持一致）。 */
    public static final int TOOL_X = 178;
    public static final int TOOL_Y = 40;
    public static final int INV_X = 250;
    public static final int INV_Y = 141;
    public static final int HOTBAR_Y = 199;

    private final KnappingTableBlockEntity table;

    /** 客户端构造：从网络数据读取方块位置并查找方块实体。 */
    public KnappingTableMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, resolveTable(playerInventory, buf.readBlockPos()));
    }

    /** 服务端构造（由 MenuProvider 调用）。 */
    public KnappingTableMenu(int containerId, Inventory playerInventory, KnappingTableBlockEntity table) {
        super(ModMenus.KNAPPING_TABLE.get(), containerId);
        this.table = table;

        // 打制工具槽 ×5
        Container toolContainer = new ToolSlotContainer(table.getToolSlots());
        for (int i = 0; i < TOOL_SLOTS; i++) {
            this.addSlot(new ToolSlot(toolContainer, i, TOOL_X, TOOL_Y + i * 18));
        }
        // 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        INV_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, INV_X + col * 18, HOTBAR_Y));
        }
    }

    private static KnappingTableBlockEntity resolveTable(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof KnappingTableBlockEntity table) {
            return table;
        }
        // 兜底：方块实体缺失时构造空实体，避免客户端崩溃
        return new KnappingTableBlockEntity(pos,
                inventory.player.level().getBlockState(pos).getBlock().defaultBlockState());
    }

    public KnappingTableBlockEntity getTable() {
        return table;
    }

    public int getSlotCount() {
        return this.slots.size();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < TOOL_SLOTS) {
            // 工具槽 → 背包
            if (!this.moveItemStackTo(stack, TOOL_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 背包 → 工具槽（仅打制工具 / 石材可放入）
            if (!this.moveItemStackTo(stack, 0, TOOL_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(table.getBlockPos().getX() + 0.5,
                table.getBlockPos().getY() + 0.5, table.getBlockPos().getZ() + 0.5) < 64.0;
    }

    /** 关闭界面时：把吸附在光标上的工具放回最近的空槽位。 */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide && !player.isRemoved() && !table.getActiveTool().isEmpty()) {
            table.putTool(-1);
        }
    }

    /** 打制工具槽：仅接受打制工具（石质工具组件）或可用作粗制工具的石材。 */
    public static boolean isValidKnappingTool(ItemStack stack) {
        return stack.has(ModDataComponents.STONE_TOOL.get()) || StoneMaterial.byItem(stack.getItem()) != null;
    }

    private static class ToolSlot extends Slot {
        public ToolSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isValidKnappingTool(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /** 桥接方块实体的工具槽列表，服务端改动实时反映到菜单同步。 */
    private static class ToolSlotContainer implements Container {
        private final NonNullList<ItemStack> items;

        public ToolSlotContainer(NonNullList<ItemStack> items) {
            this.items = items;
        }

        @Override
        public int getContainerSize() {
            return items.size();
        }

        @Override
        public boolean isEmpty() {
            return items.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int index) {
            return items.get(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            return net.minecraft.world.ContainerHelper.removeItem(items, index, count);
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            return net.minecraft.world.ContainerHelper.takeItem(items, index);
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            items.set(index, stack);
            setChanged();
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < items.size(); i++) {
                items.set(i, ItemStack.EMPTY);
            }
        }
    }
}
