package com.example.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ScrollableEffectWidget extends AbstractWidget {

    public static class EffectGridEntry {
        public final String effectId;
        public final String shortName;
        public final int level;
        public final ItemStack iconStack;

        public EffectGridEntry(String effectId, String shortName, int level, Item iconItem) {
            this.effectId = effectId;
            this.shortName = shortName;
            this.level = level;
            this.iconStack = new ItemStack(iconItem);
        }
    }

    private final List<EffectGridEntry> entries = new ArrayList<>();
    private final List<Button> gridButtons = new ArrayList<>();
    private final BiConsumer<String, Integer> onSelectionChanged;

    private double scrollAmount = 0.0;
    private int selectedIndex = 0;

    public ScrollableEffectWidget(int x, int y, int width, int height, BiConsumer<String, Integer> onSelectionChanged) {
        super(x, y, width, height, Component.literal("Scrollable Effect Grid"));
        this.onSelectionChanged = onSelectionChanged;
        buildGridEntries();
        createGridButtons();
    }

    private void buildGridEntries() {
        addEffect("minecraft:speed", "Spd", Items.SUGAR);
        addEffect("minecraft:haste", "Hst", Items.GOLDEN_PICKAXE);
        addEffect("minecraft:resistance", "Res", Items.NETHERITE_CHESTPLATE);
        addEffect("minecraft:jump_boost", "Jmp", Items.RABBIT_FOOT);
        addEffect("minecraft:strength", "Str", Items.BLAZE_POWDER);
        addEffect("minecraft:regeneration", "Reg", Items.GHAST_TEAR);
        addEffect("minecraft:night_vision", "NVs", Items.GOLDEN_CARROT);
    }

    private void addEffect(String effectId, String shortName, Item icon) {
        for (int lvl = 1; lvl <= 10; lvl++) {
            entries.add(new EffectGridEntry(effectId, shortName + " " + toRoman(lvl), lvl, icon));
        }
    }

    private static String toRoman(int num) {
        String[] roman = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (num >= 1 && num <= 10) ? roman[num] : String.valueOf(num);
    }

    private void createGridButtons() {
        gridButtons.clear();
        int btnWidth = (this.width - 14) / 2; // 2-column grid layout
        int btnHeight = 22;

        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            EffectGridEntry entry = entries.get(i);

            int col = i % 2;
            int row = i / 2;
            int btnX = this.getX() + 4 + (col * (btnWidth + 4));
            int btnY = this.getY() + 4 + (row * (btnHeight + 4));

            Button btn = Button.builder(Component.literal("  " + entry.shortName), b -> {
                this.selectedIndex = index;
                if (this.onSelectionChanged != null) {
                    this.onSelectionChanged.accept(entry.effectId, entry.level);
                }
            }).bounds(btnX, btnY, btnWidth, btnHeight).build();

            gridButtons.add(btn);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        // Container background box
        extractor.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xD0101010);
        extractor.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY(), 0xFF444444);
        extractor.fill(this.getX() - 1, this.getY() + this.height, this.getX() + this.width + 1, this.getY() + this.height + 1, 0xFF444444);

        int btnHeight = 22;
        int rowHeight = btnHeight + 4;

        for (int i = 0; i < gridButtons.size(); i++) {
            Button btn = gridButtons.get(i);
            int row = i / 2;
            int currentY = (int) (this.getY() + 4 + (row * rowHeight) - scrollAmount);
            btn.setY(currentY);

            // Scissor Viewport Check: Render only buttons inside container bounds
            if (currentY + btnHeight > this.getY() && currentY < this.getY() + this.height) {
                btn.visible = true;
                btn.extractRenderState(extractor, mouseX, mouseY, partialTick);

                // Render item icon inside button
                EffectGridEntry entry = entries.get(i);
                extractor.item(entry.iconStack, btn.getX() + 2, btn.getY() + 3);

                // Highlight selected button
                if (i == selectedIndex) {
                    extractor.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0x4000FF00);
                }
            } else {
                btn.visible = false;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOver(mouseX, mouseY)) {
            int totalRows = (entries.size() + 1) / 2;
            int totalContentHeight = totalRows * 26;
            int maxScroll = Math.max(0, totalContentHeight - this.height);
            scrollAmount = Math.max(0, Math.min(maxScroll, scrollAmount - (verticalAmount * 18)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (isMouseOver(mouseX, mouseY)) {
            for (Button btn : gridButtons) {
                if (btn.visible && btn.isMouseOver(mouseX, mouseY)) {
                    btn.onClick(event, doubleClick);
                    return;
                }
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
