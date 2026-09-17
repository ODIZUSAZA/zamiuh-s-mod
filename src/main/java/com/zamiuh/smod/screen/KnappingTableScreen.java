package com.zamiuh.smod.screen;

import com.zamiuh.smod.blockentity.KnappingTableBlockEntity;
import com.zamiuh.smod.knapping.KnapGrid;
import com.zamiuh.smod.menu.KnappingTableMenu;
import com.zamiuh.smod.network.KnappingPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 敲石台打制小游戏界面（docs/13 v0.1.1）：
 * 左侧为 32×32 待打制石头剖面网格；右侧为打制工具栏（5 格，从上到下）。
 *
 * 操作流程：
 * 1. 左键点击工具栏中的工具石头 → 吸附到光标；
 * 2. 长按左键蓄力 → 蓄力条在工具旁从小到大再变小周期变化；
 * 3. 松开鼠标 → 砸向光标下方格，按工具锋利度与力量移除 5×5 内的方格；
 * 4. 再次点击工具栏 → 放下工具（自动吸附最近空槽位）；
 * 5. 点击「取出成品」按钮 → 结算打制度 / 锋利度 / 尖锐度 / 耐久度。
 */
public class KnappingTableScreen extends AbstractContainerScreen<KnappingTableMenu> {
    private static final int GRID_X = 10;
    private static final int GRID_Y = 28;
    private static final int CELL = 5;

    private static final int COLOR_BG = 0xF0121212;
    private static final int COLOR_PANEL = 0xFF202020;
    private static final int COLOR_SLOT = 0xFF2E2E2E;
    private static final int COLOR_FILLED = 0xFF9A9A9A;
    private static final int COLOR_REMOVED = 0xFF181818;
    private static final int COLOR_HOVER = 0x80FFFFFF;
    private static final int COLOR_TEXT = 0xFFE0E0E0;
    private static final int COLOR_HINT = 0xFF8A8A8A;

    /** 蓄力周期（毫秒），速度中等。 */
    private static final long CHARGE_PERIOD_MS = 1200L;

    private boolean charging = false;
    private long chargeStartMs = 0L;

    public KnappingTableScreen(KnappingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 420;
        this.imageHeight = 224;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.smod.knapping.take"),
                        button -> PacketDistributor.sendToServer(
                                new KnappingPayloads.TakeResultPayload(menu.getTable().getBlockPos())))
                .bounds(this.leftPos + 170, this.topPos + 152, 78, 18)
                .build());
    }

    // ==================== 渲染 ====================

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos,
                this.leftPos + this.imageWidth, this.topPos + this.imageHeight, COLOR_BG);

        renderGrid(guiGraphics, mouseX, mouseY);
        renderSlotBackgrounds(guiGraphics);
        renderLabels(guiGraphics);
        renderAttachedTool(guiGraphics, mouseX, mouseY);
    }

    private void renderGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        KnappingTableBlockEntity table = menu.getTable();
        KnapGrid grid = table.getGrid();
        int originX = this.leftPos + GRID_X;
        int originY = this.topPos + GRID_Y;

        guiGraphics.fill(originX - 2, originY - 2, originX + KnapGrid.SIZE * CELL + 2,
                originY + KnapGrid.SIZE * CELL + 2, COLOR_PANEL);

        int[] hover = cellUnderMouse(mouseX, mouseY);
        for (int y = 0; y < KnapGrid.SIZE; y++) {
            for (int x = 0; x < KnapGrid.SIZE; x++) {
                int color = grid.isFilled(x, y) ? COLOR_FILLED : COLOR_REMOVED;
                if (hover != null && hover[0] == x && hover[1] == y && !table.getActiveTool().isEmpty()) {
                    color = COLOR_HOVER;
                }
                guiGraphics.fill(originX + x * CELL, originY + y * CELL,
                        originX + (x + 1) * CELL, originY + (y + 1) * CELL, color);
            }
        }
    }

    private void renderSlotBackgrounds(GuiGraphics guiGraphics) {
        for (int i = 0; i < menu.getSlotCount(); i++) {
            var slot = menu.getSlot(i);
            guiGraphics.fill(this.leftPos + slot.x - 1, this.topPos + slot.y - 1,
                    this.leftPos + slot.x + 17, this.topPos + slot.y + 17, COLOR_SLOT);
        }
    }

    private void renderLabels(GuiGraphics guiGraphics) {
        KnappingTableBlockEntity table = menu.getTable();
        guiGraphics.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 8, COLOR_TEXT, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.smod.knapping.toolbar"),
                this.leftPos + KnappingTableMenu.TOOL_X, this.topPos + KnappingTableMenu.TOOL_Y - 12, COLOR_TEXT, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                this.leftPos + KnappingTableMenu.INV_X, this.topPos + KnappingTableMenu.INV_Y - 12, COLOR_TEXT, false);

        if (table.getStoneStack().isEmpty()) {
            guiGraphics.drawString(this.font,
                    Component.translatable("gui.smod.knapping.no_stone"),
                    this.leftPos + GRID_X, this.topPos + GRID_Y + 4, COLOR_HINT, false);
        }
        guiGraphics.drawString(this.font,
                Component.translatable("gui.smod.knapping.help"),
                this.leftPos + GRID_X, this.topPos + 196, COLOR_HINT, false);
    }

    private void renderAttachedTool(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack activeTool = menu.getTable().getActiveTool();
        if (activeTool.isEmpty()) {
            return;
        }
        guiGraphics.renderItem(activeTool, mouseX - 8, mouseY - 8);
        if (charging) {
            float value = chargeValue(System.currentTimeMillis());
            int barX = mouseX + 12;
            int barY = mouseY - 10;
            guiGraphics.fill(barX - 1, barY - 1, barX + 43, barY + 7, 0xFF000000);
            guiGraphics.fill(barX, barY, barX + 42, barY + 6, COLOR_PANEL);
            int width = Math.max(1, (int) (42 * value));
            int r = (int) (85 + 170 * value);
            int g = 255 - (int) (170 * value);
            guiGraphics.fill(barX, barY, barX + width, barY + 6, 0xFF000000 | (r << 16) | (g << 8) | 85);
        }
    }

    /** 蓄力条三角波：从小到大再变小循环（从按下时刻起算）。 */
    private float chargeValue(long nowMs) {
        double elapsed = ((nowMs - chargeStartMs) % CHARGE_PERIOD_MS + CHARGE_PERIOD_MS) % CHARGE_PERIOD_MS;
        double phase = elapsed / (double) CHARGE_PERIOD_MS;
        return (float) (phase < 0.5 ? phase * 2 : 2 - phase * 2);
    }

    // ==================== 鼠标交互 ====================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        KnappingTableBlockEntity table = menu.getTable();
        boolean toolAttached = !table.getActiveTool().isEmpty();

        if (button == 0 && toolAttached && charging) {
            return true; // 蓄力中忽略其他点击
        }

        // 工具栏区域
        int[] toolSlot = toolSlotUnderMouse(mouseX, mouseY);
        if (button == 0 && toolSlot != null) {
            int index = toolSlot[0];
            if (toolAttached) {
                // 再次点击工具栏 → 放下（自动吸附最近空槽位）
                PacketDistributor.sendToServer(new KnappingPayloads.PutToolPayload(table.getBlockPos(), index));
                return true;
            }
            if (!menu.getCarried().isEmpty() || hasShiftDown()) {
                return super.mouseClicked(mouseX, mouseY, button); // 原版：放入 / Shift 移动
            }
            if (!menu.getSlot(index).getItem().isEmpty()) {
                // 左键点击工具石头 → 吸附到光标
                PacketDistributor.sendToServer(new KnappingPayloads.AttachToolPayload(table.getBlockPos(), index));
                return true;
            }
            return true;
        }

        // 网格区域：长按蓄力
        if (button == 0 && toolAttached) {
            int[] cell = cellUnderMouse(mouseX, mouseY);
            if (cell != null) {
                charging = true;
                chargeStartMs = System.currentTimeMillis();
                return true;
            }
        }

        // 吸附工具时屏蔽背包原版点击，避免同时拿起物品
        if (button == 0 && toolAttached && overInventoryArea(mouseX, mouseY)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && charging) {
            charging = false;
            int[] cell = cellUnderMouse(mouseX, mouseY);
            if (cell != null && !menu.getTable().getActiveTool().isEmpty()) {
                float value = chargeValue(System.currentTimeMillis());
                int power = 10 + (int) (value * 90); // 10~100
                PacketDistributor.sendToServer(new KnappingPayloads.StrikePayload(
                        menu.getTable().getBlockPos(), cell[0], cell[1], power));
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // ==================== 坐标辅助 ====================

    /** 光标下的网格格（仅实格有效），无效返回 null。 */
    private int[] cellUnderMouse(double mouseX, double mouseY) {
        int x = (int) Math.floor((mouseX - this.leftPos - GRID_X) / CELL);
        int y = (int) Math.floor((mouseY - this.topPos - GRID_Y) / CELL);
        if (x < 0 || y < 0 || x >= KnapGrid.SIZE || y >= KnapGrid.SIZE) {
            return null;
        }
        if (!menu.getTable().getGrid().isValidTarget(x, y)) {
            return null;
        }
        return new int[]{x, y};
    }

    /** 光标下的工具槽序号，不在工具栏区域返回 null。 */
    private int[] toolSlotUnderMouse(double mouseX, double mouseY) {
        for (int i = 0; i < KnappingTableMenu.TOOL_SLOTS; i++) {
            int sx = this.leftPos + KnappingTableMenu.TOOL_X;
            int sy = this.topPos + KnappingTableMenu.TOOL_Y + i * 18;
            if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18) {
                return new int[]{i};
            }
        }
        return null;
    }

    private boolean overInventoryArea(double mouseX, double mouseY) {
        double x0 = this.leftPos + KnappingTableMenu.INV_X;
        double y0 = this.topPos + KnappingTableMenu.INV_Y - 12;
        double x1 = this.leftPos + KnappingTableMenu.INV_X + 9 * 18;
        double y1 = this.topPos + KnappingTableMenu.HOTBAR_Y + 18;
        return mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
    }
}
