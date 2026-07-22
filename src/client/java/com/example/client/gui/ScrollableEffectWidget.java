package com.example.client.gui;

import com.example.payment.PaymentCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ScrollableEffectWidget extends AbstractWidget {

    public static class EffectEntry {
        public final String effectId;
        public final String name;
        public final int level;
        public final ItemStack iconItem;

        public EffectEntry(String effectId, String name, int level, ItemStack iconItem) {
            this.effectId = effectId;
            this.name = name;
            this.level = level;
            this.iconItem = iconItem;
        }
    }

    private final List<EffectEntry> entries = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final BiConsumer<String, Integer> onSelectionChanged;
    
    private double scrollAmount = 0.0;
    private int selectedIndex = -1;

    public ScrollableEffectWidget(int x, int y, int width, int height, BiConsumer<String, Integer> onSelectionChanged) {
        super(x, y, width, height, Component.literal("Custom Beacon Effects"));
        this.onSelectionChanged = onSelectionChanged;
        buildEntries();
        createButtons();
    }

    private void buildEntries() {
        addEffectEntries("minecraft:speed", "Speed", Items.SUGAR);
        addEffectEntries("minecraft:haste", "Haste", Items.GOLDEN_PICKAXE);
        addEffectEntries("minecraft:resistance", "Resistance", Items.NETHERITE_CHESTPLATE);
        addEffectEntries("minecraft:jump_boost", "Jump Boost", Items.RABBIT_FOOT);
        addEffectEntries("minecraft:strength", "Strength", Items.BLAZE_POWDER);
        addEffectEntries("minecraft:regeneration", "Regeneration", Items.GHAST_TEAR);
        addEffectEntries("minecraft:night_vision", "Night Vision", Items.GOLDEN_CARROT);
    }

    private void addEffectEntries(String effectId, String name, net.minecraft.world.item.Item item) {
        for (int lvl = 1; lvl <= 10; lvl++) {
            entries.add(new EffectEntry(effectId, name + " " + toRoman(lvl), lvl, new ItemStack(item)));
        }
    }

    private static String toRoman(int num) {
        String[] roman = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (num >= 1 && num <= 10) ? roman[num] : String.valueOf(num);
    }

    private void createButtons() {
        buttons.clear();
        int btnWidth = this.width - 12;
        int btnHeight = 20;

        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            EffectEntry entry = entries.get(i);

            Button btn = Button.builder(Component.literal(entry.name), b -> {
                this.selectedIndex = index;
                if (this.onSelectionChanged != null) {
                    this.onSelectionChanged.accept(entry.effectId, entry.level);
                }
            }).bounds(this.getX() + 2, this.getY() + (i * (btnHeight + 2)), btnWidth, btnHeight).build();

            buttons.add(btn);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        // Render scroll container background
        extractor.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xD0101010);
        extractor.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY(), 0xFF444444);
        extractor.fill(this.getX() - 1, this.getY() + this.height, this.getX() + this.width + 1, this.getY() + this.height + 1, 0xFF444444);

        int btnHeight = 20;
        int spacing = 2;

        for (int i = 0; i < buttons.size(); i++) {
            Button btn = buttons.get(i);
            int currentY = (int) (this.getY() + 2 + (i * (btnHeight + spacing)) - scrollAmount);
            btn.setY(currentY);

            // Scissor clipping check: only render buttons within visible widget bounds
            if (currentY + btnHeight > this.getY() && currentY < this.getY() + this.height) {
                btn.visible = true;
                btn.extractRenderState(extractor, mouseX, mouseY, partialTick);

                // Render item icon inside button
                EffectEntry entry = entries.get(i);
                extractor.item(entry.iconItem, btn.getX() + 4, btn.getY() + 2);

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
            int totalContentHeight = entries.size() * 22;
            int maxScroll = Math.max(0, totalContentHeight - this.height);
            scrollAmount = Math.max(0, Math.min(maxScroll, scrollAmount - (verticalAmount * 16)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (isMouseOver(mouseX, mouseY)) {
            for (Button btn : buttons) {
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
